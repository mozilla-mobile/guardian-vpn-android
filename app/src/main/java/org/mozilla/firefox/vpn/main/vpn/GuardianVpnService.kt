package org.mozilla.firefox.vpn.main.vpn

import android.content.Context
import android.content.Intent
import com.wireguard.android.backend.TunnelManager
import com.wireguard.android.backend.WireGuardVpnService
import org.mozilla.firefox.vpn.GuardianApp
import org.mozilla.firefox.vpn.util.NotificationUtil

class GuardianVpnService : WireGuardVpnService() {

    private val component by lazy {
        (applicationContext as GuardianApp).guardianComponent
    }

    override val tunnelManager: TunnelManager<*> by lazy {
        component.vpnManager.tunnelManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(EXTRA_COMMAND)) {
            COMMAND_TURN_ON -> startForeground(NotificationUtil.DEFAULT_NOTIFICATION_ID, NotificationUtil.createBaseBuilder(this).build())
            COMMAND_TURN_OFF -> stopForeground(false)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        fun getPermissionIntent(context: Context): Intent? {
            return prepare(context)
        }
    }
}
