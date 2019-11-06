package org.mozilla.firefox.vpn.device.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.device.data.DeviceRepository

class CurrentDeviceUseCase(
    private val deviceRepository: DeviceRepository
) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        deviceRepository.getDevice()
    }
}
