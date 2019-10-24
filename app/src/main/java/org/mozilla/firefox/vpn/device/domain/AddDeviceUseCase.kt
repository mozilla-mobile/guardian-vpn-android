package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result

class AddDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(token: String): Result<DeviceInfo> {
        return deviceRepository.addDevice(android.os.Build.MODEL, "Bearer $token").apply {
            userRepository.refreshUserInfo()
        }
    }
}
