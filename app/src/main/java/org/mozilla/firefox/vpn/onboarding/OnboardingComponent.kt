package org.mozilla.firefox.vpn.onboarding

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.device.domain.AddDeviceUseCase
import org.mozilla.firefox.vpn.user.domain.CreateUserUseCase
import org.mozilla.firefox.vpn.user.domain.GetLoginInfoUseCase
import org.mozilla.firefox.vpn.user.domain.VerifyLoginUseCase

interface OnboardingComponent {
    val viewModel: OnboardingViewModel
}

class OnboardingComponentImpl(
    private val guardianComponent: GuardianComponent
) : OnboardingComponent, GuardianComponent by guardianComponent {

    override val viewModel: OnboardingViewModel
        get() = OnboardingViewModel(
            loginInfoUseCase = GetLoginInfoUseCase(userRepo),
            verifyLoginUseCase = VerifyLoginUseCase(userRepo),
            createUserUseCase = CreateUserUseCase(userRepo),
            addDeviceUseCase = AddDeviceUseCase(deviceRepo, userRepo)
        )
}
