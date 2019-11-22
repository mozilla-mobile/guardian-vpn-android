package org.mozilla.firefox.vpn.device.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.UserStateResolver
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.isDeviceLimitReached
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.findAvailableModelName

class CurrentDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val userStateResolver: UserStateResolver
) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        if (userStateResolver.resolve().isDeviceLimitReached()) {
            val devices = userRepository.getUserInfo()?.user?.devices ?: emptyList()
            CurrentDevice(createDummyDevice(devices), "")
        } else {
            deviceRepository.getDevice()
        }
    }

    private fun createDummyDevice(existDevices: List<DeviceInfo>): DeviceInfo {
        val name = findAvailableModelName(existDevices)
        return DeviceInfo(name, "", "", "", "")
    }
}
