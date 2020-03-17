package org.mozilla.firefox.vpn.report

import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.service.LoginInfo
import org.mozilla.firefox.vpn.service.LoginResult
import org.mozilla.firefox.vpn.user.data.UserInfo
import org.mozilla.firefox.vpn.user.data.isDeviceLimitReached
import org.mozilla.firefox.vpn.user.data.isSubscribed
import org.mozilla.firefox.vpn.util.GLog
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.TimeFormat
import org.mozilla.firefox.vpn.util.TimeUtil
import org.mozilla.firefox.vpn.util.onError
import org.mozilla.firefox.vpn.util.onSuccess

inline fun <reified T : Any> Result<T>.doReport(
    tag: String,
    errorMsg: String? = null,
    successMsg: String? = null
): Result<T> {
    return this
        .onSuccess { data ->
            val dataString = "data=(${reportDataType(data)})"
            val msg = successMsg?.let { "msg=$it, $dataString" } ?: dataString
            reportSuccess(tag, msg)
        }
        .onError { e ->
            val errorString = "e=$e"
            val msg = errorMsg?.let { "msg=$it, $errorString" } ?: errorString
            reportError(tag, msg)
        }
}

inline fun <reified T : Any> reportDataType(value: T): String {
    return when (value) {
        is LoginInfo -> value.toReport()
        is LoginResult -> value.toReport()
        is UserInfo -> value.toReport()
        is DeviceInfo -> value.toReport()
        is List<*> -> {
            val type = value.random()?.let { it::class.java.simpleName } ?: "Unknown"
            "List(type=$type, size=${value.size})"
        }
        else -> "${T::class.java.simpleName}, please impl toReport() explicitly"
    }
}

fun reportSuccess(tag: String, msg: String) {
    GLog.report(tag, "success($msg)")
}

fun reportError(tag: String, msg: String) {
    GLog.report(tag, "error($msg)")
}

fun LoginInfo.toReport(): String {
    val time = System.currentTimeMillis()
    val expire = try {
        TimeUtil.parse(this.expiresOn, TimeFormat.Iso8601).time
    } catch (e: Exception) {
        0L
    }
    val expireIn = expire - time
    val interval = this.pollInterval
    return "expireIn=$expireIn, pollInterval=$interval"
}

fun LoginResult.toReport(): String {
    val subscribed = user.subscription.isSubscribed
    val numDevices = user.devices
    return "subscribed=$subscribed, numDevices=${numDevices.size}"
}

fun UserInfo.toReport(): String {
    val subscribed = isSubscribed
    val limitReached = isDeviceLimitReached
    return "subscribed=$subscribed, deviceLimitReached=$limitReached"
}

fun DeviceInfo.toReport(): String {
    return this.pubKey
}
