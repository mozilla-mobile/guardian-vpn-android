package org.mozilla.firefox.vpn.device.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.device.domain.*
import org.mozilla.firefox.vpn.isDeviceLimitReached
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.shouldRegisterDevice
import org.mozilla.firefox.vpn.user.data.checkAuth
import org.mozilla.firefox.vpn.user.domain.LogoutUseCase
import org.mozilla.firefox.vpn.user.domain.GetUserInfoUseCase
import org.mozilla.firefox.vpn.user.domain.NotifyUserStateUseCase
import org.mozilla.firefox.vpn.util.Result

class DevicesViewModel(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val removeDevicesUseCase: RemoveDeviceUseCase,
    private val currentDeviceUseCase: CurrentDeviceUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val addDeviceUseCase: AddDeviceUseCase,
    private val getDeviceCountUseCase: GetDeviceCountUseCase,
    private val userStates: UserStates,
    private val logoutUseCase: LogoutUseCase,
    private val notifyUserStateUseCase: NotifyUserStateUseCase
) : ViewModel() {

    val devices = MutableLiveData<DevicesUiModel>()
    val deviceCount = MutableLiveData<Pair<Int, Int>>()
    val isAuthorized = MutableLiveData<Boolean>(true)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getUserInfoUseCase(true)
            refreshDevices()
        }
    }

    fun deleteDevice(device: DeviceInfo) = viewModelScope.launch(Dispatchers.IO) {
        removeDevicesUseCase(device.pubKey).checkAuth(
            unauthorized = {
                logoutUseCase()
            }
        )
        notifyUserStateUseCase()
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
        if (userStates.state.shouldRegisterDevice()) {
            registerNewDevice()
        }

        when (val result = getDevicesUseCase()) {
            is Result.Success -> notifyDevicesChanged(result.value)
            is Result.Fail -> notifyDevicesChanged(emptyList())
        }

        notifyDeviceCountChanged()
    }

    private suspend fun notifyDevicesChanged(list: List<DeviceInfo>) {
        val current = currentDeviceUseCase()
        val limitReached = userStates.state.isDeviceLimitReached()
        devices.value = DevicesUiModel(list, current, limitReached)
    }

    private fun notifyDeviceCountChanged() {
        deviceCount.value = getDeviceCountUseCase()
    }
}

data class DevicesUiModel(
    val devices: List<DeviceInfo>,
    val currentDevice: CurrentDevice?,
    val isLimitReached: Boolean
)
