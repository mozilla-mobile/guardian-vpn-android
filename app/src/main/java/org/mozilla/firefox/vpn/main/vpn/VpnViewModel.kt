package org.mozilla.firefox.vpn.main.vpn

import android.app.Application
import androidx.lifecycle.*
import com.wireguard.config.Config
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.servers.domain.*

class VpnViewModel(
    application: Application,
    private val vpnManager: VpnManager,
    private val getServerConfigUseCase: GetServerConfigUseCase,
    vpnStateProvider: VpnStateProvider,
    selectedServerProvider: SelectedServerProvider
) : AndroidViewModel(application) {

    val selectedServer = selectedServerProvider.observable.map {
        it?.let {
            config = getServerConfigUseCase(it)
            if (vpnManager.isConnected()) {
                getServerConfigUseCase(it)?.apply {
                    connectVpn()
                }
            }
            it
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
        selectedServerProvider.notifyServerChanged()
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
        vpnManager.connect(config!!)
    }

    private fun disconnectVpn() {
        vpnManager.disconnect()
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
