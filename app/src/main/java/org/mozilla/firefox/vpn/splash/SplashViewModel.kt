package org.mozilla.firefox.vpn.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.UserState
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.user.data.checkAuth
import org.mozilla.firefox.vpn.user.domain.LogoutUseCase
import org.mozilla.firefox.vpn.user.domain.RefreshUserInfoUseCase

class SplashViewModel(
    refreshUserInfoUseCase: RefreshUserInfoUseCase,
    logoutUseCase: LogoutUseCase,
    userStates: UserStates
) : ViewModel() {

    val showOnboarding = MutableLiveData<Unit>()
    val showLoggedOutOnboarding = MutableLiveData<Unit>()
    val showMainPage = MutableLiveData<Unit>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val oldState = userStates.state
            refreshUserInfoUseCase().checkAuth(
                unauthorized = {
                    logoutUseCase()
                }
            )

            when (userStates.state) {
                UserState.Login -> if (oldState != UserState.Login) {
                    showLoggedOutOnboarding.postValue(Unit)
                } else {
                    showOnboarding.postValue(Unit)
                }
                else -> showMainPage.postValue(Unit)
            }
        }
    }
}
