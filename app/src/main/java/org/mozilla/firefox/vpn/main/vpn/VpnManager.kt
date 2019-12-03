package org.mozilla.firefox.vpn.main.vpn

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService.Builder
import android.os.SystemClock
import androidx.lifecycle.*
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.TunnelManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.main.MainActivity
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.domain.createConfig
import org.mozilla.firefox.vpn.util.GLog
import org.mozilla.firefox.vpn.util.PingUtil
import org.mozilla.firefox.vpn.util.flatMap
import org.mozilla.firefox.vpn.util.measureElapsedRealtime
import java.util.concurrent.TimeUnit

class VpnManager(
    private val appContext: Context
): VpnStateProvider {

    private val tunnelManager = TunnelManager(appContext, object: TunnelManager.VpnBuilderProvider{
        override fun patchBuilder(builder: Builder): Builder {
            val configureIntent = Intent()
            // TODO: Fix this weird dependency
            configureIntent.component = ComponentName(appContext, MainActivity::class.java)
            configureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            builder.setConfigureIntent(
                PendingIntent.getActivity(appContext, 0, configureIntent, 0)
            )
            return builder
        }
    })

    private val connectRequest = MutableLiveData<ConnectRequest>()

    private val _stateObservable = connectRequest.switchMap { request ->
        when (request) {
            ConnectRequest.Connect -> monitorConnectedState()
            ConnectRequest.Disconnect -> monitorDisconnectedState()
            ConnectRequest.ForceConnected -> monitorSignalState()
            ConnectRequest.ForceDisconnect -> liveData<VpnState> { emit(VpnState.Disconnected) }
            is ConnectRequest.Switch -> monitorSwitchingState(request.oldServer, request.newServer)
        }
    }

    override val stateObservable: LiveData<VpnState> = _stateObservable.map { it }

    private var lastSignalTime = SystemClock.elapsedRealtime()
    private var lastStableTime = SystemClock.elapsedRealtime()

    fun isGranted(): Boolean {
        return tunnelManager.isGranted()
    }

    suspend fun connect(
        server: ServerInfo,
        currentDevice: CurrentDevice
    ) = withContext(Dispatchers.Main.immediate) {
        when {
            isConnected() -> connectRequest.value = ConnectRequest.ForceConnected
            else -> {
                connectRequest.value = ConnectRequest.Connect
                val tunnel = Tunnel(TUNNEL_NAME, currentDevice.createConfig(server))
                tunnelManager.tunnelUp(tunnel)
            }
        }
    }

    suspend fun switch(
        oldServer: ServerInfo,
        newServer: ServerInfo,
        currentDevice: CurrentDevice
    ) = withContext(Dispatchers.Main.immediate) {
        connectRequest.value = ConnectRequest.Switch(oldServer, newServer)
        val tunnel = Tunnel(TUNNEL_NAME, currentDevice.createConfig(newServer))
        tunnelManager.tunnelUp(tunnel)
    }

    suspend fun disconnect() = withContext(Dispatchers.Main.immediate) {
        tunnelManager.tunnelDown()
        connectRequest.value = ConnectRequest.Disconnect
    }

    suspend fun shutdownConnection() = withContext(Dispatchers.Main.immediate) {
        tunnelManager.tunnelDown()
        connectRequest.value = ConnectRequest.ForceDisconnect
    }

    fun isConnected(): Boolean {
        return tunnelManager.isConnected()
    }

    override fun getState(): VpnState {
        return if (isConnected()) {
            VpnState.Connected
        } else {
            VpnState.Disconnected
        }
    }

    fun getDuration(): Long {
        return tunnelManager.upDuration
    }

    private fun monitorConnectedState(): LiveData<VpnState> {
        return liveData(Dispatchers.IO, 0) {
            emit(VpnState.Connecting)

            if (verifyConnected()) {
                emit(VpnState.Connected)
                connectRequest.postValue(ConnectRequest.ForceConnected)
            } else {
                shutdownConnection()
            }
        }
    }

    private fun monitorSwitchingState(oldServer: ServerInfo, newServer: ServerInfo): LiveData<VpnState> {
        return liveData(Dispatchers.IO, 0) {
            emit(VpnState.Switching(oldServer, newServer))

            if (verifyConnected()) {
                emit(VpnState.Connected)
                connectRequest.postValue(ConnectRequest.ForceConnected)
            } else {
                shutdownConnection()
            }
        }
    }

    private suspend fun runPingLoop(tunnel: Tunnel) {
        val host = tunnel.config.peers.first().endpoint.get().host
        while (true) {
            val (pingSuccess, pingSec) = measureElapsedRealtime(TimeUnit.SECONDS) { ping(host) }
            GLog.d(TAG, "ping result=${pingSuccess}, seconds=${pingSec}")
            delay(PING_INTERVAL_MS)
        }
    }

    private fun monitorSignalState(): LiveData<VpnState> {
        lastSignalTime = SystemClock.elapsedRealtime()
        lastStableTime = SystemClock.elapsedRealtime()

        return liveData(Dispatchers.IO, 0) {
            val tunnel = tunnelManager.currentTunnel ?: return@liveData

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
            var prevRx = tunnelManager.getStatistics(tunnel).totalRx()
            while (true) {
                delay(RX_DETECT_INTERVAL_MS)
                emit(tunnelManager.getStatistics(tunnel).totalRx() > prevRx)
                prevRx = tunnelManager.getStatistics(tunnel).totalRx()
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

    sealed class ConnectRequest {
        object Connect : ConnectRequest()
        class Switch(val oldServer: ServerInfo, val newServer: ServerInfo) : ConnectRequest()
        object Disconnect : ConnectRequest()
        object ForceConnected : ConnectRequest()
        object ForceDisconnect : ConnectRequest()
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
