package org.mozilla.firefox.vpn.apptunneling.ui

import android.content.pm.ApplicationInfo
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.apptunneling.domain.AddExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetAppTunnelingSwitchStateUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetPackagesUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetProtectNewAppsSwitchStateUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetShowSystemAppsSwitchStateUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.RemoveExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.SwitchAppTunnelingUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.SwitchProtectNewAppsUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.SwitchShowSystemAppsUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.util.combineWith

class AppTunnelingViewModel(
    vpnStateProvider: VpnStateProvider,
    private val getPackagesUseCase: GetPackagesUseCase,
    private val getExcludeAppUseCase: GetExcludeAppUseCase,
    private val addExcludeAppUseCase: AddExcludeAppUseCase,
    private val removeExcludeAppUseCase: RemoveExcludeAppUseCase,
    private val getAppTunnelingSwitchStateUseCase: GetAppTunnelingSwitchStateUseCase,
    private val switchAppTunnelingUseCase: SwitchAppTunnelingUseCase,
    private val getShowSystemAppsSwitchStateUseCase: GetShowSystemAppsSwitchStateUseCase,
    private val switchShowSystemAppsUseCase: SwitchShowSystemAppsUseCase,
    private val getProtectNewAppsSwitchStateUseCase: GetProtectNewAppsSwitchStateUseCase,
    private val switchProtectNewAppsUseCase: SwitchProtectNewAppsUseCase
) : ViewModel() {

    private val installedApps = MutableLiveData<List<ApplicationInfo>>()
    private val excludeApps = MutableLiveData<Set<String>>()

    val uiModel: MediatorLiveData<AppTunnelingUiState> =
        installedApps.combineWith(excludeApps) { packageList, excludeList ->
            return@combineWith AppTunnelingUiState.StateLoaded(AppTunnelingUiModel(packageList, excludeList))
        }

    val vpnState = vpnStateProvider.stateObservable

    val enableState = uiModel.combineWith(vpnState) { _, vpnState ->
        return@combineWith vpnState is VpnState.Disconnected
    }

    init {
        uiModel.value = AppTunnelingUiState.StateLoading
        viewModelScope.launch(Dispatchers.IO) {
            loadInstalledApps()
            loadExcludeApps(true)
        }
    }

    fun addExcludeApp(packageName: String): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
        addExcludeAppUseCase(packageName)
        loadExcludeApps()
    }

    fun addExcludeApp(packageNameSet: Set<String>): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
        addExcludeAppUseCase(packageNameSet)
        loadExcludeApps()
    }

    fun removeExcludeApp(packageName: String): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
        removeExcludeAppUseCase(packageName)
        loadExcludeApps()
    }

    fun removeExcludeApp(packageNameSet: Set<String>): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
        removeExcludeAppUseCase(packageNameSet)
        loadExcludeApps()
    }

    fun getAppTunnelingSwitchState(): Boolean {
        return getAppTunnelingSwitchStateUseCase()
    }

    fun switchAppTunneling(isChecked: Boolean): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
        switchAppTunnelingUseCase(isChecked)
    }

    fun getShowSystemAppsSwitchState(): Boolean {
        return getShowSystemAppsSwitchStateUseCase()
    }

    fun switchShowSystemApps(isChecked: Boolean): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
        switchShowSystemAppsUseCase(isChecked)
        loadInstalledApps()
    }

    fun getProtectNewAppsSwitchState(): Boolean {
        return getProtectNewAppsSwitchStateUseCase()
    }

    fun switchProtectNewApps(isChecked: Boolean): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
        switchProtectNewAppsUseCase(isChecked)
    }

    private suspend fun loadInstalledApps() {
        installedApps.postValue(getPackagesUseCase())
    }

    private suspend fun loadExcludeApps(forceUpdate: Boolean = false) {
        excludeApps.postValue(getExcludeAppUseCase(forceUpdate))
    }

    sealed class InfoState(
        val infoDrawableId: Int,
        val infoTextResId: Int
    ) {
        object Normal : InfoState(
            R.drawable.ic_information,
            R.string.app_tunneling_switch_info
        )

        abstract class Warning(
            infoTextResId: Int
        ) : InfoState(
            R.drawable.ic_error,
            infoTextResId
        )

        object SwitchOnWarning : Warning(
            R.string.app_tunneling_switch_on_warning
        )

        object SwitchOffWarning : Warning(
            R.string.app_tunneling_switch_off_warning
        )
    }
}

sealed class AppTunnelingUiState {
    object StateLoading : AppTunnelingUiState()
    data class StateLoaded(val uiModel: AppTunnelingUiModel) : AppTunnelingUiState()
}

data class AppTunnelingUiModel(
    val packageList: List<ApplicationInfo>,
    val excludeList: Set<String>
)
