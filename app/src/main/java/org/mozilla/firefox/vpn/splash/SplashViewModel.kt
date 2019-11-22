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
    userStates: UserStates
) : ViewModel() {

    val showOnboarding = MutableLiveData<Unit>()
    val showMainPage = MutableLiveData<Unit>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.refreshUserInfo()

            when (userStates.state) {
                UserState.Login -> showOnboarding.postValue(Unit)
                else -> showMainPage.postValue(Unit)
            }
        }
    }
}
