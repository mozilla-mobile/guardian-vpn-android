package org.mozilla.firefox.vpn.splash

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.UserStates

interface SplashComponent {
    val viewModel: SplashViewModel
}

class SplashComponentImpl(
    guardianComponent: GuardianComponent
) : SplashComponent, GuardianComponent by guardianComponent {

    override val viewModel: SplashViewModel
        get() = SplashViewModel(
            userStates = UserStates(userStateResolver)
        )
}
