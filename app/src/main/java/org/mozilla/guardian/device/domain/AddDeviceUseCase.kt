package org.mozilla.guardian.device.domain

import org.mozilla.guardian.device.data.DeviceRepository
import org.mozilla.guardian.user.data.DeviceInfo
import org.mozilla.guardian.user.data.Result
import org.mozilla.guardian.user.data.UnauthorizedException
import org.mozilla.guardian.user.data.UserRepository

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
