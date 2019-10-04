package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.DeviceInfo
import org.mozilla.firefox.vpn.user.data.Result
import org.mozilla.firefox.vpn.user.data.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.UserRepository

class AddDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Result<DeviceInfo> {
        if (deviceRepository.getDevice() != null && deviceRepository.getPrivateKey() != null) {
            return Result.Fail(RuntimeException("already registered"))
        }
        val token = userRepository.getToken()?.let {
            "Bearer $it"
        } ?: return Result.Fail(UnauthorizedException)
        return deviceRepository.addDevice(android.os.Build.MODEL, token)
    }
}
