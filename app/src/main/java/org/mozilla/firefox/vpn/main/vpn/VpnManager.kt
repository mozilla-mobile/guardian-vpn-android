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

class VpnManager(
    private val appContext: Context
) : VpnStateProvider {

    val tunnelManager = TunnelManager(GuardianVpnService::class.java)
    var serviceProxy: ServiceProxy? = null

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

    private var lastSignalTime = SystemClock.elapsedRealtime()
    private var lastStableTime = SystemClock.elapsedRealtime()

    private var upTime = 0L

    private val serviceStateListener = object : VpnServiceStateListener {
        override fun onServiceUp(proxy: ServiceProxy) {
            this@VpnManager.serviceProxy = proxy
            if (upTime == 0L) {
                upTime = SystemClock.elapsedRealtime()
            }
        }

        override fun onServiceDown(isRevoked: Boolean) {
            if (isRevoked) {
                action.postValue(Action.DisconnectImmediately)
            }
            this@VpnManager.serviceProxy = null
            upTime = 0L
        }
    }

    fun isGranted(): Boolean {
        return GuardianVpnService.getPermissionIntent(appContext) == null
    }

    suspend fun connect(
        server: ServerInfo,
        currentDevice: CurrentDevice
    ) = withContext(Dispatchers.Main.immediate) {
        when {
            isConnected() -> action.value = Action.ConnectImmediately
            else -> {
                action.value = Action.Connect
                tunnelManager.turnOn(
                    appContext,
                    Tunnel(TUNNEL_NAME, currentDevice.createConfig(server)),
                    serviceStateListener
                )
            }
        }
    }

    suspend fun switch(
        oldServer: ServerInfo,
        newServer: ServerInfo,
        currentDevice: CurrentDevice
    ) = withContext(Dispatchers.Main.immediate) {
        action.value = Action.Switch(oldServer, newServer)
        tunnelManager.turnOn(
            appContext,
            Tunnel(TUNNEL_NAME, currentDevice.createConfig(newServer)),
            serviceStateListener
        )
    }

    suspend fun disconnect() = withContext(Dispatchers.Main.immediate) {
        tunnelManager.turnOff(appContext)
        action.value = Action.Disconnect
    }

    suspend fun shutdownConnection() = withContext(Dispatchers.Main.immediate) {
        tunnelManager.turnOff(appContext)
        action.value = Action.DisconnectImmediately
    }

    fun isConnected(): Boolean {
        return tunnelManager.tunnel?.isUp() ?: false
    }

    override fun getState(): VpnState {
        return if (isConnected()) {
            VpnState.Connected
        } else {
            VpnState.Disconnected
        }
    }

    fun getDuration(): Long {
        if (upTime == 0L || tunnelManager.tunnel?.isUp() != true) {
            return 0L
        }
        return SystemClock.elapsedRealtime() - upTime
    }

    private fun monitorConnectedState(): LiveData<VpnState> {
        return liveData(Dispatchers.IO, 0) {
            emit(VpnState.Connecting)

            if (verifyConnected()) {
                emit(VpnState.Connected)
                action.postValue(Action.ConnectImmediately)
            } else {
                shutdownConnection()
            }
        }
    }

    private fun monitorSwitchingState(oldServer: ServerInfo, newServer: ServerInfo): LiveData<VpnState> {
        return liveData(Dispatchers.IO, 0) {
            emit(VpnState.Switching(oldServer, newServer))

            if (verifySwitched()) {
                emit(VpnState.Connected)
                action.postValue(Action.ConnectImmediately)
            } else {
                shutdownConnection()
            }
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
        lastSignalTime = SystemClock.elapsedRealtime()
        lastStableTime = SystemClock.elapsedRealtime()

        return liveData(Dispatchers.IO, 0) {
            val tunnel = tunnelManager.tunnel ?: return@liveData

            coroutineScope {
                launch(Dispatchers.IO) { runPingLoop(tunnel) }

                withContext(Dispatchers.Main) {
                    rxChangedFlow(tunnel)
                        .flatMap { signalStableFlow(it) }
                        .flatMap { signalStateFlow(it) }
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
                delay(RX_DETECT_INTERVAL_MS)
                val newRx = serviceProxy.getStatistic(tunnel).totalRx()
                emit(newRx > prevRx)
                prevRx = newRx
            }
        }
    }

    private fun signalStableFlow(hasSignal: Boolean): Flow<Boolean> {
        return flow {
            val timeDiff = SystemClock.elapsedRealtime() - lastSignalTime
            when {
                hasSignal -> {
                    lastSignalTime = SystemClock.elapsedRealtime()
                    emit(true)
                }
                timeDiff >= UNSTABLE_THRESHOLD_MS -> emit(false)
                else -> GLog.d(TAG, "${(UNSTABLE_THRESHOLD_MS - timeDiff) / 1000} secs to unstable")
            }
        }
    }

    private fun signalStateFlow(isStable: Boolean): Flow<VpnState> {
        return flow {
            val timeDiff = SystemClock.elapsedRealtime() - lastStableTime
            when {
                isStable -> {
                    lastStableTime = SystemClock.elapsedRealtime()
                    emit(VpnState.Connected)
                }
                SystemClock.elapsedRealtime() - lastStableTime > NO_SIGNAL_THRESHOLD_MS -> emit(VpnState.NoSignal)
                else -> {
                    GLog.d(TAG, "${(NO_SIGNAL_THRESHOLD_MS - timeDiff) / 1000} secs to no-signal")
                    emit(VpnState.Unstable)
                }
            }
        }
    }

    private suspend fun verifyConnected(): Boolean {
        // TODO: Now we just simply delay 1 second and report connected. Maybe we should block and
        // test until the internet is reachable before returning true
        delay(1000)
        return true
    }

    private suspend fun verifySwitched(): Boolean {
        delay(1500)
        return true
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
    }
}
