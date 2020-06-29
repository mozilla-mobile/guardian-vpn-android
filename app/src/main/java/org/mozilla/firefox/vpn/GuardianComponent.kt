package org.mozilla.firefox.vpn

import com.wireguard.android.backend.TunnelManager
import org.mozilla.firefox.vpn.apptunneling.data.AppTunnelingRepository
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.main.vpn.GuardianVpnManager
import org.mozilla.firefox.vpn.main.vpn.GuardianVpnService
import org.mozilla.firefox.vpn.main.vpn.VpnManager
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.servers.domain.SelectedServerProvider
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.service.newInstance
import org.mozilla.firefox.vpn.update.UpdateManager
import org.mozilla.firefox.vpn.user.data.ReferralManager
import org.mozilla.firefox.vpn.user.data.SessionManager
import org.mozilla.firefox.vpn.user.data.UserRepository

interface GuardianComponent {
    val userRepo: UserRepository
    val deviceRepo: DeviceRepository
    val serverRepo: ServerRepository
    val appTunnelingRepo: AppTunnelingRepository
    val tunnelManager: TunnelManager<*>
    val vpnManager: VpnManager
    val userStateResolver: UserStateResolver
    val selectedServerProvider: SelectedServerProvider
    val updateManager: UpdateManager
}

class GuardianComponentImpl(
    private val coreComponent: CoreComponent
) : GuardianComponent, CoreComponent by coreComponent {

    private val sessionManager = SessionManager(prefs)

    private val referralManager = ReferralManager(coreComponent.app.applicationContext, prefs)

    var service = GuardianService.newInstance(sessionManager)

    override val userRepo: UserRepository by lazy {
        UserRepository(service, sessionManager, referralManager)
    }

    override val deviceRepo: DeviceRepository by lazy {
        DeviceRepository(service, prefs)
    }

    override val serverRepo: ServerRepository by lazy {
        ServerRepository(service, prefs)
    }

    override val appTunnelingRepo: AppTunnelingRepository by lazy {
        AppTunnelingRepository(app.packageManager, prefs)
    }

    override val tunnelManager: TunnelManager<*> = TunnelManager(GuardianVpnService::class.java)
    override var vpnManager: VpnManager = GuardianVpnManager(app, tunnelManager)

    override val userStateResolver: UserStateResolver by lazy {
        UserStateResolver(userRepo, deviceRepo).apply { refresh() }
    }

    override val selectedServerProvider: SelectedServerProvider = SelectedServerProvider(serverRepo)

    override val updateManager by lazy { UpdateManager(app) }
}
