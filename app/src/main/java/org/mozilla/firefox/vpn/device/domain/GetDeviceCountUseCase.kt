package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.UserStateResolver
import org.mozilla.firefox.vpn.isDeviceLimitReached
import org.mozilla.firefox.vpn.user.data.UserRepository

class GetDeviceCountUseCase(
    private val userRepository: UserRepository,
    private val userStateResolver: UserStateResolver
) {

    operator fun invoke(): Pair<Int, Int> {
        val userInfo = userRepository.getUserInfo() ?: return 0 to 0
        val isLimitReached = userStateResolver.resolve().isDeviceLimitReached()

        val count = userInfo.user.devices.size + if (isLimitReached) { 1 } else { 0 }
        return count to userInfo.user.maxDevices
    }
}
