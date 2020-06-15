package org.mozilla.firefox.vpn.main.vpn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.wireguard.config.Config
import kotlinx.coroutines.delay
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.util.distinctBy

class MockVpnManager : VpnManager {
    private var state: VpnState = VpnState.Disconnected
    private var connectedTime = 0L

    private val _stateObservable = MutableLiveData<VpnState>()

    override val stateObservable: LiveData<VpnState> = _stateObservable
        .map { it }
        .distinctBy { v1, v2 -> v1 != v2 }

    override fun getState(): VpnState {
        return state
    }

    override fun isGranted(): Boolean {
        return true
    }

    override suspend fun connect(server: ServerInfo, serverConfig: Config) {
        transfer(VpnState.Connecting)
        transfer(VpnState.Connected, 1500)
        connectedTime = System.currentTimeMillis()
        // transfer(VpnState.NoSignal, 1500)
    }

    override suspend fun switch(
        oldServer: ServerInfo,
        newServer: ServerInfo,
        serverConfig: Config
    ) {
        transfer(VpnState.Switching(oldServer, newServer))
        transfer(VpnState.Connected, 1000)
    }

    override suspend fun disconnect() {
        transfer(VpnState.Disconnecting)
        transfer(VpnState.Disconnected, 1000)
        connectedTime = 0
    }

    override suspend fun shutdownConnection() {
        transfer(VpnState.Disconnected)
        connectedTime = 0
    }

    override fun isConnected(): Boolean {
        return state == VpnState.Connected || state == VpnState.Unstable || state == VpnState.NoSignal
    }

    override fun getDuration(): Long {
        if (connectedTime == 0L) {
            return 0
        }
        return System.currentTimeMillis() - connectedTime
    }

    private suspend fun transfer(newState: VpnState, delayMillis: Long = 0) {
        delay(delayMillis)
        state = newState
        _stateObservable.postValue(newState)
    }
}
