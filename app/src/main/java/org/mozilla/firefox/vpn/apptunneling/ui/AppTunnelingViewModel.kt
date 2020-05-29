package org.mozilla.firefox.vpn.apptunneling.ui

import androidx.lifecycle.ViewModel
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

}