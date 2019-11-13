package org.mozilla.firefox.vpn.main.vpn

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.model.Tunnel
import com.wireguard.android.model.TunnelManager
import com.wireguard.android.util.AsyncWorker
import com.wireguard.config.Config
import org.mozilla.firefox.vpn.backend.FileConfigStore
import org.mozilla.firefox.vpn.main.MainActivity

class VpnManager(
    private val appContext: Context,
    private val prefs: SharedPreferences
) {

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

    fun isGranted(): Boolean {
        return GoBackend.VpnService.prepare(appContext) == null
    }

    fun connect(name: String, config: Config) {
        tunnel = tunnelManager.create(config, name)
        tunnel?.state = Tunnel.State.UP
    }

    fun disconnect() {
        tunnel?.state = Tunnel.State.DOWN
        tunnel = null
    }

    fun isConnected(): Boolean {
        return tunnel?.state == Tunnel.State.UP
    }
}
