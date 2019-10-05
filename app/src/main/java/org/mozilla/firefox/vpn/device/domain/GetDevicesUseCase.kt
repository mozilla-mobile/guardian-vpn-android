package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.user.data.DeviceInfo
import org.mozilla.firefox.vpn.user.data.Result
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.user.data.mapValue

class GetDevicesUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Result<List<DeviceInfo>> {
        return userRepository.getUserInfo().mapValue { it.devices }
    }
}
