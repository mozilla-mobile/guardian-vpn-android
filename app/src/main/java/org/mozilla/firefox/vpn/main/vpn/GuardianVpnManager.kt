package org.mozilla.firefox.vpn.main.vpn

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.wireguard.android.backend.ServiceProxy
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.TunnelManager
import com.wireguard.android.backend.VpnServiceStateListener
import com.wireguard.android.backend.isUp
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.domain.createConfig
import org.mozilla.firefox.vpn.util.GLog
import org.mozilla.firefox.vpn.util.PingUtil
import org.mozilla.firefox.vpn.util.distinctBy
import org.mozilla.firefox.vpn.util.flatMap
import org.mozilla.firefox.vpn.util.measureElapsedRealtime

class GuardianVpnManager(
    private val appContext: Context,
    private val tunnelManager: TunnelManager<*>
) : VpnManager, VpnStateProvider {
    private var serviceProxy: ServiceProxy? = null

    private val action = MutableLiveData<Action>()

    private val _stateObservable = action.switchMap { request ->
        when (request) {
            Action.Connect -> monitorConnectedState()
            Action.Disconnect -> monitorDisconnectedState()
            Action.ConnectImmediately -> monitorSignalState()
            Action.DisconnectImmediately -> liveData<VpnState> { emit(VpnState.Disconnected) }
            is Action.Switch -> monitorSwitchingState(request.oldServer, request.newServer)
        }
    }

    override val stateObservable: LiveData<VpnState> = _stateObservable
        .map { it }
        .distinctBy { v1, v2 -> v1 != v2 }

    private val connectedStatesVerifier = ConnectedStatesVerifier()

    private var upTime = 0L

    override fun isGranted(): Boolean {
        return GuardianVpnService.getPermissionIntent(appContext) == null
    }

    override suspend fun connect(
        server: ServerInfo,
        currentDevice: CurrentDevice
    ) = withContext(Dispatchers.Main.immediate) {
        when {
            isConnected() -> action.value = Action.ConnectImmediately
            else -> connectInternal(server, currentDevice)
        }
    }

    private fun connectInternal(server: ServerInfo, currentDevice: CurrentDevice) {
        val tunnel = Tunnel(TUNNEL_NAME, currentDevice.createConfig(server))

        tunnelManager.turnOn(appContext, tunnel, object : VpnServiceStateListener {
            override fun onServiceUp(proxy: ServiceProxy) {
                onVpnServiceUp(proxy, true)
                action.postValue(Action.Connect)
            }

            override fun onServiceDown(isRevoked: Boolean) {
                onVpnServiceDown(isRevoked)
            }
        })

        connectedStatesVerifier.reset(MAX_CONNECT_DURATION)
    }

    override suspend fun switch(
        oldServer: ServerInfo,
        newServer: ServerInfo,
        currentDevice: CurrentDevice
    ) = withContext(Dispatchers.Main.immediate) {
        connectedStatesVerifier.reset(MAX_SWITCH_DURATION)

        val tunnel = Tunnel(TUNNEL_NAME, currentDevice.createConfig(newServer))
        tunnelManager.turnOn(appContext, tunnel, object : VpnServiceStateListener {
            override fun onServiceUp(proxy: ServiceProxy) {
                onVpnServiceUp(proxy, false)
            }

            override fun onServiceDown(isRevoked: Boolean) {
                onVpnServiceDown(isRevoked)
            }
        })

        action.value = Action.Switch(oldServer, newServer)
    }

    private fun onVpnServiceUp(proxy: ServiceProxy, isNewConnection: Boolean) {
        this@GuardianVpnManager.serviceProxy = proxy
        if (isNewConnection) {
            upTime = SystemClock.elapsedRealtime()
        }
    }

    private fun onVpnServiceDown(isRevoked: Boolean) {
        if (isRevoked) {
            action.postValue(Action.DisconnectImmediately)
        }
        this@GuardianVpnManager.serviceProxy = null
        upTime = 0L
    }

    override suspend fun disconnect() = withContext(Dispatchers.Main.immediate) {
        tunnelManager.turnOff(appContext)
        action.value = Action.Disconnect
    }

    override suspend fun shutdownConnection() = withContext(Dispatchers.Main.immediate) {
        tunnelManager.turnOff(appContext)
        action.value = Action.DisconnectImmediately
    }

    override fun isConnected(): Boolean {
        return tunnelManager.tunnel?.isUp() ?: false
    }

    override fun getState(): VpnState {
        return if (isConnected()) {
            VpnState.Connected
        } else {
            VpnState.Disconnected
        }
    }

    override fun getDuration(): Long {
        if (upTime == 0L || tunnelManager.tunnel?.isUp() != true) {
            return 0L
        }
        return SystemClock.elapsedRealtime() - upTime
    }

    private fun monitorConnectedState(): LiveData<VpnState> {
        return liveData(Dispatchers.IO, 0) {
            emit(VpnState.Connecting as VpnState)
            action.postValue(Action.ConnectImmediately)
        }
    }

    private fun monitorSwitchingState(oldServer: ServerInfo, newServer: ServerInfo): LiveData<VpnState> {
        return liveData(Dispatchers.IO, 0) {
            emit(VpnState.Switching(oldServer, newServer) as VpnState)
            delay(MIN_SWITCH_DELAY)
            action.postValue(Action.ConnectImmediately)
        }
    }

    private suspend fun runPingLoop(tunnel: Tunnel) {
        val host = tunnel.config.peers.first().endpoint.get().host
        while (true) {
            val (pingSuccess, pingSec) = measureElapsedRealtime(TimeUnit.SECONDS) { ping(host) }
            GLog.d(TAG, "ping result=$pingSuccess, seconds=$pingSec")
            delay(PING_INTERVAL_MS)
        }
    }

    private fun monitorSignalState(): LiveData<VpnState> {
        return liveData(Dispatchers.IO, 0) {
            val tunnel = tunnelManager.tunnel ?: return@liveData

            coroutineScope {
                launch { runPingLoop(tunnel) }

                withContext(Dispatchers.Main) {
                    rxChangedFlow(tunnel)
                        .flatMap { connectedStatesVerifier.verify(it) }
                        .collect { emit(it) }
                }
            }
        }
    }

    private fun rxChangedFlow(tunnel: Tunnel): Flow<Boolean> {
        return flow {
            val serviceProxy = serviceProxy ?: run {
                emit(false)
                return@flow
            }

            var prevRx = serviceProxy.getStatistic(tunnel).totalRx()
            while (true) {
                val newRx = serviceProxy.getStatistic(tunnel).totalRx()
                emit(newRx > prevRx)
                prevRx = newRx
                delay(RX_DETECT_INTERVAL_MS)
            }
        }
    }

    private fun ping(hostAddress: String): Boolean {
        return PingUtil.echoPing(hostAddress)
    }

    private fun monitorDisconnectedState(): LiveData<VpnState> {
        return liveData(Dispatchers.IO, 0) {
            emit(VpnState.Disconnecting)
            delay(1000)
            emit(VpnState.Disconnected)
        }
    }

    private class ConnectedStatesVerifier {
        private var lastSignalTime = SystemClock.elapsedRealtime()
        private var lastStableTime = SystemClock.elapsedRealtime()

        /** Given a series of signal state (on/off) over a time period, verify() will return one of the three
         * connected states {connected, unstable, no_signal} */
        fun verify(hasSignal: Boolean): Flow<VpnState> {
            return stableFlow(hasSignal).map { verifyConnectedState(it) }
        }

        fun stableFlow(hasSignal: Boolean) = flow {
            val timeDiff = SystemClock.elapsedRealtime() - lastSignalTime
            val isUnstable = timeDiff >= UNSTABLE_THRESHOLD_MS

            when {
                hasSignal -> {
                    lastSignalTime = SystemClock.elapsedRealtime()
                    emit(true)
                }

                isUnstable -> emit(false)

                else -> GLog.d(TAG, "${(UNSTABLE_THRESHOLD_MS - timeDiff) / 1000} secs to unstable")
            }
        }

        fun verifyConnectedState(isStable: Boolean): VpnState {
            val timeDiff = SystemClock.elapsedRealtime() - lastStableTime
            val noSignal = timeDiff > NO_SIGNAL_THRESHOLD_MS

            return when {
                isStable -> {
                    lastStableTime = SystemClock.elapsedRealtime()
                    VpnState.Connected
                }

                noSignal -> VpnState.NoSignal

                else -> {
                    GLog.d(TAG, "${(NO_SIGNAL_THRESHOLD_MS - timeDiff) / 1000} secs to no-signal")
                    VpnState.Unstable
                }
            }
        }

        fun reset(initialTolerance: Long) {
            lastSignalTime = SystemClock.elapsedRealtime() - (UNSTABLE_THRESHOLD_MS - initialTolerance)
            lastStableTime = SystemClock.elapsedRealtime() - (UNSTABLE_THRESHOLD_MS - initialTolerance)
        }
    }

    /** Actions for vpn state transfer */
    sealed class Action {
        object Connect : Action()
        class Switch(val oldServer: ServerInfo, val newServer: ServerInfo) : Action()
        object Disconnect : Action()
        object ConnectImmediately : Action()
        object DisconnectImmediately : Action()
    }

    companion object {
        private const val TAG = "VpnManager"
        private const val TUNNEL_NAME = "guardian_tunnel"

        private val UNSTABLE_THRESHOLD_MS = TimeUnit.SECONDS.toMillis(30)
        private val NO_SIGNAL_THRESHOLD_MS = TimeUnit.SECONDS.toMillis(120)

        private val RX_DETECT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1)
        private val PING_INTERVAL_MS = TimeUnit.SECONDS.toMillis(2)

        private val MAX_CONNECT_DURATION = TimeUnit.SECONDS.toMillis(5)
        private val MAX_SWITCH_DURATION = TimeUnit.SECONDS.toMillis(5)

        private val MIN_SWITCH_DELAY = 500L
    }
}
