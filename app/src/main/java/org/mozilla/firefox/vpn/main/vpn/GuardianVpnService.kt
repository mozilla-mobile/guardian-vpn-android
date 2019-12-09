package org.mozilla.firefox.vpn.main.vpn

import android.content.Context
import android.content.Intent
import com.wireguard.android.backend.TunnelManager
import com.wireguard.android.backend.WireGuardVpnService
import org.mozilla.firefox.vpn.GuardianApp

class GuardianVpnService : WireGuardVpnService() {

    private val component by lazy {
        (applicationContext as GuardianApp).guardianComponent
    }

    override val tunnelManager: TunnelManager<*> by lazy {
        component.vpnManager.tunnelManager
    }

    companion object {
        fun getPermissionIntent(context: Context): Intent? {
            return prepare(context)
        }
    }
}
