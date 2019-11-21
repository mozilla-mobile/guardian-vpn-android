package org.mozilla.firefox.vpn.device

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.device.domain.AddDeviceUseCase
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase
import org.mozilla.firefox.vpn.device.domain.GetDevicesUseCase
import org.mozilla.firefox.vpn.device.domain.RemoveDeviceUseCase
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
            getDevicesUseCase = GetDevicesUseCase(userRepo),
            removeDevicesUseCase = RemoveDeviceUseCase(deviceRepo, userRepo, userStateResolver),
            currentDeviceUseCase = CurrentDeviceUseCase(deviceRepo),
            getUserInfoUseCase = GetUserInfoUseCase(userRepo),
            addDeviceUseCase = AddDeviceUseCase(deviceRepo, userRepo, userStateResolver),
            userStates = UserStates(userStateResolver)
        )
}
