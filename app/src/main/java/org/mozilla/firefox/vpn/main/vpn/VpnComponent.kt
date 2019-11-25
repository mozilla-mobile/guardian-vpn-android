package org.mozilla.firefox.vpn.main.vpn

import org.mozilla.firefox.vpn.CoreComponent
import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.servers.domain.SetSelectedServerUseCase

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
            userRepository = userRepo,
            deviceRepository = deviceRepo,
            getServersUseCase = GetServersUseCase(userRepo, serverRepo),
            setSelectedServerUseCase = SetSelectedServerUseCase(serverRepo)
        )
}
