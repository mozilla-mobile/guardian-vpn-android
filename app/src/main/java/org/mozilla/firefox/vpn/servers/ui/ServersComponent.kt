package org.mozilla.firefox.vpn.servers.ui

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase

interface ServersComponent {
    val viewModel: ServersViewModel
}

class ServersComponentImpl(
    private val guardianComponent: GuardianComponent
) : ServersComponent, GuardianComponent by guardianComponent {

    override val viewModel: ServersViewModel
        get() = ServersViewModel(
            getServersUseCase = GetServersUseCase(userRepo, serverRepo)
        )
}
