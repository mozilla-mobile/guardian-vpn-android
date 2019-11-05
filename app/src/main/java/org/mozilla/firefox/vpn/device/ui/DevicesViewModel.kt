package org.mozilla.firefox.vpn.device.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.device.domain.GetDevicesUseCase
import org.mozilla.firefox.vpn.device.domain.RemoveDeviceUseCase
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.util.Result

class DevicesViewModel(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val removeDevicesUseCase: RemoveDeviceUseCase
) : ViewModel() {

    val devices: MutableLiveData<List<DeviceInfo>> = MutableLiveData()
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

    private fun refreshDevices() {
        when (val result = getDevicesUseCase()) {
            is Result.Success -> devices.postValue(result.value)
            is Result.Fail -> handleFail(result.exception)
        }
    }

    private fun handleFail(exception: Exception) {
        when (exception) {
            is UnauthorizedException -> {
                isAuthorized.postValue(false)
                devices.postValue(emptyList())
            }
        }
    }
}
