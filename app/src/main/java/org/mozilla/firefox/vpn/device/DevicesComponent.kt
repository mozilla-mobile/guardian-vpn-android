package org.mozilla.firefox.vpn.device

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase
import org.mozilla.firefox.vpn.device.domain.GetDevicesUseCase
import org.mozilla.firefox.vpn.device.domain.RemoveDeviceUseCase
import org.mozilla.firefox.vpn.device.ui.DevicesViewModel

interface DevicesComponent {
    val viewModel: DevicesViewModel
}

class DevicesComponentImpl(
    private val guardianComponent: GuardianComponent
) : DevicesComponent, GuardianComponent by guardianComponent {

    override val viewModel: DevicesViewModel
        get() = DevicesViewModel(
            getDevicesUseCase = GetDevicesUseCase(userRepo),
            removeDevicesUseCase = RemoveDeviceUseCase(deviceRepo, userRepo),
            currentDeviceUseCase = CurrentDeviceUseCase(deviceRepo)
        )
}
