package org.mozilla.firefox.vpn.apptunneling.ui

import android.content.pm.ApplicationInfo
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
import org.mozilla.firefox.vpn.apptunneling.domain.RemoveExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.SwitchAppTunnelingUseCase
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
    private val switchStateUseCase: SwitchAppTunnelingUseCase
) : ViewModel() {

    private val installedApps = MutableLiveData<List<ApplicationInfo>>()
    private val excludeApps = MutableLiveData<Set<String>>()

    val uiModel = installedApps.combineWith(excludeApps) { packageList, excludeList ->
        return@combineWith AppTunnelingUiModel(packageList, excludeList)
    }

    val vpnState = vpnStateProvider.stateObservable

    val enableState = uiModel.combineWith(vpnState) { _, vpnState ->
        return@combineWith vpnState is VpnState.Disconnected
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

    fun switchAppTunneling(isChecked: Boolean): Job = viewModelScope.launch(Dispatchers.Main.immediate) {
        switchStateUseCase(isChecked)
    }

    private suspend fun loadInstalledApps(includeInternalApps: Boolean = false) {
        installedApps.postValue(getPackagesUseCase(includeInternalApps))
    }

    private suspend fun loadExcludeApps() {
        excludeApps.postValue(getExcludeAppUseCase())
    }

    sealed class UIState(
        val infoDrawableId: Int,
        val infoTextResId: Int
    ) {
        abstract class Normal : UIState(
            R.drawable.ic_information,
            R.string.app_tunneling_switch_info
        )

        object SwitchOnEnabled : Normal()

        object SwitchOffEnabled : Normal()

        abstract class Warning(
            infoTextResId: Int
        ) : UIState(
            R.drawable.ic_error,
            infoTextResId
        )

        object SwitchOnDisabled : Warning(
            R.string.app_tunneling_switch_on_warning
        )

        object SwitchOffDisabled : Warning(
            R.string.app_tunneling_switch_off_warning
        )
    }
}

data class AppTunnelingUiModel(
    val packageList: List<ApplicationInfo>,
    val excludeList: Set<String>
)
