package org.mozilla.firefox.vpn.main.vpn

import android.app.Application
import androidx.lifecycle.*
import com.wireguard.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.domain.*
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.then

class VpnViewModel(
    application: Application,
    private val vpnManager: VpnManager,
    private val getServerConfigUseCase: GetServerConfigUseCase,
    vpnStateProvider: VpnStateProvider,
    selectedServerProvider: SelectedServerProvider,
    private val getServersUseCase: GetServersUseCase,
    private val getSelectedServerUseCase: GetSelectedServerUseCase
) : AndroidViewModel(application) {

    private val initialServer = MutableLiveData<ServerInfo?>()

    val selectedServer = MediatorLiveData<ServerInfo?>().apply {
        var selected: ServerInfo?

        addSource(initialServer) { initServer ->
            initServer ?: return@addSource
            selected = initServer
            config = getServerConfigUseCase(initServer)
            value = initServer

            addSource(selectedServerProvider.observable) { newServer ->
                newServer?.let {
                    if (vpnManager.isConnected() && selected != it) {
                        selected = it
                        config = getServerConfigUseCase(it)
                        switchVpn()
                    }
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
            else -> UIState.Disconnecting
        }
    }

    /* UIState we explicitly want to transit to */
    private val _uiState: MutableLiveData<UIState> = MutableLiveData()

    val uiState: LiveData<UIState> = MediatorLiveData<UIState>().apply {
        addSource(_uiState) { value = it }
        addSource(_vpnState) { value = it }
    }

    private var config: Config? = null

    init {
        viewModelScope.launch(Dispatchers.Main) {
            initialServer.value = initSelectedServer()
        }
    }

    private suspend fun initSelectedServer(): ServerInfo? = withContext(Dispatchers.IO) {
        val result = getServersUseCase(FilterStrategy.ByCity).then { getSelectedServerUseCase(it) }
        when (result) {
            is Result.Success -> result.value
            else -> null
        }
    }

    fun executeAction(action: Action) {
        when (action) {
            is Action.Connect -> {
                if (vpnManager.isGranted()) {
                    connectVpn()
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

    private fun connectVpn() {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            vpnManager.connect(config!!)
        }
    }

    private fun switchVpn() {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            vpnManager.switch(config!!)
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
        object Switching : UIState()
        object Connected : UIState()
        object Disconnected : UIState()
        object RequestPermission : UIState()
    }

    sealed class Action {
        object Connect : Action()
        object Disconnect : Action()
    }
}
