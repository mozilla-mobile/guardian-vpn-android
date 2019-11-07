package org.mozilla.firefox.vpn.device.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result
import kotlin.math.max

class AddDeviceUseCase(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(token: String): Result<DeviceInfo> {
        val userInfo = userRepository.getUserInfo() ?: return Result.Fail(UnauthorizedException())
        val devices = userInfo.user.devices
        val deviceName = findAvailableDeviceName(android.os.Build.MODEL, devices.map { it.name })

        return deviceRepository.addDevice(deviceName, "Bearer $token").apply {
            userRepository.refreshUserInfo()
        }
    }

    companion object {
        fun findAvailableDeviceName(deviceName: String, existNames: List<String>): String {
            var idx = 1
            var maxIdx = idx

            val regex = """$deviceName( \((\d+)\))?""".toRegex()

            existNames.forEach { existName ->
                regex.find(existName)?.let { result ->
                    idx++

                    result.groupValues[2].toIntOrNull()?.let { i ->
                        maxIdx = max(maxIdx, i)
                    }
                }
            }

            return if (idx == 1) {
                deviceName
            } else {
                "$deviceName (${maxIdx + 1})"
            }
        }
    }
}
