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
import com.wireguard.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.mozilla.firefox.vpn.main.MainActivity
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.util.GLog
import org.mozilla.firefox.vpn.util.PingUtil
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
            builder.setConfigureIntent(PendingIntent.getActivity(appContext, 0, configureIntent, 0))
            return builder
        }
    })

    private val connectRequest = MutableLiveData<ConnectRequest>()

    private val _stateObservable = Transformations.switchMap(connectRequest) { request ->
        when (request) {
            ConnectRequest.Connect -> monitorConnectedState()
            ConnectRequest.Disconnect -> monitorDisconnectedState()
            ConnectRequest.ForceDisconnect -> liveData { emit(VpnState.Disconnected as VpnState) }
        }
    }

    override val stateObservable: LiveData<VpnState> = _stateObservable.map { it }

    fun isGranted(): Boolean {
        return tunnelManager.isGranted()
    }

    fun connect(config: Config) {
        connectRequest.value = ConnectRequest.Connect

        val tunnel = Tunnel(TUNNEL_NAME, config)
        tunnelManager.tunnelUp(tunnel)
    }

    fun disconnect() {
        tunnelManager.tunnelDown()
        connectRequest.value = ConnectRequest.Disconnect
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

    private fun monitorConnectedState(): LiveData<VpnState> {
        return liveData(Dispatchers.IO, 0) {
            emit(VpnState.Connecting)

            if (verifyConnected()) {
                emit(VpnState.Connected)
            } else {
                connectRequest.postValue(ConnectRequest.ForceDisconnect)
                return@liveData
            }

            monitorSignal().collect {
                emit(it)
            }
        }
    }

    private suspend fun verifyConnected(): Boolean {
        // TODO: Now we just simply delay 1 second and report connected. Maybe we should block and
        // test until the internet is reachable before returning true
        delay(1000)
        return true
    }

    private suspend fun monitorSignal(): Flow<VpnState> {
        val noSignalCheckDuration = 120
        val stableCheckDuration = 30

        val noSignalThreshold = noSignalCheckDuration / stableCheckDuration
        var unstableCount = 0

        return flow {
            while (true) {
                val stable = isStable(stableCheckDuration)
                when {
                    stable -> {
                        emit(VpnState.Connected)
                        unstableCount = 0
                    }
                    unstableCount >= noSignalThreshold -> emit(VpnState.NoSignal)
                    else -> {
                        emit(VpnState.Unstable)
                        unstableCount++
                    }
                }
            }
        }
    }

    private suspend fun isStable(durationSecs: Int): Boolean {
        val tunnel = tunnelManager.currentTunnel ?: return false
        val config = tunnel.config
        val host = config.peers.first().endpoint.get().host
        val initialRx = tunnelManager.getStatistics(tunnel).totalRx()

        val pingTs = SystemClock.elapsedRealtime()
        val pingSuccess = ping(host)
        GLog.d(TAG, "ping result=${pingSuccess}, time=${SystemClock.elapsedRealtime() - pingTs}")

        val checkInterval = 5L
        repeat((durationSecs / checkInterval).toInt()) {
            delay(TimeUnit.SECONDS.toMillis(checkInterval))
            val latestRx = tunnelManager.getStatistics(tunnel).totalRx()
            GLog.d(TAG, "rx change=${latestRx - initialRx}")
        }

        val latestRx = tunnelManager.getStatistics(tunnel).totalRx()
        GLog.d(TAG, "conclude rx change=${latestRx - initialRx}")

        return latestRx > initialRx
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
        object Disconnect : ConnectRequest()
        object ForceDisconnect : ConnectRequest()
    }

    companion object {
        private const val TAG = "VpnManager"
        private const val TUNNEL_NAME = "guardian_tunnel"
    }
}
