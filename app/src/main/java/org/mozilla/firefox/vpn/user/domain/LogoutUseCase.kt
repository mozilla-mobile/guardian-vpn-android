package org.mozilla.firefox.vpn.user.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.GLog

class LogoutUseCase(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository
) {

    operator fun invoke() {
        GLog.report(TAG, "logged out")
        userRepository.removeUserInfo()
        deviceRepository.removeDevice()
    }

    companion object {
        private const val TAG = "LogoutUseCase"
    }
}
