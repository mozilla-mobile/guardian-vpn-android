package org.mozilla.firefox.vpn.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.mozilla.firefox.vpn.UserState
import org.mozilla.firefox.vpn.UserStates

class SplashViewModel(
    userStates: UserStates
) : ViewModel() {

    val showOnboarding = MutableLiveData<Unit>()
    val showLoggedOutOnboarding = MutableLiveData<Unit>()
    val showMainPage = MutableLiveData<Unit>()

    init {
        val oldState = userStates.state
        when (userStates.state) {
            UserState.Login -> if (oldState != UserState.Login) {
                showLoggedOutOnboarding.value = Unit
            } else {
                showOnboarding.value = Unit
            }
            else -> showMainPage.value = Unit
        }
    }
}
