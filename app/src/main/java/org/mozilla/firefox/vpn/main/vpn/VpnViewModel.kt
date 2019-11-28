package org.mozilla.firefox.vpn.main.vpn

import android.app.Application
import androidx.lifecycle.*
import com.wireguard.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.domain.GetServerConfigUseCase
import org.mozilla.firefox.vpn.servers.domain.SelectedServerProvider

class VpnViewModel(
    application: Application,
    private val vpnManager: VpnManager,
    private val getServerConfigUseCase: GetServerConfigUseCase,
    vpnStateProvider: VpnStateProvider,
    selectedServerProvider: SelectedServerProvider
) : AndroidViewModel(application) {

    private val initialServer = MutableLiveData<ServerInfo>()

    val selectedServer = MediatorLiveData<ServerInfo>().apply {
        var selected: ServerInfo? = null

        addSource(initialServer) {
            selected = it
            config = getServerConfigUseCase(it)
            value = it
        }

        addSource(selectedServerProvider.observable) {
            val info = it ?: return@addSource
            if (vpnManager.isConnected() && selected != info) {
                selected = info
                config = getServerConfigUseCase(info)
                switchVpn()
            }
            value = info
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
        initialServer.value = selectedServerProvider.selectedServer
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
