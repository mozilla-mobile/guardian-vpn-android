package org.mozilla.firefox.vpn.device

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.device.domain.AddDeviceUseCase
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase
import org.mozilla.firefox.vpn.device.domain.GetDevicesUseCase
import org.mozilla.firefox.vpn.device.domain.RemoveDeviceUseCase
import org.mozilla.firefox.vpn.device.ui.DevicesViewModel
import org.mozilla.firefox.vpn.user.domain.LogoutUseCase
import org.mozilla.firefox.vpn.user.domain.NotifyUserStateUseCase
import org.mozilla.firefox.vpn.user.domain.RefreshUserInfoUseCase

interface DevicesComponent {
    val viewModel: DevicesViewModel
}

class DevicesComponentImpl(
    private val guardianComponent: GuardianComponent
) : DevicesComponent, GuardianComponent by guardianComponent {

    override val viewModel: DevicesViewModel
        get() = DevicesViewModel(
            getDevicesUseCase = GetDevicesUseCase(userRepo, userStateResolver),
            removeDevicesUseCase = RemoveDeviceUseCase(deviceRepo, userRepo),
            currentDeviceUseCase = CurrentDeviceUseCase(deviceRepo, userRepo, userStateResolver),
            addDeviceUseCase = AddDeviceUseCase(deviceRepo, userRepo),
            userStates = UserStates(userStateResolver),
            logoutUseCase = LogoutUseCase(userRepo, deviceRepo),
            notifyUserStateUseCase = NotifyUserStateUseCase(userStateResolver),
            refreshUserInfoUseCase = RefreshUserInfoUseCase(userRepo)
        )
}
