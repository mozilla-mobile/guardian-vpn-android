package org.mozilla.firefox.vpn.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.domain.AddDeviceUseCase
import org.mozilla.firefox.vpn.user.domain.CreateUserUseCase
import org.mozilla.firefox.vpn.user.domain.GetTokenUseCase
import org.mozilla.firefox.vpn.user.domain.SaveAuthTokenUseCase
import org.mozilla.firefox.vpn.util.GLog
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.StringResource
import org.mozilla.firefox.vpn.util.onError

class OnboardingViewModel(
    val bus: Bus,
    private val createUserUseCase: CreateUserUseCase,
    private val addDeviceUseCase: AddDeviceUseCase,
    private val getTokenUseCase: GetTokenUseCase,
    private val saveAuthTokenUseCase: SaveAuthTokenUseCase
) : ViewModel() {

    var isLoggedOut: Boolean = false
        set(value) {
            if (value) {
                bus.showLoggedOutMessage.postValue(StringResource(R.string.onboarding_logged_out))
            }
            field = value
        }

    private var loginJob: Job = Job()

    fun startLoginFlow() {
        // Some edge cases (e.g., rapidly clicking the "get started" button) can cause this
        // method to be repeatedly called.  We cancel any previous attempts here, so that
        // at most one is running at any time.
        runBlocking { loginJob.cancelAndJoin() }
        loginJob = viewModelScope.launch(Dispatchers.IO) {

            when (val token = getTokenUseCase(scope = this)) {
                is Result.Success -> {
                    saveAuthTokenUseCase(token.value)

                    setupNewUser()

                    bus.launchMainPage.postValue(Unit)
                }
                is Result.Fail -> {
                    // TODO this would be a good place to show an error toast, if we had
                    // copy. This at least prevents them from watching an endless progress
                    // bar on failure.
                    bus.closeTabsToOnboarding.postValue(Unit)
                    GLog.d(TAG, "verify login failed: ${token.exception}")
                }
            }
        }
    }

    private suspend fun setupNewUser() = withContext(Dispatchers.IO) {
        // Note: `createUserUseCase` and `AddDeviceUseCase` are temporally coupled
        createUserUseCase()
        addDeviceUseCase()
            .onError { GLog.d(TAG, "add device failed: $it") }
    }

    companion object {
        private const val TAG = "OnboardingViewModel"
    }
}
