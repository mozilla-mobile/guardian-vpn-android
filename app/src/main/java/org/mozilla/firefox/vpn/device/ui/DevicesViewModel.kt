package org.mozilla.firefox.vpn.device.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase
import org.mozilla.firefox.vpn.device.domain.GetDevicesUseCase
import org.mozilla.firefox.vpn.device.domain.RemoveDeviceUseCase
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.user.domain.GetUserInfoUseCase
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.TimeFormat
import org.mozilla.firefox.vpn.util.TimeFormatException
import org.mozilla.firefox.vpn.util.TimeUtil

class DevicesViewModel(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val removeDevicesUseCase: RemoveDeviceUseCase,
    private val currentDeviceUseCase: CurrentDeviceUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {

    val devices = MutableLiveData<DevicesModel>()
    val deviceCount = MutableLiveData<Pair<Int, Int>>()
    val isAuthorized = MutableLiveData<Boolean>(true)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            refreshDevices()
        }
    }

    fun deleteDevice(device: DeviceInfo) = viewModelScope.launch(Dispatchers.IO) {
        when (val result = removeDevicesUseCase(device.pubKey)) {
            is Result.Success -> refreshDevices()
            is Result.Fail -> handleFail(result.exception)
        }
    }

    private fun refreshDevices() = viewModelScope.launch(Dispatchers.Main) {
        val current = currentDeviceUseCase()
        when (val result = getDevicesUseCase()) {
            is Result.Success -> {
                val list = result.value.sortedByDescending {
                    TimeUtil.parseOrNull(it.createdAt, TimeFormat.Iso8601)?.time ?: Long.MIN_VALUE
                }
                devices.value = DevicesModel(list, current)
            }
            is Result.Fail -> handleFail(result.exception)
        }

        getUserInfoUseCase()?.let {
            deviceCount.value = it.user.devices.size to it.user.maxDevices
        }
    }

    private fun handleFail(exception: Exception) {
        when (exception) {
            is UnauthorizedException -> {
                isAuthorized.value = false
                devices.value = DevicesModel(emptyList(), null)
            }
        }
    }
}

data class DevicesModel(
    val devices: List<DeviceInfo>,
    val currentDevice: CurrentDevice?
)
