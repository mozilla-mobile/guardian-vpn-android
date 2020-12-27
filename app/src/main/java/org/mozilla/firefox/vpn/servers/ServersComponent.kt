package org.mozilla.firefox.vpn.servers

import org.mozilla.firefox.vpn.CoreComponent
import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.main.vpn.domain.VpnManagerStateProvider
import org.mozilla.firefox.vpn.servers.domain.GetSelectedServerUseCase
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.servers.domain.SetSelectedServerUseCase
import org.mozilla.firefox.vpn.servers.ui.ServersViewModel

interface ServersComponent {
    val viewModel: ServersViewModel
}

class ServersComponentImpl(
    private val coreComponent: CoreComponent,
    private val guardianComponent: GuardianComponent
) : ServersComponent, GuardianComponent by guardianComponent, CoreComponent by coreComponent {

    override val viewModel: ServersViewModel
        get() = ServersViewModel(
            vpnStateProvider = VpnManagerStateProvider(vpnManager),
            getServersUseCase = GetServersUseCase(serverRepo),
            setSelectedServerUseCase = SetSelectedServerUseCase(serverRepo, selectedServerProvider),
            getSelectedServerUseCase = GetSelectedServerUseCase(serverRepo)
        )
}
