package org.mozilla.firefox.vpn.onboarding

import androidx.lifecycle.MediatorLiveData
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
    val loginInfo = LiveEvent<LoginInfo>()
    val launchMainPage = LiveEvent<Unit>()

    private val loginFlowStarted = LiveEvent<Boolean>()

    private var verificationJob: Job? = null

    val promptLogin = object : MediatorLiveData<String>() {
        private var info: LoginInfo? = null
        private var isStarted = false

        init {
            addSource(loginInfo) {
                info = it
                notifyIfNeeded()
            }

            addSource(loginFlowStarted) {
                isStarted = true
                notifyIfNeeded()
            }
        }

        private fun notifyIfNeeded() {
            info?.takeIf { isStarted }?.let {
                value = it.loginUrl
                verifyLogin(it)
            }
        }
    }

    fun prepareLoginFlow() = viewModelScope.launch(Dispatchers.Main) {
        val info = getLoginInfo()

        info.onSuccess {
            loginInfo.value = it
        }
    }

    fun startLoginFlow() {
        viewModelScope.launch(Dispatchers.Main) { loginFlowStarted.value = true }
    }

    fun cancelLoginFlow() {
        verificationJob?.cancel("cancel verification by cancelLoginFlow()")
    }

    private suspend fun getLoginInfo() = withContext(Dispatchers.IO) {
        loginInfoUseCase()
    }

    private fun verifyLogin(info: LoginInfo) {
        verificationJob = viewModelScope
            .launch(Dispatchers.IO) { processVerifyResult(verifyLoginUseCase(info)) }
            .addCompletionHandler { verificationJob = null }
    }

    private suspend fun processVerifyResult(verifyResult: Result<LoginResult>) {
        when (verifyResult) {
            is Result.Success -> processLoginResult(verifyResult.value)
            is Result.Fail -> toast.postValue(StringResource("${verifyResult.exception}"))
        }
    }

    private suspend fun processLoginResult(
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
