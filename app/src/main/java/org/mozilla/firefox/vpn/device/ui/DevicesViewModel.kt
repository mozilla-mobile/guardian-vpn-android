package org.mozilla.firefox.vpn.device.ui

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
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

    private val refreshExplicitly = MutableLiveData<DevicesUiState>()
    private val refreshPeriodically = liveData(Dispatchers.IO, 0L) {
        while (true) {
            refreshUserInfoUseCase().checkAuth(
                authorized = {
                    registerBlocking()
                    emit(DevicesUiState.StateLoaded(buildDevicesUiModel()))
                },
                unauthorized = {
                    logoutUseCase()
                    notifyUserStateUseCase()
                }
            )
            delay(POLL_INTERVAL)
        }
    }

    private val deletingDevices = mutableListOf<DeviceInfo>()

    val devicesUiModel = MediatorLiveData<DevicesUiState>().apply {
        addSource(refreshExplicitly) { value = it }
        addSource(refreshPeriodically) { value = it }
    }

    private val singleThreadDispatcher = Executors.newSingleThreadExecutor()
        .asCoroutineDispatcher()

    init {
        loadDevicesList()
    }

    fun loadDevicesList() {
        refreshExplicitly.value = DevicesUiState.StateLoading
        viewModelScope.launch(Dispatchers.IO) {
            refreshUserInfoUseCase().checkAuth(
                authorized = {
                    registerBlocking()
                    refreshExplicitly.postValue(DevicesUiState.StateLoaded(buildDevicesUiModel()))
                },
                unauthorized = {
                    logoutUseCase()
                    notifyUserStateUseCase()
                }
            )
        }
    }

    fun deleteDevice(device: DeviceInfo) = viewModelScope.launch(Dispatchers.Main.immediate) {
        addDeletingDevice(device)

        withContext(Dispatchers.IO) {
            refreshExplicitly.postValue(DevicesUiState.StateLoaded(buildDevicesUiModel()))

            removeDevicesUseCase(device.pubKey).checkAuth(
                authorized = {
                    removeDeletingDevice(device)
                    registerBlocking()
                    refreshDevices()
                },
                unauthorized = {
                    logoutUseCase()
                }
            )
        }
    }

    private suspend fun registerBlocking() = withContext(singleThreadDispatcher) {
        registerIfNeeded()
    }

    private suspend fun registerIfNeeded() {
        if (userStates.state.shouldRegisterDevice()) {
            registerNewDevice()
        }
    }

    private suspend fun addDeletingDevice(device: DeviceInfo) = withContext(Dispatchers.Main) {
        deletingDevices.add(device)
    }

    private suspend fun removeDeletingDevice(device: DeviceInfo) = withContext(Dispatchers.Main) {
        deletingDevices.remove(device)
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
        refreshExplicitly.value = DevicesUiState.StateLoaded(buildDevicesUiModel())
    }

    private suspend fun buildDevicesUiModel(): DevicesUiModel {
        val devices = when (val result = getDevicesUseCase()) {
            is Result.Success -> result.value
            is Result.Fail -> emptyList()
        }.map {
            DeviceItemUiModel(it, deletingDevices.contains(it))
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
        private const val POLL_INTERVAL = 5000L
    }
}

sealed class DevicesUiState {
    object StateLoading : DevicesUiState()
    data class StateLoaded(val uiModel: DevicesUiModel) : DevicesUiState()
    object StateError : DevicesUiState()
}

data class DevicesUiModel(
    val devices: List<DeviceItemUiModel>,
    val currentDevice: CurrentDevice?,
    val isLimitReached: Boolean,
    val maxDevices: Int
)

data class DeviceItemUiModel(
    val info: DeviceInfo,
    val isLoading: Boolean = false
)
