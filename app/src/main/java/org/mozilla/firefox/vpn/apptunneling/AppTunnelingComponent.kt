package org.mozilla.firefox.vpn.apptunneling

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.apptunneling.domain.AddExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetPackagesUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.RemoveExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.ui.AppTunnelingViewModel

interface AppTunnelingComponent {
    val viewModel: AppTunnelingViewModel
}

class AppTunnelingComponentImpl(
    private val guardianComponent: GuardianComponent
) : AppTunnelingComponent, GuardianComponent by guardianComponent {

    override val viewModel: AppTunnelingViewModel
        get() = AppTunnelingViewModel(
            getExcludeAppUseCase = GetExcludeAppUseCase(appTunnelingRepo),
            addExcludeAppUseCase = AddExcludeAppUseCase(appTunnelingRepo),
            getPackagesUseCase = GetPackagesUseCase(appTunnelingRepo),
            removeExcludeAppUseCase = RemoveExcludeAppUseCase(appTunnelingRepo)
        )
}
