package org.mozilla.firefox.vpn.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            is Result.Fail -> toast.postValue(StringResource("${result.exception}"))
        }
    }

    private suspend fun onLoginSuccess(
        loginResult: LoginResult
    ) = withContext(Dispatchers.IO) {

        val user = createUserUseCase(loginResult)

        addDeviceUseCase(user.token)
            .onError { toast.postValue(StringResource("add device failed: $it")) }

        withContext(Dispatchers.Main) {
            launchMainPage.value = Unit
        }
    }
}
