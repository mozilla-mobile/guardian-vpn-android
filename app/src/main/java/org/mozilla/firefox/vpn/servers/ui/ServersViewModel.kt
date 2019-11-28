package org.mozilla.firefox.vpn.servers.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.domain.FilterStrategy
import org.mozilla.firefox.vpn.servers.domain.GetSelectedServerUseCase
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.servers.domain.SetSelectedServerUseCase
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.onSuccess

class ServersViewModel(
    private val getServersUseCase: GetServersUseCase,
    private val setSelectedServerUseCase: SetSelectedServerUseCase,
    private val getSelectedServerUseCase: GetSelectedServerUseCase
) : ViewModel() {

    val servers by lazy {
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            val result = getServersUseCase(FilterStrategy.ByCity)
            if (result is Result.Success) {
                emit(result.value)
            } else {
                emit(null)
            }
        }
    }

    private val _selectedServer = MutableLiveData<ServerInfo>()
    val selectedServer = _selectedServer

    fun updateSelectedServer() {
        getSelectedServerUseCase(servers.value ?: emptyList())
            .onSuccess { _selectedServer.value = it }
    }

    fun executeAction(action: Action) {
        when (action) {
            is Action.Switch -> switchToServer(action.server)
        }
    }

    private fun switchToServer(server: ServerInfo) {
        setSelectedServerUseCase(FilterStrategy.ByCity, server)
    }

    sealed class Action {
        class Switch(val server: ServerInfo) : Action()
    }
}
