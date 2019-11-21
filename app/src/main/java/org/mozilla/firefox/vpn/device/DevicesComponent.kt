package org.mozilla.firefox.vpn.device

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.device.domain.*
import org.mozilla.firefox.vpn.device.ui.DevicesViewModel
import org.mozilla.firefox.vpn.user.domain.GetUserInfoUseCase

interface DevicesComponent {
    val viewModel: DevicesViewModel
}

class DevicesComponentImpl(
    private val guardianComponent: GuardianComponent
) : DevicesComponent, GuardianComponent by guardianComponent {

    override val viewModel: DevicesViewModel
        get() = DevicesViewModel(
            getDevicesUseCase = GetDevicesUseCase(userRepo, userStateResolver),
            removeDevicesUseCase = RemoveDeviceUseCase(deviceRepo, userRepo, userStateResolver),
            currentDeviceUseCase = CurrentDeviceUseCase(deviceRepo, userRepo, userStateResolver),
            getUserInfoUseCase = GetUserInfoUseCase(userRepo),
            addDeviceUseCase = AddDeviceUseCase(deviceRepo, userRepo, userStateResolver),
            getDeviceCountUseCase = GetDeviceCountUseCase(userRepo, userStateResolver),
            userStates = UserStates(userStateResolver)
        )
}
