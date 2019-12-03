package org.mozilla.firefox.vpn.main.settings.domain

import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.main.vpn.VpnManager
import org.mozilla.firefox.vpn.user.data.UserRepository

class SignOutUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val vpnManager: VpnManager
) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        vpnManager.shutdownConnection()

        val userInfo = userRepository.getUserInfo() ?: return@withContext
        userRepository.removeUserInfo()

        removeDeviceAsync("Bearer ${userInfo.token}")
    }

    private suspend fun removeDeviceAsync(token: String) {
        CoroutineScope(coroutineContext + NonCancellable).launch {
            deviceRepository.getDevice()?.let {
                deviceRepository.unregisterDevice(it.device.pubKey, token)
            }
        }
    }
}
