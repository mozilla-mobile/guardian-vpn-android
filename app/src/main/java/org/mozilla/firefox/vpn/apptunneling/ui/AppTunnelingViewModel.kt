package org.mozilla.firefox.vpn.apptunneling.ui

import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import org.mozilla.firefox.vpn.apptunneling.domain.AddExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetExcludeAppUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetPackagesUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.RemoveExcludeAppUseCase

class AppTunnelingViewModel(
    private val getExcludeAppUseCase: GetExcludeAppUseCase,
    private val removeExcludeAppUseCase: RemoveExcludeAppUseCase,
    private val addExcludeAppUseCase: AddExcludeAppUseCase,
    private val getPackagesUseCase: GetPackagesUseCase
) : ViewModel() {

    val uiModel by lazy {
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(AppTunnelingUiModel(getPackagesUseCase(), getExcludeAppUseCase()))
        }
    }
}

data class AppTunnelingUiModel(
    val packageList: List<ApplicationInfo>,
    val excludeList: Set<String>
)
