package org.mozilla.firefox.vpn.main.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.*
import org.mozilla.firefox.vpn.util.GLog
import org.mozilla.firefox.vpn.util.TimeFormat
import org.mozilla.firefox.vpn.util.TimeFormatException
import org.mozilla.firefox.vpn.util.TimeUtil

class AppStateUseCase(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository
) {

    operator fun invoke(): AppState {
        val userInfo = userRepository.getUserInfo() ?: return AppState.Login
        GLog.i(TAG, "[invoke] user=$userInfo")

        if (!userInfo.isSubscribed) {
            return AppState.Subscribe
        }

        val currentDevice = deviceRepository.getDevice()?.device
        val isLimitReached = userInfo.isDeviceLimitReached
        if (currentDevice == null) {
            return if (isLimitReached) {
                AppState.DeviceLimitReached
            } else {
                AppState.RegisterDevice
            }
        }

        return AppState.Normal
    }

    companion object {
        private const val TAG = "AppAvailabilityState"
    }
}

sealed class AppState {
    object Login : AppState()
    object Subscribe : AppState()
    object DeviceLimitReached : AppState()
    object RegisterDevice : AppState()
    object Normal : AppState()
}

private val UserInfo.isSubscribed: Boolean
    get() {
        val subscription = this.user.subscription
        val now = TimeUtil.now()
        val renewDate = try {
            TimeUtil.parse(subscription.vpn.renewsOn, TimeFormat.Iso8601)
        } catch (e: TimeFormatException) {
            GLog.e("[isSubscribed] illegal renewDate format: $e")
            return false
        }
        GLog.i("[isSubscribed] current=$now")
        GLog.i("[isSubscribed] renewOn=$renewDate")

        return subscription.vpn.active && now.before(renewDate)
    }


private val UserInfo.isDeviceLimitReached: Boolean
    get() {
        return user.devices.size >= user.maxDevices
    }
