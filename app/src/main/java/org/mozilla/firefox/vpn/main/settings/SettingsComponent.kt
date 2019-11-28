package org.mozilla.firefox.vpn.main.settings

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.main.settings.domain.SignOutUseCase

interface SettingsComponent {
    val viewModel: SettingsViewModel
}

class SettingsComponentImpl(
    private val guardianComponent: GuardianComponent
) : SettingsComponent, GuardianComponent by guardianComponent {

    override val viewModel: SettingsViewModel = SettingsViewModel(
        userRepository = userRepo,
        userStates = UserStates(userStateResolver),
        signOutUseCase = SignOutUseCase(deviceRepo, userRepo, vpnManager)
    )
}
