package org.mozilla.firefox.vpn.servers.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import org.mozilla.firefox.vpn.servers.domain.FilterStrategy
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.util.Result

class ServersViewModel(
    private val getServersUseCase: GetServersUseCase
) : ViewModel() {

    val servers by lazy {
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            val result = getServersUseCase(FilterStrategy.ByCountry)
            if (result is Result.Success) {
                emit(result.value)
            } else {
                emit(null)
            }
        }
    }
}