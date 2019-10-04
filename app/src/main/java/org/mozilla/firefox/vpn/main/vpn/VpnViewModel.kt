package org.mozilla.firefox.vpn.main.vpn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VpnViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Click to toggle VPN on/off"
    }
    val text: LiveData<String> = _text
}