package org.mozilla.firefox.vpn.help

import org.mozilla.firefox.vpn.CoreComponent
import org.mozilla.firefox.vpn.GuardianComponent

interface HelpComponent {
    val viewModel: HelpViewModel
}

class HelpComponentImpl(
    private val coreComponent: CoreComponent,
    private val guardianComponent: GuardianComponent
) : HelpComponent, GuardianComponent by guardianComponent, CoreComponent by coreComponent {

    override val viewModel: HelpViewModel = HelpViewModel(
        app = app,
        userRepository = userRepo,
        deviceRepository = deviceRepo
    )
}
