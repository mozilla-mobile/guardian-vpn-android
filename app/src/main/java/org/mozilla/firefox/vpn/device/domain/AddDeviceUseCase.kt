package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.UserStateResolver
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.findAvailableModelName

class AddDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val userStateResolver: UserStateResolver
) {

    suspend operator fun invoke(token: String): Result<DeviceInfo> {
        val userInfo = userRepository.getUserInfo() ?: return Result.Fail(UnauthorizedException())
        val devices = userInfo.user.devices
        val deviceName = findAvailableModelName(devices)

        return deviceRepository.addDevice(deviceName, "Bearer $token").apply {
            userRepository.refreshUserInfo()
            userStateResolver.refresh()
        }
    }
}
