package org.mozilla.firefox.vpn.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import org.mozilla.firefox.vpn.onboarding.domain.ClearPendingLoginInfoUseCase
import org.mozilla.firefox.vpn.onboarding.domain.GetPendingLoginInfoUseCase
import org.mozilla.firefox.vpn.onboarding.domain.SetPendingLoginInfoUseCase
import org.mozilla.firefox.vpn.service.LoginInfo
import org.mozilla.firefox.vpn.service.LoginResult
import org.mozilla.firefox.vpn.user.domain.CreateUserUseCase
import org.mozilla.firefox.vpn.user.domain.GetLoginInfoUseCase
import org.mozilla.firefox.vpn.user.domain.VerifyLoginUseCase
import org.mozilla.firefox.vpn.util.GLog
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.StringResource
import org.mozilla.firefox.vpn.util.addCompletionHandler
import org.mozilla.firefox.vpn.util.onError
import org.mozilla.firefox.vpn.util.onSuccess

class OnboardingViewModel(
    private val loginInfoUseCase: GetLoginInfoUseCase,
    private val verifyLoginUseCase: VerifyLoginUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val addDeviceUseCase: AddDeviceUseCase,
    private val setPendingLoginInfoUseCase: SetPendingLoginInfoUseCase,
    private val getPendingLoginInfoUseCase: GetPendingLoginInfoUseCase,
    private val clearPendingLoginInfoUseCase: ClearPendingLoginInfoUseCase
) : ViewModel() {

    val toast = LiveEvent<StringResource>()
    val showLoggedOutMessage = LiveEvent<StringResource>()
    val launchMainPage = LiveEvent<Unit>()
    val promptLogin = LiveEvent<String>()

    private val _uiModel = MutableLiveData<UiModel>()
    val uiModel: LiveData<UiModel> = _uiModel

    private var verificationJob: Job? = null

    var isLoggedOut: Boolean = false
        set(value) {
            if (value) {
                showLoggedOutMessage.postValue(StringResource(R.string.onboarding_logged_out))
            }
            field = value
        }

    fun startLoginFlow() {
        viewModelScope.launch(Dispatchers.Main) { getLoginInfo().onSuccess { login(it) } }
    }

    fun resumeLoginFlow() {
        _uiModel.value = UiModel(true)
        viewModelScope.launch(Dispatchers.Main) {
            getPendingLoginInfoUseCase()
                ?.let { verifyLogin(it) }
                ?: run { _uiModel.value = UiModel(false) }
        }
    }

    private fun cancelLoginFlow() {
        verificationJob?.cancel("verification cancelled")
    }

    private suspend fun getLoginInfo() = withContext(Dispatchers.IO) {
        loginInfoUseCase()
    }

    private suspend fun login(info: LoginInfo) = withContext(Dispatchers.Main) {
        setPendingLoginInfoUseCase(info)
        promptLogin.value = info.loginUrl
        verificationJob = verifyLoginAsync(info).addCompletionHandler { verificationJob = null }
    }

    private suspend fun verifyLogin(info: LoginInfo, retry: Boolean = false) = withContext(Dispatchers.IO) {
        val result = verifyLoginUseCase(info, retry)

        clearPendingLoginInfoUseCase()

        when (result) {
            is Result.Success -> onLoginSuccess(result.value)
            is Result.Fail -> {
                _uiModel.postValue(UiModel(false))
                cancelLoginFlow()
                GLog.d(TAG, "verify login failed: ${result.exception}")
            }
        }
    }

    private suspend fun verifyLoginAsync(info: LoginInfo) = viewModelScope.launch(Dispatchers.IO) {
        verifyLogin(info, true)
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

    data class UiModel(
        val isLoading: Boolean
    )

    companion object {
        private const val TAG = "OnboardingViewModel"
    }
}
