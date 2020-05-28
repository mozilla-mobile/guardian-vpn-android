package org.mozilla.firefox.vpn.apptunneling

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.apptunneling.ui.AppTunnelingViewModel

interface AppTunnelingComponent {
    val viewModel: AppTunnelingViewModel
}

class AppTunnelingComponentImpl(
    private val guardianComponent: GuardianComponent
) : AppTunnelingComponent, GuardianComponent by guardianComponent {

    override val viewModel: AppTunnelingViewModel
        get() = AppTunnelingViewModel()
}
