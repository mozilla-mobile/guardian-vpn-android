package org.mozilla.firefox.vpn.onboarding

import org.mozilla.firefox.vpn.CoreComponent
import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.device.domain.AddDeviceUseCase
import org.mozilla.firefox.vpn.onboarding.domain.ClearPendingLoginInfoUseCase
import org.mozilla.firefox.vpn.onboarding.domain.GetPendingLoginInfoUseCase
import org.mozilla.firefox.vpn.onboarding.domain.SetPendingLoginInfoUseCase
import org.mozilla.firefox.vpn.user.domain.CreateUserUseCase
import org.mozilla.firefox.vpn.user.domain.GetLoginInfoUseCase
import org.mozilla.firefox.vpn.user.domain.VerifyLoginUseCase

interface OnboardingComponent {
    val viewModel: OnboardingViewModel
}

class OnboardingComponentImpl(
    private val coreComponent: CoreComponent,
    private val guardianComponent: GuardianComponent
) : OnboardingComponent, GuardianComponent by guardianComponent, CoreComponent by coreComponent {

    override val viewModel: OnboardingViewModel
        get() = OnboardingViewModel(
            loginInfoUseCase = GetLoginInfoUseCase(userRepo),
            verifyLoginUseCase = VerifyLoginUseCase(userRepo),
            createUserUseCase = CreateUserUseCase(userRepo, userStateResolver),
            addDeviceUseCase = AddDeviceUseCase(deviceRepo, userRepo),
            setPendingLoginInfoUseCase = SetPendingLoginInfoUseCase(prefs),
            getPendingLoginInfoUseCase = GetPendingLoginInfoUseCase(prefs),
            clearPendingLoginInfoUseCase = ClearPendingLoginInfoUseCase(prefs)
        )
}
