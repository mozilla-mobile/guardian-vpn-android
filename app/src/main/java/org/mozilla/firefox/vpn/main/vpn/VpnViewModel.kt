package org.mozilla.firefox.vpn.main.vpn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.R
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
            VpnState.Disconnected -> UIState.Disconnected(UIModel.Disconnected())
            VpnState.Connecting -> UIState.Connecting(UIModel.Connecting())
            VpnState.Connected -> UIState.Connected(UIModel.Connected())
            VpnState.Disconnecting -> UIState.Disconnecting(UIModel.Disconnecting())
            is VpnState.Switching -> {
                UIState.Switching(UIModel.Switching(it.oldServer.city.name, it.newServer.city.name))
            }
            VpnState.Unstable -> UIState.Unstable(UIModel.Unstable())
            VpnState.NoSignal -> UIState.NoSignal(UIModel.NoSignal())
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
        _uiState.value = UIState.Disconnected(UIModel.Disconnected())
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
        class Connecting(val uiModel: UIModel) : UIState()
        class Disconnecting(val uiModel: UIModel) : UIState()
        class Switching(val uiModel: UIModel) : UIState()
        class Connected(val uiModel: UIModel) : UIState()
        class Disconnected(val uiModel: UIModel) : UIState()
        class Unstable(val uiModel: UIModel) : UIState()
        class NoSignal(val uiModel: UIModel) : UIState()
        object RequestPermission : UIState()
    }

    sealed class UIModel(
        val style: Styles,
        val titleId: Int,
        val descriptionId: Int,
        val switchOn: Boolean
    ) {

        class Connected : UIModel(
            Styles.Secure,
            R.string.vpn_state_online,
            R.string.vpn_state_online_hint,
            true
        )
        class Connecting : UIModel(
            Styles.Secure.copy(switchAlpha = 0.5f),
            R.string.vpn_state_connecting,
            R.string.vpn_state_connecting_hint,
            true
        )
        class Disconnected : UIModel(
            Styles.Insecure,
            R.string.vpn_state_offline,
            R.string.vpn_state_offline_hint,
            false
        )
        class Disconnecting : UIModel(
            Styles.Insecure.copy(switchAlpha = 0.5f),
            R.string.vpn_state_disconnecting,
            R.string.vpn_state_disconnecting_hint,
            false
        )
        class Switching(val from: String, val to: String) : UIModel(
            Styles.Secure.copy(switchAlpha = 0.5f),
            R.string.vpn_state_switching,
            R.string.vpn_state_switching_hint,
            true
        )
        abstract class WarningState(
            titleId: Int,
            descriptionId: Int,
            val stateTextId: Int,
            val stateColorId: Int
        ) : UIModel(
            Styles.Secure,
            titleId,
            descriptionId,
            true
        )
        class Unstable : WarningState(
            R.string.vpn_state_unstable,
            R.string.vpn_state_check_connection,
            R.string.vpn_state_unstable,
            R.color.yellow50
        )
        class NoSignal : WarningState(
            R.string.vpn_state_no_signal,
            R.string.vpn_state_check_connection,
            R.string.vpn_state_no_signal,
            R.color.red50
        )

        data class Styles(
            val bkgColorId: Int,
            val titleColorId: Int,
            val descriptionColorId: Int,
            val bkgElevation: Int,
            var switchAlpha: Float = 1f
        ) {
            companion object {
                val Secure = Styles(
                    R.color.purple90,
                    android.R.color.white,
                    R.color.white80,
                    R.dimen.vpn_panel_elevation_secure
                )

                val Insecure = Styles(
                    android.R.color.transparent,
                    R.color.gray50,
                    R.color.gray40,
                    R.dimen.vpn_panel_elevation_insecure
                )
            }
        }
    }

    sealed class Action {
        object Connect : Action()
        object Disconnect : Action()
    }
}
