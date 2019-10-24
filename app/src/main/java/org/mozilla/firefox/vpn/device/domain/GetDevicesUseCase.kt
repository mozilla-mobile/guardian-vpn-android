package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result

class GetDevicesUseCase(
    private val userRepository: UserRepository
) {

    operator fun invoke(): Result<List<DeviceInfo>> {
        return userRepository.getUserInfo()?.user?.devices?.let {
            Result.Success(it)
        } ?: Result.Fail(UnauthorizedException())
    }
}
