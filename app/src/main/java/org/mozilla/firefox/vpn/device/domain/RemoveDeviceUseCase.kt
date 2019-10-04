package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.Result
import org.mozilla.firefox.vpn.user.data.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.UserRepository

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
