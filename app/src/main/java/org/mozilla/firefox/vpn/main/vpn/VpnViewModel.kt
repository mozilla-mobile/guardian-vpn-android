package org.mozilla.firefox.vpn.main.vpn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.servers.data.ServerRepository
import org.mozilla.firefox.vpn.user.data.UserRepository

class VpnViewModel(
    private val vpnManager: VpnManager,
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Click to toggle VPN on/off"
    }
    val text: LiveData<String> = _text
}
