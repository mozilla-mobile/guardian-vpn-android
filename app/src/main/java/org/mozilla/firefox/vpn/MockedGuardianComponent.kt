package org.mozilla.firefox.vpn

import com.wireguard.android.backend.TunnelManager
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.main.vpn.GuardianVpnService
import org.mozilla.firefox.vpn.main.vpn.MockVpnManager
import org.mozilla.firefox.vpn.main.vpn.VpnManager
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.servers.domain.SelectedServerProvider
import org.mozilla.firefox.vpn.service.MockGuardianService
import org.mozilla.firefox.vpn.update.UpdateManager
import org.mozilla.firefox.vpn.user.data.SessionManager
import org.mozilla.firefox.vpn.user.data.UserRepository

class MockedGuardianComponent(
    private val coreComponent: CoreComponent
) : GuardianComponent, CoreComponent by coreComponent {

    private val sessionManager = SessionManager(prefs)

    var service = MockGuardianService()

    override val userRepo: UserRepository by lazy {
        UserRepository(service, sessionManager)
    }

    override val deviceRepo: DeviceRepository by lazy {
        DeviceRepository(service, prefs)
    }

    override val serverRepo: ServerRepository by lazy {
        ServerRepository(service, prefs)
    }

    override val tunnelManager: TunnelManager<*> =
        TunnelManager(GuardianVpnService::class.java)
    override var vpnManager: VpnManager =
        MockVpnManager()

    override val userStateResolver: UserStateResolver by lazy {
        UserStateResolver(userRepo, deviceRepo).apply { refresh() }
    }

    override val selectedServerProvider: SelectedServerProvider =
        SelectedServerProvider(serverRepo)

    override val updateManager by lazy { UpdateManager(app) }
}
