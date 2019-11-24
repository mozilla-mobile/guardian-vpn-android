package org.mozilla.firefox.vpn.main.vpn

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.model.Tunnel
import com.wireguard.android.model.TunnelManager
import com.wireguard.android.util.AsyncWorker
import com.wireguard.config.Config
import org.mozilla.firefox.vpn.backend.FileConfigStore
import org.mozilla.firefox.vpn.main.MainActivity
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider

class VpnManager(
    private val appContext: Context,
    prefs: SharedPreferences
): VpnStateProvider {

    private val _stateObservable = MutableLiveData<VpnState>()
    override val stateObservable: LiveData<VpnState> = _stateObservable.map { it }

    private val tunnelManager = TunnelManager(
        appContext,
        prefs,
        GoBackend(
            appContext,
            // TODO: Fix this weird dependency
            ComponentName(appContext, MainActivity::class.java)
        ),
        FileConfigStore(),
        AsyncWorker(AsyncTask.SERIAL_EXECUTOR, Handler(Looper.getMainLooper()))
    )

    private var tunnel: Tunnel? = null

    private val connectTransition = listOf(VpnState.Connecting, VpnState.Connected)
    private val disconnectTransition = listOf(VpnState.Disconnecting, VpnState.Disconnected)
    private val switchTransition = listOf(VpnState.Switching, VpnState.Connected)

    fun isGranted(): Boolean {
        return GoBackend.VpnService.prepare(appContext) == null
    }

    fun connect(config: Config) {
        if (isConnected()) {
            switchTransition.dispatch()
        } else {
            connectTransition.dispatch()
        }
        tunnel = tunnelManager.create(config, TUNNEL_NAME)
        tunnel?.state = Tunnel.State.UP
    }

    fun disconnect() {
        disconnectTransition.dispatch()
        tunnel?.state = Tunnel.State.DOWN
        tunnel = null
    }

    fun isConnected(): Boolean {
        return tunnel?.state == Tunnel.State.UP
    }

    override fun getState(): VpnState {
        return if (isConnected()) {
            VpnState.Connected
        } else {
            VpnState.Disconnected
        }
    }

    private fun List<VpnState>.dispatch() {
        forEach { _stateObservable.value = it }
    }

    companion object {
        private const val TUNNEL_NAME = "guardian_tunnel"
    }
}
