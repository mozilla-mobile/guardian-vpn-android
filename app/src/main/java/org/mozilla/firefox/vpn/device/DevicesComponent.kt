package org.mozilla.firefox.vpn.device

import org.mozilla.firefox.vpn.GuardianComponent
import org.mozilla.firefox.vpn.device.domain.GetDevicesUseCase
import org.mozilla.firefox.vpn.device.domain.RemoveDeviceUseCase
import org.mozilla.firefox.vpn.device.ui.DevicesViewModel

interface DevicesComponent {
    val viewModel: DevicesViewModel
}

class DevicesComponentImpl(private val guardianComponent: GuardianComponent) : DevicesComponent {

    override val viewModel: DevicesViewModel
        get() = DevicesViewModel(
            getDevices = GetDevicesUseCase(guardianComponent.userRepo),
            removeDevices = RemoveDeviceUseCase(guardianComponent.deviceRepo, guardianComponent.userRepo)
        )
}
