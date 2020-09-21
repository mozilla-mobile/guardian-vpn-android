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
import org.mozilla.firefox.vpn.util.GLog

class SignOutUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val vpnManager: VpnManager
) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        GLog.report(TAG, "sign out")
        vpnManager.shutdownConnection()
        removeDeviceAsync()
    }

    private suspend fun removeDeviceAsync() {
        CoroutineScope(coroutineContext + NonCancellable).launch {
            deviceRepository.getDevice()?.let {
                deviceRepository.unregisterDevice(it.device.pubKey)
                userRepository.invalidateSession()
            }
        }
    }

    companion object {
        private const val TAG = "SignOutUseCase"
    }
}
