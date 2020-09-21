package org.mozilla.firefox.vpn.onboarding

import com.hadilq.liveevent.LiveEvent
import org.mozilla.firefox.vpn.CoreComponent
import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.device.domain.AddDeviceUseCase
import org.mozilla.firefox.vpn.user.domain.CreateUserUseCase
import org.mozilla.firefox.vpn.user.domain.GetAuthCodeUseCase
import org.mozilla.firefox.vpn.user.domain.GetTokenUseCase
import org.mozilla.firefox.vpn.user.domain.SaveAuthTokenUseCase
import org.mozilla.firefox.vpn.util.StringResource

interface OnboardingComponent {
    val viewModel: OnboardingViewModel
}

class OnboardingComponentImpl(
    private val coreComponent: CoreComponent,
    private val guardianComponent: GuardianComponent
) : OnboardingComponent, GuardianComponent by guardianComponent, CoreComponent by coreComponent {

    override val viewModel: OnboardingViewModel
        get() {
            val bus = Bus()
            return OnboardingViewModel(
                bus = bus,
                createUserUseCase = CreateUserUseCase(userRepo, userStateResolver),
                addDeviceUseCase = AddDeviceUseCase(deviceRepo, userRepo),
                getTokenUseCase = GetTokenUseCase(userRepo, GetAuthCodeUseCase(bus)),
                saveAuthTokenUseCase = SaveAuthTokenUseCase(userRepo)
            )
        }
}

/**
 * Used to pass data to the presentation layer.
 *
 * Not technically a bus, but CollectionOfChannels is a bad name.
 */
class Bus {
    val toast = LiveEvent<StringResource>()
    val showLoggedOutMessage = LiveEvent<StringResource>()
    val launchMainPage = LiveEvent<Unit>()
    val closeTabsToOnboarding = LiveEvent<Unit>()
    val promptLogin = LiveEvent<String>()
}
