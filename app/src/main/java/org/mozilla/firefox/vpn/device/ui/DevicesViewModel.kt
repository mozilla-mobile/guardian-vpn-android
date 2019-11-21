package org.mozilla.firefox.vpn.device.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
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
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.shouldRegisterDevice
import org.mozilla.firefox.vpn.user.domain.GetUserInfoUseCase
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.TimeFormat
import org.mozilla.firefox.vpn.util.TimeUtil
import org.mozilla.firefox.vpn.util.findAvailableModelName

class DevicesViewModel(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val removeDevicesUseCase: RemoveDeviceUseCase,
    private val currentDeviceUseCase: CurrentDeviceUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val addDeviceUseCase: AddDeviceUseCase,
    private val userStates: UserStates
) : ViewModel() {

    val devices = MutableLiveData<DevicesUiModel>()
    val deviceCount = MutableLiveData<Pair<Int, Int>>()
    val isAuthorized = MutableLiveData<Boolean>(true)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            refreshDevices()
        }
    }

    fun deleteDevice(device: DeviceInfo) = viewModelScope.launch(Dispatchers.IO) {
        when (val result = removeDevicesUseCase(device.pubKey)) {
            is Result.Success -> onDeviceDeleted()
            is Result.Fail -> handleFail(result.exception)
        }
    }

    private suspend fun onDeviceDeleted() {
        if (userStates.state.shouldRegisterDevice()) {
            registerNewDevice()
        }
        refreshDevices()
    }

    private suspend fun registerNewDevice() {
        val token = getUserInfoUseCase()?.token ?: return
        addDeviceUseCase(token)
    }

    private suspend fun refreshDevices() = withContext(Dispatchers.Main) {
        when (val result = getDevicesUseCase()) {
            is Result.Success -> notifyDevicesChanged(result.value)
            is Result.Fail -> handleFail(result.exception)
        }

        getUserInfoUseCase()?.let {
            deviceCount.value = it.user.devices.size to it.user.maxDevices
        }
    }

    private suspend fun notifyDevicesChanged(list: List<DeviceInfo>) {
        val sorted = list.sortedByDescending {
            TimeUtil.parseOrNull(it.createdAt, TimeFormat.Iso8601)?.time ?: Long.MIN_VALUE
        }.toMutableList()

        val limitReached = userStates.state.isDeviceLimitReached()

        val current = if (limitReached) {
            val info = createDummyDevice(list)
            sorted.add(0, info)
            CurrentDevice(info, "")
        } else {
            currentDeviceUseCase()
        }

        devices.value = DevicesUiModel(sorted, current, limitReached)
    }

    private fun createDummyDevice(existDevices: List<DeviceInfo>): DeviceInfo {
        val name = findAvailableModelName(existDevices)
        return DeviceInfo(name, "", "", "", "")
    }

    private fun handleFail(exception: Exception) {
        when (exception) {
            is UnauthorizedException -> handleUnauthorizedException()
        }
    }

    private fun handleUnauthorizedException() {
        isAuthorized.value = false
        devices.value = DevicesUiModel(emptyList(), null, false)
    }
}

data class DevicesUiModel(
    val devices: List<DeviceInfo>,
    val currentDevice: CurrentDevice?,
    val isLimitReached: Boolean
)
