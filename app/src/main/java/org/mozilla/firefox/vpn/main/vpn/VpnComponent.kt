package org.mozilla.firefox.vpn.main.vpn

import org.mozilla.firefox.vpn.CoreComponent
import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.VpnManagerStateProvider
import org.mozilla.firefox.vpn.servers.domain.GetSelectedServerUseCase
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase

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
            selectedServerProvider = selectedServerProvider,
            getServersUseCase = GetServersUseCase(userRepo, serverRepo),
            getSelectedServerUseCase = GetSelectedServerUseCase(serverRepo),
            currentDeviceUseCase = CurrentDeviceUseCase(deviceRepo, userRepo, userStateResolver)
        )
}
