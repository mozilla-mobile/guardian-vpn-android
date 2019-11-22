package org.mozilla.firefox.vpn.device.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.UserStateResolver
import org.mozilla.firefox.vpn.isDeviceLimitReached
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.TimeFormat
import org.mozilla.firefox.vpn.util.TimeUtil
import org.mozilla.firefox.vpn.util.findAvailableModelName

class GetDevicesUseCase(
    private val userRepository: UserRepository,
    private val userStateResolver: UserStateResolver
) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        userRepository.getUserInfo()?.user?.devices
            ?.let { devices ->
                val sorted = devices.sortedByDescending {
                    TimeUtil.parseOrNull(it.createdAt, TimeFormat.Iso8601)?.time ?: Long.MIN_VALUE
                }.toMutableList()

                if (userStateResolver.resolve().isDeviceLimitReached()) {
                    sorted.add(0, createDummyDevice(devices))
                }

                Result.Success(sorted.toList())
            }
            ?: Result.Fail(UnauthorizedException())
    }

    private fun createDummyDevice(existDevices: List<DeviceInfo>): DeviceInfo {
        val name = findAvailableModelName(existDevices)
        return DeviceInfo(name, "", "", "", "")
    }
}
