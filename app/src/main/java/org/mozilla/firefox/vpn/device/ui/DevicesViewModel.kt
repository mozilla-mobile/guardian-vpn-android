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
import org.mozilla.firefox.vpn.util.Result

class DevicesViewModel(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val removeDevicesUseCase: RemoveDeviceUseCase,
    private val currentDeviceUseCase: CurrentDeviceUseCase
) : ViewModel() {

    val devices: MutableLiveData<DevicesModel> = MutableLiveData()
    val isAuthorized: MutableLiveData<Boolean> = MutableLiveData(true)

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
            is Result.Success -> devices.value = DevicesModel(result.value, current)
            is Result.Fail -> handleFail(result.exception)
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
