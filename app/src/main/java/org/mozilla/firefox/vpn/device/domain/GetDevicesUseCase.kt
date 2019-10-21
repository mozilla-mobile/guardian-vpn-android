package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.user.data.DeviceInfo
import org.mozilla.firefox.vpn.user.data.Result
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.user.data.UnauthorizedException

class GetDevicesUseCase(
    private val userRepository: UserRepository
) {

    operator fun invoke(): Result<List<DeviceInfo>> {
        return userRepository.getUserInfo()?.user?.devices?.let {
            Result.Success(it)
        } ?: Result.Fail(UnauthorizedException)
    }
}
