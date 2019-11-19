package org.mozilla.firefox.vpn.main.settings

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.main.settings.domain.SignOutUseCase
import org.mozilla.firefox.vpn.user.data.UserRepository

class SettingsViewModel(
    userRepository: UserRepository,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    val gotoMainPage = LiveEvent<Unit>()

    val userInfo by lazy {
        liveData(viewModelScope.coroutineContext + Dispatchers.Main) {
            userRepository.getUserInfo()?.let { emit(it) }
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.Main) {
            signOutUseCase()
            gotoMainPage.value = Unit
        }
    }
}