package org.mozilla.firefox.vpn.device.ui

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.device.domain.AddDeviceUseCase
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase
import org.mozilla.firefox.vpn.device.domain.GetDevicesUseCase
import org.mozilla.firefox.vpn.device.domain.RemoveDeviceUseCase
import org.mozilla.firefox.vpn.isDeviceLimitReached
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.shouldRegisterDevice
import org.mozilla.firefox.vpn.user.data.checkAuth
import org.mozilla.firefox.vpn.user.domain.LogoutUseCase
import org.mozilla.firefox.vpn.user.domain.NotifyUserStateUseCase
import org.mozilla.firefox.vpn.user.domain.RefreshUserInfoUseCase
import org.mozilla.firefox.vpn.util.Result

class DevicesViewModel(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val removeDevicesUseCase: RemoveDeviceUseCase,
    private val currentDeviceUseCase: CurrentDeviceUseCase,
    private val addDeviceUseCase: AddDeviceUseCase,
    private val userStates: UserStates,
    private val logoutUseCase: LogoutUseCase,
    private val notifyUserStateUseCase: NotifyUserStateUseCase,
    private val refreshUserInfoUseCase: RefreshUserInfoUseCase
) : ViewModel() {

    private val refreshExplicitly = MutableLiveData<DevicesUiModel>()
    private val refreshPeriodically = liveData(Dispatchers.IO, 0L) {
        while (true) {
            refreshUserInfoUseCase().checkAuth(
                authorized = {
                    emit(buildDevicesUiModel())
                },
                unauthorized = {
                    logoutUseCase()
                    notifyUserStateUseCase()
                }
            )
            delay(POLL_INTERVAL)
        }
    }

    val devicesUiModel = MediatorLiveData<DevicesUiModel>().apply {
        addSource(refreshExplicitly) { value = it }
        addSource(refreshPeriodically) { value = it }
    }

    fun deleteDevice(device: DeviceInfo) = viewModelScope.launch(Dispatchers.IO) {
        removeDevicesUseCase(device.pubKey).checkAuth(
            unauthorized = {
                logoutUseCase()
            }
        )
        notifyUserStateUseCase()

        if (userStates.state.shouldRegisterDevice()) {
            registerNewDevice()
        }

        refreshDevices()
    }

    private suspend fun registerNewDevice() {
        addDeviceUseCase().checkAuth(
            unauthorized = {
                logoutUseCase()
            }
        )
        notifyUserStateUseCase()
    }

    private suspend fun refreshDevices() = withContext(Dispatchers.Main) {
        refreshExplicitly.value = buildDevicesUiModel()
    }

    private suspend fun buildDevicesUiModel(): DevicesUiModel {
        val devices = when (val result = getDevicesUseCase()) {
            is Result.Success -> result.value
            is Result.Fail -> emptyList()
        }

        val current = currentDeviceUseCase()
        val limitReached = userStates.state.isDeviceLimitReached()

        val maxDevices = when (val result = refreshUserInfoUseCase()) {
            is Result.Success -> result.value.user.maxDevices
            is Result.Fail -> 0
        }

        return DevicesUiModel(devices, current, limitReached, maxDevices)
    }

    companion object {
        private const val POLL_INTERVAL = 2000L
    }
}

data class DevicesUiModel(
    val devices: List<DeviceInfo>,
    val currentDevice: CurrentDevice?,
    val isLimitReached: Boolean,
    val maxDevices: Int
)
