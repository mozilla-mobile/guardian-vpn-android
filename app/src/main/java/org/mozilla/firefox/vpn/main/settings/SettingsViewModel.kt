package org.mozilla.firefox.vpn.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.main.settings.domain.SignOutUseCase
import org.mozilla.firefox.vpn.user.data.UserRepository

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Settings Fragment"
    }
    val text: LiveData<String> = _text

    val gotoMainPage = LiveEvent<Unit>()

    fun signOut() {
        viewModelScope.launch(Dispatchers.Main) {
            signOutUseCase()
            gotoMainPage.value = Unit
        }
    }
}