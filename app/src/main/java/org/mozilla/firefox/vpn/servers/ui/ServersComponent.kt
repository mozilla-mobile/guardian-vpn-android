package org.mozilla.firefox.vpn.servers.ui

import org.mozilla.firefox.vpn.CoreComponent
import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase

interface ServersComponent {
    val viewModel: ServersViewModel
}

class ServersComponentImpl(
    private val coreComponent: CoreComponent,
    private val guardianComponent: GuardianComponent
) : ServersComponent, GuardianComponent by guardianComponent, CoreComponent by coreComponent {

    override val viewModel: ServersViewModel
        get() = ServersViewModel(
            getServersUseCase = GetServersUseCase(userRepo, serverRepo)
        )
}
