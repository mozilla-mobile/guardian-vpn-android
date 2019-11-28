package org.mozilla.firefox.vpn.util

import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

object PingUtil {
    private const val TAG = "PingUtil"

    fun echoPing(hostAddress: String, timeOut: Int = 5000): Boolean {
        val address = try {
            InetAddress.getByName(hostAddress)
        } catch (e: UnknownHostException) {
            GLog.d(TAG, "getByName: $e")
            return false
        }

        try {
            return address.isReachable(timeOut)
        } catch (e: IOException) {
            GLog.d(TAG, "isReachable: $e")
        } catch (e: IllegalArgumentException) {
            GLog.d(TAG, "isReachable: $e")
        }
        return false
    }

    fun systemPing(hostAddress: String): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 $hostAddress")
            return ipProcess.waitFor() == 0
        } catch (e: Exception) {
            GLog.d(TAG, "systemPing: $e")
        }
        return false
    }
}
