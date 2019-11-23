package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.onSuccess
import org.mozilla.firefox.vpn.util.then

class RemoveDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(pubKey: String): Result<Unit> {
        return userRepository.refreshUserInfo()
            .then { deviceRepository.unregisterDevice(pubKey, "Bearer ${it.token}") }
            .onSuccess { userRepository.refreshUserInfo() }
    }
}
