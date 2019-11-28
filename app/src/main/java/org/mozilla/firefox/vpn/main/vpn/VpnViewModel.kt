package org.mozilla.firefox.vpn.main.vpn

import android.app.Application
import androidx.lifecycle.*
import com.wireguard.config.Config
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import org.mozilla.firefox.vpn.GuardianApp
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.servers.domain.FilterStrategy
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.servers.domain.SetSelectedServerUseCase
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result
import java.net.InetAddress

class VpnViewModel(
    application: Application,
    private val vpnManager: VpnManager,
    private val deviceRepository: DeviceRepository,
    private val getServersUseCase: GetServersUseCase,
    private val setSelectedServerUseCase: SetSelectedServerUseCase,
    vpnStateProvider: VpnStateProvider
) : AndroidViewModel(application) {

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

    private val config: Config? by lazy {
        prepareConfig()
    }

    private fun prepareConfig(): Config? {
        val currentDevice = deviceRepository.getDevice() ?: return null
        val device = currentDevice.device
        val privateKey = currentDevice.privateKeyBase64
        //val privateKey = KeyPair().privateKey.toBase64()

        val wgInterface = Interface.Builder().apply {
            val ipv4Address = device.ipv4Address
            setKeyPair(KeyPair(Key.fromBase64(privateKey)))
            addAddress(InetNetwork.parse(ipv4Address))
            addDnsServer(InetAddress.getByAddress(byteArrayOf(1, 1, 1, 1)))
            excludeApplication(getApplication<GuardianApp>().packageName)
        }.build()

        return Config.Builder().apply {
            setInterface(wgInterface)

            val peers = ArrayList<Peer>(1)
            peers.add(Peer.Builder().apply {
                setPublicKey(Key.fromBase64("Wy2FhqDJcZU03O/D9IUG/U5BL0PLbF06nvsfgIwrmGk="))
                parseEndpoint("185.232.22.58:32768")
                setPersistentKeepalive(60)
                parseAllowedIPs("0.0.0.0/0")
            }.build())
//            peers.add(Peer.Builder().apply {
//                setPublicKey(Key.fromBase64("Rzh64qPcg8W8klJq0H4EZdVCH7iaPuQ9falc99GTgRA="))
//                parseEndpoint("103.231.88.2:32768")
//                setPersistentKeepalive(60)
//                parseAllowedIPs("0.0.0.0/0")
//            }.build())

            addPeers(peers)
        }.build()
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
        object Switch : Action()
    }
}
