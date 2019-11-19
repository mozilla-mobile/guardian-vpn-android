package org.mozilla.firefox.vpn.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.main.settings.domain.SignOutUseCase
import org.mozilla.firefox.vpn.user.data.UserInfo
import org.mozilla.firefox.vpn.user.data.UserRepository

class SettingsViewModel(
    userRepository: UserRepository,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Settings Fragment"
    }
    val text: LiveData<String> = _text

    private val _showUserInfo = MutableLiveData<UserInfo>()
    val showUserInfo: LiveData<UserInfo> = _showUserInfo

    val gotoMainPage = LiveEvent<Unit>()

    init {
        userRepository.getUserInfo()?.let { _showUserInfo.value = it }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.Main) {
            signOutUseCase()
            gotoMainPage.value = Unit
        }
    }
}