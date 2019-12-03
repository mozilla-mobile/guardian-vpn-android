package org.mozilla.firefox.vpn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.user.data.isDeviceLimitReached
import org.mozilla.firefox.vpn.user.data.isSubscribed
import org.mozilla.firefox.vpn.util.GLog

class UserStates(private val resolver: UserStateResolver) {
    val state: UserState
        get() = resolver.resolve()

    val stateObservable = resolver.stateObservable
}

class UserStateResolver(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository
) {
    private val _stateObservable = MutableLiveData<UserState>()
    val stateObservable: LiveData<UserState> = _stateObservable.map { it }

    fun refresh(): UserState {
        return resolve().apply { _stateObservable.postValue(this) }
    }

    fun resolve(): UserState {
        val userInfo = userRepository.getUserInfo() ?: return UserState.Login
        GLog.i(TAG, "[invoke] user=$userInfo")

        if (!userInfo.isSubscribed) {
            return UserState.Subscribe
        }

        val currentDevice = deviceRepository.getDevice()?.device
        val isLimitReached = userInfo.isDeviceLimitReached
        if (currentDevice == null) {
            return if (isLimitReached) {
                UserState.DeviceLimitReached
            } else {
                UserState.RegisterDevice
            }
        }

        return UserState.Normal
    }

    companion object {
        private const val TAG = "UserStateResolver"
    }
}

sealed class UserState {
    object Login : UserState()
    object Subscribe : UserState()
    object DeviceLimitReached : UserState()
    object RegisterDevice : UserState()
    object Normal : UserState()
}

fun UserState.isDeviceLimitReached() = this == UserState.DeviceLimitReached
fun UserState.shouldRegisterDevice() = this == UserState.RegisterDevice
