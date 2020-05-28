package org.mozilla.firefox.vpn.main.settings

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.isDeviceLimitReached
import org.mozilla.firefox.vpn.main.settings.domain.GetGleanUseCase
import org.mozilla.firefox.vpn.main.settings.domain.SetGleanUseCase
import org.mozilla.firefox.vpn.main.settings.domain.SignOutUseCase
import org.mozilla.firefox.vpn.user.data.UserRepository

class SettingsViewModel(
    userRepository: UserRepository,
    userStates: UserStates,
    private val signOutUseCase: SignOutUseCase,
    private val getGleanUseCase: GetGleanUseCase,
    private val setGleanUseCase: SetGleanUseCase
) : ViewModel() {

    val gotoMainPage = LiveEvent<Unit>()

    val isGleanEnabled = MutableLiveData<Boolean>()

    init {
        viewModelScope.launch(IO) {
            isGleanEnabled.postValue(getGleanUseCase())
        }
    }

    val showDeviceLimitReached: LiveData<Boolean> by lazy {
        Transformations.map(userStates.stateObservable) { it.isDeviceLimitReached() }
    }

    val userInfo by lazy {
        liveData(viewModelScope.coroutineContext + Dispatchers.Main) {
            userRepository.getUserInfo()?.let { emit(it) }
        }
    }

    fun setGleanEnable(enable: Boolean) {
        if (enable != isGleanEnabled.value) {
            setGleanUseCase(enable)
            isGleanEnabled.value = enable
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.Main) {
            signOutUseCase()
            gotoMainPage.value = Unit
        }
    }
}
