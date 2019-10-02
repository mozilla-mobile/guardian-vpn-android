package org.mozilla.guardian.main.vpn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VpnViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Vpn Fragment"
    }
    val text: LiveData<String> = _text
}