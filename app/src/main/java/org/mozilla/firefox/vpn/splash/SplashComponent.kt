package org.mozilla.firefox.vpn.splash

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.splash.domain.RefreshUserInfoUseCase
import org.mozilla.firefox.vpn.user.domain.LogoutUseCase

interface SplashComponent {
    val viewModel: SplashViewModel
}

class SplashComponentImpl(
    guardianComponent: GuardianComponent
) : SplashComponent, GuardianComponent by guardianComponent {

    override val viewModel: SplashViewModel
        get() = SplashViewModel(
            refreshUserInfoUseCase = RefreshUserInfoUseCase(userRepo),
            logoutUseCase = LogoutUseCase(userRepo, deviceRepo),
            userStates = UserStates(userStateResolver)
        )
}
