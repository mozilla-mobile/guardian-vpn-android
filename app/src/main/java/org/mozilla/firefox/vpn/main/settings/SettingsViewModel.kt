package org.mozilla.firefox.vpn.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.user.data.UserRepository

class SettingsViewModel(
    val deviceRepository: DeviceRepository,
    val userRepository: UserRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Settings Fragment"
    }
    val text: LiveData<String> = _text

    fun signOut() {

    }
}