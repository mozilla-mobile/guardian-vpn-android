package org.mozilla.firefox.vpn.apptunneling

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.apptunneling.domain.AddExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetAppTunnelingSwitchStateUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetPackagesUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.RemoveExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.SwitchAppTunnelingUseCase
import org.mozilla.firefox.vpn.apptunneling.ui.AppTunnelingViewModel

interface AppTunnelingComponent {
    val viewModel: AppTunnelingViewModel
}

class AppTunnelingComponentImpl(
    private val guardianComponent: GuardianComponent
) : AppTunnelingComponent, GuardianComponent by guardianComponent {

    override val viewModel: AppTunnelingViewModel
        get() = AppTunnelingViewModel(
            getPackagesUseCase = GetPackagesUseCase(appTunnelingRepo),
            getExcludeAppUseCase = GetExcludeAppUseCase(appTunnelingRepo),
            addExcludeAppUseCase = AddExcludeAppUseCase(appTunnelingRepo),
            removeExcludeAppUseCase = RemoveExcludeAppUseCase(appTunnelingRepo),
            getAppTunnelingSwitchStateUseCase = GetAppTunnelingSwitchStateUseCase(appTunnelingRepo),
            switchStateUseCase = SwitchAppTunnelingUseCase(appTunnelingRepo)
        )
}
