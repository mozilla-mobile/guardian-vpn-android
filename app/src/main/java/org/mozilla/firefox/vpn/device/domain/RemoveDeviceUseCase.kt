package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result

class RemoveDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(pubKey: String): Result<Unit> {
        val bearer = userRepository.getUserInfo()?.token?.let {
            "Bearer $it"
        } ?: return Result.Fail(UnauthorizedException())
        return deviceRepository.removeDevice(pubKey, bearer).apply {
            userRepository.refreshUserInfo()
        }
    }
}
