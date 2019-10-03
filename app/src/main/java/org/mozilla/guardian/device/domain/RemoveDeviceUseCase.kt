package org.mozilla.guardian.device.domain

import org.mozilla.guardian.device.data.DeviceRepository
import org.mozilla.guardian.user.data.Result
import org.mozilla.guardian.user.data.UnauthorizedException
import org.mozilla.guardian.user.data.UserRepository

class RemoveDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(pubKey: String): Result<Unit> {
        val token = userRepository.getToken()?.let {
            "Bearer $it"
        } ?: return Result.Fail(UnauthorizedException)
        return deviceRepository.removeDevice(pubKey, token)
    }
}
