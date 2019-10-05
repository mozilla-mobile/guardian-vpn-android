package org.mozilla.firefox.vpn.device.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.device.domain.GetDevicesUseCase
import org.mozilla.firefox.vpn.device.domain.RemoveDeviceUseCase
import org.mozilla.firefox.vpn.user.data.DeviceInfo
import org.mozilla.firefox.vpn.user.data.Result
import org.mozilla.firefox.vpn.user.data.UnauthorizedException
import org.mozilla.firefox.vpn.user.data.UserRepository

class DevicesViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository(application)
    private val deviceRepo = DeviceRepository(application)

    private val getDevices = GetDevicesUseCase(userRepo)
    private val removeDevices = RemoveDeviceUseCase(deviceRepo, userRepo)

    val devices: MutableLiveData<List<DeviceInfo>> = MutableLiveData()
    val isAuthorized: MutableLiveData<Boolean> = MutableLiveData(true)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            refreshDevices()
        }
    }

    fun deleteDevice(device: DeviceInfo) = viewModelScope.launch(Dispatchers.IO) {
        when (val result = removeDevices(device.pubKey)) {
            is Result.Success -> refreshDevices()
            is Result.Fail -> handleFail(result.exception)
        }
    }

    private suspend fun refreshDevices() {
        when (val result = getDevices()) {
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
