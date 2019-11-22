package org.mozilla.firefox.vpn.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.UserState
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.user.data.UserRepository

class SplashViewModel(
    private val userRepository: UserRepository,
    private val userStates: UserStates
) : ViewModel() {

    val showOnboarding = MutableLiveData<Unit>()
    val showMainPage = MutableLiveData<Unit>()

    init {
        when (userStates.state) {
            UserState.Login -> showOnboarding.value = Unit
            else -> enterMainPage()
        }
    }

    private fun enterMainPage() = viewModelScope.launch(Dispatchers.IO) {
        userRepository.refreshUserInfo()
        showMainPage.postValue(Unit)
    }
}
