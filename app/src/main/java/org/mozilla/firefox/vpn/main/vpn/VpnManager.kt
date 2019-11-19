package org.mozilla.firefox.vpn.main.vpn

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService.Builder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.TunnelManager
import com.wireguard.config.Config
import org.mozilla.firefox.vpn.main.MainActivity
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider

class VpnManager(
    private val appContext: Context
): VpnStateProvider {

    private val _stateObservable = MutableLiveData<VpnState>()
    override val stateObservable: LiveData<VpnState> = _stateObservable.map { it }

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

    private val connectTransition = listOf(VpnState.Connecting, VpnState.Connected)
    private val disconnectTransition = listOf(VpnState.Disconnecting, VpnState.Disconnected)
    private val switchTransition = listOf(VpnState.Switching, VpnState.Connected)

    fun isGranted(): Boolean {
        return tunnelManager.isGranted()
    }

    fun connect(config: Config) {
        if (isConnected()) {
            switchTransition.dispatch()
        } else {
            connectTransition.dispatch()
        }
        tunnelManager.tunnelUp(Tunnel(TUNNEL_NAME, config))
    }

    fun disconnect() {
        disconnectTransition.dispatch()
        tunnelManager.tunnelDown()
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

    private fun List<VpnState>.dispatch() {
        forEach { _stateObservable.value = it }
    }

    companion object {
        private const val TUNNEL_NAME = "guardian_tunnel"
    }
}
