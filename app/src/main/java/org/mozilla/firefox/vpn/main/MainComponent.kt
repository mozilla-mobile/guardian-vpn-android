package org.mozilla.firefox.vpn.main

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.main.settings.domain.SignOutUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.VpnManagerStateProvider
import org.mozilla.firefox.vpn.user.domain.GetVersionsUseCase
import org.mozilla.firefox.vpn.user.domain.RefreshUserInfoUseCase

interface MainComponent {
    val viewModel: MainViewModel
}

class MainComponentImpl(
    private val guardianComponent: GuardianComponent
) : MainComponent, GuardianComponent by guardianComponent {
    override val viewModel: MainViewModel
        get() = MainViewModel(
            versionsUseCase = GetVersionsUseCase(userRepo),
            signOutUseCase = SignOutUseCase(deviceRepo, userRepo, vpnManager),
            vpnStateProvider = VpnManagerStateProvider(vpnManager),
            userStates = UserStates(userStateResolver),
            refreshUserInfoUseCase = RefreshUserInfoUseCase(userRepo)
        )
}
