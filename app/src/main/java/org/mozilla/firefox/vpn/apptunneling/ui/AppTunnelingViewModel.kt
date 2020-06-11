package org.mozilla.firefox.vpn.apptunneling.ui

import android.content.pm.ApplicationInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.apptunneling.domain.AddExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetAppTunnelingSwitchStateUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetPackagesUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.RemoveExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.SwitchAppTunnelingUseCase
import org.mozilla.firefox.vpn.util.combineWith

class AppTunnelingViewModel(
    private val getPackagesUseCase: GetPackagesUseCase,
    private val getExcludeAppUseCase: GetExcludeAppUseCase,
    private val addExcludeAppUseCase: AddExcludeAppUseCase,
    private val removeExcludeAppUseCase: RemoveExcludeAppUseCase,
    private val getAppTunnelingSwitchStateUseCase: GetAppTunnelingSwitchStateUseCase,
    private val switchStateUseCase: SwitchAppTunnelingUseCase
) : ViewModel() {

    private val installedApps = MutableLiveData<List<ApplicationInfo>>()
    private val excludeApps = MutableLiveData<Set<String>>()

    val uiModel = installedApps.combineWith(excludeApps) { packageList, excludeList ->
        return@combineWith AppTunnelingUiModel(packageList, excludeList)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadInstalledApps()
            loadExcludeApps()
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

    fun switchAppTunneling(isChecked: Boolean) {
        switchStateUseCase(isChecked)
    }

    private suspend fun loadInstalledApps(includeInternalApps: Boolean = false) {
        installedApps.postValue(getPackagesUseCase(includeInternalApps))
    }

    private suspend fun loadExcludeApps() {
        excludeApps.postValue(getExcludeAppUseCase())
    }
}

data class AppTunnelingUiModel(
    val packageList: List<ApplicationInfo>,
    val excludeList: Set<String>
)
