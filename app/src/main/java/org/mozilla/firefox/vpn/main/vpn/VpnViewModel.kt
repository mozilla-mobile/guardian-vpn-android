package org.mozilla.firefox.vpn.main.vpn

import androidx.lifecycle.*
import com.wireguard.config.Config
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result
import java.net.InetAddress

class VpnViewModel(
    private val vpnManager: VpnManager,
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val getServersUseCase: GetServersUseCase
) : ViewModel() {

    private val _uiState: MutableLiveData<UIState> = MutableLiveData()
    val uiState: LiveData<UIState> = _uiState

    val servers = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        val result = getServersUseCase.invoke(GetServersUseCase.FilterStrategy.ByCountry)
        if (result is Result.Success) {
            emit(result.value.countries)
        } else {
            emit(null)
        }
    }

    private val config: Config? by lazy {
        prepareConfig()
    }

    init {
        if (vpnManager.isConnected()) {
            _uiState.value = UIState.Connected
        } else {
            _uiState.value = UIState.Disconnected
        }
    }

    private fun prepareConfig(): Config? {
        val currentDevice = deviceRepository.getDevice() ?: return null
        val device = currentDevice.device
        val privateKey = currentDevice.privateKeyBase64

        val wgInterface = Interface.Builder().apply {
            val ipv4Address = device.ipv4Address
            setKeyPair(KeyPair(Key.fromBase64(privateKey)))
            addAddress(InetNetwork.parse(ipv4Address))
            addDnsServer(InetAddress.getByAddress(byteArrayOf(1, 1, 1, 1)))
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
            peers.add(Peer.Builder().apply {
                setPublicKey(Key.fromBase64("Rzh64qPcg8W8klJq0H4EZdVCH7iaPuQ9falc99GTgRA="))
                parseEndpoint("103.231.88.2:32768")
                setPersistentKeepalive(60)
                parseAllowedIPs("0.0.0.0/0")
            }.build())

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
        _uiState.value = UIState.Connecting
        vpnManager.connect("aaa", config!!)
        _uiState.value = UIState.Connected
    }

    private fun disconnectVpn() {
        _uiState.value = UIState.Disconnecting
        vpnManager.disconnect()
        _uiState.value = UIState.Disconnected
    }

    sealed class UIState {
        object Connecting : UIState()
        object Disconnecting : UIState()
        object Switching : UIState()
        object Connected : UIState()
        object Disconnected : UIState()
        object RequestPermission: UIState()
    }

    sealed class Action {
        object Connect : Action()
        object Disconnect : Action()
        object Switch : Action()
    }
}
