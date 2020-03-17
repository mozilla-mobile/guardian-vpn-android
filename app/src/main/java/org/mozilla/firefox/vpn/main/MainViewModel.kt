package org.mozilla.firefox.vpn.main

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.BuildConfig
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.UserState
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.isDeviceLimitReached
import org.mozilla.firefox.vpn.main.settings.domain.SignOutUseCase
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.user.domain.GetVersionsUseCase
import org.mozilla.firefox.vpn.util.GLog
import org.mozilla.firefox.vpn.util.onSuccess

class MainViewModel(
    private val versionsUseCase: GetVersionsUseCase,
    private val signOutUseCase: SignOutUseCase,
    vpnStateProvider: VpnStateProvider,
    private val userStates: UserStates
) : ViewModel() {

    val showForceUpdate get() = liveData(Dispatchers.IO) {
        versionsUseCase().onSuccess {
            val current = BuildConfig.VERSION_CODE
            val minimum = it.minimum.version.toInt()
            GLog.report(TAG, "version(current=$current, minimum=$minimum)")
            emit(minimum > current)
        }
    }

    private val signOutAction = MutableLiveData<Unit>()

    val launchOnboardingPage = object : MediatorLiveData<LaunchOnboardingConfig>() {
        init {
            addSource(signOutAction) { value = LaunchOnboardingConfig(false) }
            addSource(userStates.stateObservable) {
                if (it is UserState.Login) {
                    value = LaunchOnboardingConfig(true)
                }
            }
        }
    }

    val vpnIcon = vpnStateProvider.stateObservable.map {
        when (it) {
            is VpnState.Switching,
            VpnState.Unstable,
            VpnState.NoSignal,
            VpnState.Disconnecting,
            VpnState.Connected -> R.drawable.ic_vpn_connected
            else -> R.drawable.ic_vpn_disconnected
        }
    }

    val lockSetting = userStates.stateObservable.map {
        it.isDeviceLimitReached()
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.Main) {
            signOutUseCase()
            signOutAction.postValue(Unit)
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}

data class LaunchOnboardingConfig(
    val showLogoutMessage: Boolean
)
