package org.mozilla.firefox.vpn.main.settings.domain

import kotlinx.coroutines.*
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.UserRepository
import kotlin.coroutines.coroutineContext

class SignOutUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val userInfo = userRepository.getUserInfo() ?: return@withContext
        userRepository.removeUserInfo(userInfo)

        removeDeviceAsync("Bearer ${userInfo.token}")
    }

    private suspend fun removeDeviceAsync(token: String) {
        CoroutineScope(coroutineContext + NonCancellable).launch {
            deviceRepository.getDevice()?.let {
                deviceRepository.removeDevice(it.device.pubKey, token)
            }
        }
    }
}
