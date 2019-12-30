package org.mozilla.firefox.vpn.main.vpn

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.BuildConfig
import org.mozilla.firefox.vpn.GuardianApp
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.GetLatestUpdateMessageUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.SetLatestUpdateMessageUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.domain.FilterStrategy
import org.mozilla.firefox.vpn.servers.domain.GetSelectedServerUseCase
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.servers.domain.SelectedServerProvider
import org.mozilla.firefox.vpn.service.Version
import org.mozilla.firefox.vpn.ui.InAppNotificationView
import org.mozilla.firefox.vpn.update.UpdateManager
import org.mozilla.firefox.vpn.user.data.checkAuth
import org.mozilla.firefox.vpn.user.domain.LogoutUseCase
import org.mozilla.firefox.vpn.user.domain.NotifyUserStateUseCase
import org.mozilla.firefox.vpn.user.domain.RefreshUserInfoUseCase
import org.mozilla.firefox.vpn.util.StringResource
import org.mozilla.firefox.vpn.util.onSuccess
import org.mozilla.firefox.vpn.util.then

class VpnViewModel(
    application: Application,
    private val vpnManager: VpnManager,
    vpnStateProvider: VpnStateProvider,
    selectedServerProvider: SelectedServerProvider,
    private val getServersUseCase: GetServersUseCase,
    private val getSelectedServerUseCase: GetSelectedServerUseCase,
    private val currentDeviceUseCase: CurrentDeviceUseCase,
    private val getLatestUpdateMessageUseCase: GetLatestUpdateMessageUseCase,
    private val setLatestUpdateMessageUseCase: SetLatestUpdateMessageUseCase,
    private val refreshUserInfoUseCase: RefreshUserInfoUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val notifyUserStateUseCase: NotifyUserStateUseCase,
    private val updateManager: UpdateManager
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

    val updateAvailable: LiveData<Version?>
        get() = liveData(Dispatchers.IO) {
            updateManager.getLatestUpdate()
                ?.let { latest ->
                    val latestVersion = latest.version.toInt()
                    val shown = getLatestUpdateMessageUseCase() >= latestVersion
                    if (latestVersion > BuildConfig.VERSION_CODE && !shown) {
                        emit(latest)
                    } else {
                        emit(null)
                    }
                }
                ?: emit(null)
        }

    val snackBar = LiveEvent<InAppNotificationView.Config>()

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
                    currentServer?.let { tryConnectVpn(it) }
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

    private fun tryConnectVpn(server: ServerInfo) {
        if (!hasNetwork()) {
            _uiState.postValue(UIState.Disconnected(UIModel.Disconnected()))
            snackBar.value = InAppNotificationView.Config.warning(StringResource(R.string.toast_no_network))
            return
        }

        _uiState.postValue(UIState.Connecting(UIModel.Connecting()))

        viewModelScope.launch(Dispatchers.IO) {
            refreshUserInfoUseCase().checkAuth(
                authorized = { connectVpn(server) },
                unauthorized = { logout() }
            )
        }
    }

    private fun hasNetwork(): Boolean {
        val mgr = getApplication<GuardianApp>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return mgr.getNetworkCapabilities(mgr.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    private suspend fun connectVpn(server: ServerInfo) {
        currentDeviceUseCase()?.let { vpnManager.connect(server, it) }
    }

    private fun logout() {
        logoutUseCase()
        notifyUserStateUseCase()
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

    fun onUpdateMessageDismiss(version: Version) {
        setLatestUpdateMessageUseCase(version)
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
            R.string.hero_text_vpn_on,
            R.string.hero_subtext_secure_protected,
            true
        )
        class Connecting : UIModel(
            Styles.Secure.copy(switchAlpha = 0.5f),
            R.string.hero_text_connecting,
            R.string.hero_subtext_protected_shortly,
            true
        )
        class Disconnected : UIModel(
            Styles.Insecure,
            R.string.hero_text_vpn_off,
            R.string.hero_subtext_turn_on,
            false
        )
        class Disconnecting : UIModel(
            Styles.Insecure.copy(switchAlpha = 0.5f),
            R.string.hero_text_disconnecting,
            R.string.hero_subtext_disconnected_shortly,
            false
        )
        class Switching(val from: String, val to: String) : UIModel(
            Styles.Secure.copy(switchAlpha = 0.5f),
            R.string.hero_text_switching,
            R.string.hero_subtext_server_switch,
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
            R.string.hero_text_vpn_on,
            R.string.hero_subtext_check_connection,
            R.string.hero_subtext_unstable,
            R.color.yellow50
        )
        class NoSignal : WarningState(
            R.string.hero_text_vpn_on,
            R.string.hero_subtext_check_connection,
            R.string.hero_subtext_no_signal,
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
