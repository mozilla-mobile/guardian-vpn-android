package org.mozilla.firefox.vpn.main.vpn

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.domain.FilterStrategy
import org.mozilla.firefox.vpn.servers.domain.GetSelectedServerUseCase
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.servers.domain.SelectedServerProvider
import org.mozilla.firefox.vpn.util.onSuccess
import org.mozilla.firefox.vpn.util.then

class VpnViewModel(
    application: Application,
    private val vpnManager: VpnManager,
    vpnStateProvider: VpnStateProvider,
    selectedServerProvider: SelectedServerProvider,
    private val getServersUseCase: GetServersUseCase,
    private val getSelectedServerUseCase: GetSelectedServerUseCase,
    private val currentDeviceUseCase: CurrentDeviceUseCase
) : AndroidViewModel(application) {

    private val initialServer = MutableLiveData<ServerInfo>()

    private var currentServer: ServerInfo? = null

    val selectedServer = MediatorLiveData<ServerInfo?>().apply {
        addSource(initialServer) { initServer ->
            currentServer = initServer
            value = initServer

            addSource(selectedServerProvider.observable) { newServer ->
                newServer?.let {
                    onServerSelected(currentServer!!, it)
                    currentServer = it
                    value = it
                }
            }
        }
    }

    val duration by lazy {
        liveData(viewModelScope.coroutineContext + Dispatchers.IO, 0) {
            while (true) {
                emit(vpnManager.getDuration())
                delay(1000)
            }
        }
    }

    /* UIState that triggered by vpn state changed */
    private val _vpnState = vpnStateProvider.stateObservable.map {
        when (it) {
            VpnState.Disconnected -> UIState.Disconnected
            VpnState.Connecting -> UIState.Connecting
            VpnState.Connected -> UIState.Connected
            VpnState.Disconnecting -> UIState.Disconnecting
            is VpnState.Switching -> UIState.Switching(it.oldServer.city.name, it.newServer.city.name)
            else -> UIState.Disconnecting
        }
    }

    /* UIState we explicitly want to transit to */
    private val _uiState: MutableLiveData<UIState> = MutableLiveData()

    val uiState: LiveData<UIState> = MediatorLiveData<UIState>().apply {
        addSource(_uiState) { value = it }
        addSource(_vpnState) { value = it }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getServersUseCase(FilterStrategy.ByCity)
                .then { getSelectedServerUseCase(it) }
                .onSuccess { initialServer.postValue(it) }
        }
    }

    fun executeAction(action: Action) {
        when (action) {
            is Action.Connect -> {
                if (vpnManager.isGranted()) {
                    currentServer?.let { connectVpn(it) }
                } else {
                    requestPermission()
                }
            }
            is Action.Disconnect -> disconnectVpn()
        }
    }

    private fun requestPermission() {
        _uiState.value = UIState.RequestPermission
        _uiState.value = UIState.Disconnected
    }

    private fun onServerSelected(oldServer: ServerInfo, newServer: ServerInfo) {
        if (vpnManager.isConnected() && oldServer != newServer) {
            switchVpn(oldServer, newServer)
        }
    }

    private fun connectVpn(server: ServerInfo) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            currentDeviceUseCase()?.let {
                vpnManager.connect(server, it)
            }
        }
    }

    private fun switchVpn(oldServer: ServerInfo, newServer: ServerInfo) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            currentDeviceUseCase()?.let {
                vpnManager.switch(oldServer, newServer, it)
            }
        }
    }

    private fun disconnectVpn() {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            vpnManager.disconnect()
        }
    }

    sealed class UIState {
        object Connecting : UIState()
        object Disconnecting : UIState()
        class Switching(val from: String, val to: String) : UIState()
        object Connected : UIState()
        object Disconnected : UIState()
        object RequestPermission : UIState()
    }

    sealed class Action {
        object Connect : Action()
        object Disconnect : Action()
    }
}
