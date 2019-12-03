package org.mozilla.firefox.vpn.util

import kotlin.math.max
import org.mozilla.firefox.vpn.service.DeviceInfo

fun findAvailableModelName(devices: List<DeviceInfo>): String {
    return findAvailableDeviceName(android.os.Build.MODEL, devices.map { it.name })
}

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
