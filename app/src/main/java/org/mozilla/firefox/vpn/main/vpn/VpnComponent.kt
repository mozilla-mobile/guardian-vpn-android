package org.mozilla.firefox.vpn.main.vpn

import org.mozilla.firefox.vpn.CoreComponent
import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.main.vpn.domain.VpnManagerStateProvider
import org.mozilla.firefox.vpn.servers.domain.GetServerConfigUseCase

interface VpnComponent {
    val viewModel: VpnViewModel
}

class VpnComponentImpl(
    private val coreComponent: CoreComponent,
    private val guardianComponent: GuardianComponent
) : VpnComponent, GuardianComponent by guardianComponent, CoreComponent by coreComponent {

    override val viewModel: VpnViewModel
        get() = VpnViewModel(
            application = app,
            vpnManager = vpnManager,
            vpnStateProvider = VpnManagerStateProvider(vpnManager),
            getServerConfigUseCase = GetServerConfigUseCase(app, deviceRepo),
            selectedServerProvider = selectedServerProvider
        )
}
