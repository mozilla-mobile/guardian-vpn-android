package org.mozilla.firefox.vpn.main.domain

import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.*
import org.mozilla.firefox.vpn.util.GLog

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
