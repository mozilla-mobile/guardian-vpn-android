package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.UserRepository

class LogoutUseCase(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository
) {

    operator fun invoke() {
        userRepository.removeUserInfo()
        deviceRepository.removeDevice()
    }
}
