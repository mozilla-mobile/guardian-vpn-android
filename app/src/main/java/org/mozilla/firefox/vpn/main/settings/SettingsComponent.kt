package org.mozilla.firefox.vpn.main.settings

import org.mozilla.firefox.vpn.GuardianComponent

interface SettingsComponent {
    val viewModel: SettingsViewModel
}

class SettingsComponentImpl(
    private val guardianComponent: GuardianComponent
) : SettingsComponent, GuardianComponent by guardianComponent {

    override val viewModel: SettingsViewModel = SettingsViewModel(deviceRepo, userRepo)
}
