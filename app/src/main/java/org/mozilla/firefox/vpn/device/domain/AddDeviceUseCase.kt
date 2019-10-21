package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.DeviceInfo
import org.mozilla.firefox.vpn.user.data.Result

class AddDeviceUseCase(private val deviceRepository: DeviceRepository) {

    suspend operator fun invoke(token: String): Result<DeviceInfo> {
        return deviceRepository.addDevice(android.os.Build.MODEL, "Bearer $token")
    }
}
