package org.mozilla.firefox.vpn.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.domain.AddDeviceUseCase
import org.mozilla.firefox.vpn.service.LoginInfo
import org.mozilla.firefox.vpn.service.LoginResult
import org.mozilla.firefox.vpn.user.domain.CreateUserUseCase
import org.mozilla.firefox.vpn.user.domain.GetLoginInfoUseCase
import org.mozilla.firefox.vpn.user.domain.VerifyLoginUseCase
import org.mozilla.firefox.vpn.util.*

class OnboardingViewModel(
    private val loginInfoUseCase: GetLoginInfoUseCase,
    private val verifyLoginUseCase: VerifyLoginUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val addDeviceUseCase: AddDeviceUseCase
) : ViewModel() {

    val toast = LiveEvent<StringResource>()
    val launchMainPage = LiveEvent<Unit>()
    val promptLogin = LiveEvent<String>()

    private var verificationJob: Job? = null

    var isLoggedOut: Boolean = false
        set(value) {
            if (value) {
                toast.postValue(StringResource(R.string.onboarding_logged_out))
            }
            field = value
        }

    fun startLoginFlow() {
        viewModelScope.launch(Dispatchers.Main) { getLoginInfo().onSuccess { login(it) } }
    }

    fun cancelLoginFlow() {
        verificationJob?.cancel("verification cancelled")
    }

    private suspend fun getLoginInfo() = withContext(Dispatchers.IO) {
        loginInfoUseCase()
    }

    private suspend fun login(info: LoginInfo) = withContext(Dispatchers.Main) {
        promptLogin.value = info.loginUrl
        verificationJob = verifyLogin(info).addCompletionHandler { verificationJob = null }
    }

    private suspend fun verifyLogin(info: LoginInfo) = viewModelScope.launch(Dispatchers.IO) {
        when (val result = verifyLoginUseCase(info)) {
            is Result.Success -> onLoginSuccess(result.value)
            is Result.Fail -> GLog.d(TAG, "verify login failed: ${result.exception}")
        }
    }

    private suspend fun onLoginSuccess(
        loginResult: LoginResult
    ) = withContext(Dispatchers.IO) {

        createUserUseCase(loginResult)

        addDeviceUseCase()
            .onError { GLog.d(TAG, "add device failed: $it") }

        withContext(Dispatchers.Main) {
            launchMainPage.value = Unit
        }
    }

    companion object {
        private const val TAG = "OnboardingViewModel"
    }
}
