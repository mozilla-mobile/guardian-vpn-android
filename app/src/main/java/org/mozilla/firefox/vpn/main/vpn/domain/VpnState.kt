package org.mozilla.firefox.vpn.main.vpn.domain

import androidx.lifecycle.LiveData
import org.mozilla.firefox.vpn.main.vpn.VpnManager

interface VpnStateProvider {
    val stateObservable: LiveData<VpnState>
    fun getState(): VpnState
}

class VpnManagerStateProvider(private val vpnManager: VpnManager) : VpnStateProvider by vpnManager

sealed class VpnState {
    object Connecting : VpnState()
    object Connected : VpnState()
    object Disconnecting : VpnState()
    object Disconnected : VpnState()
    object Switching : VpnState()
    object Unstable : VpnState()
    object NoSignal : VpnState()
}
