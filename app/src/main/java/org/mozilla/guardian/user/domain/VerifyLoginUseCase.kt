package org.mozilla.guardian.user.domain

import android.util.Log
import kotlinx.coroutines.delay
import org.mozilla.guardian.user.data.LoginInfo
import org.mozilla.guardian.user.data.LoginResult
import org.mozilla.guardian.user.data.Result
import org.mozilla.guardian.user.data.UserRepository

class VerifyLoginUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(info: LoginInfo): Result<LoginResult, String> {
        var result = userRepository.verifyLogin(info)
        while (result !is Result.Success) {
            Log.d(TAG, "verify login fail, result=$result")

            delay(info.pollInterval * 1000L)
            result = userRepository.verifyLogin(info)
        }
        Log.d(TAG, "verify login success, result=$result")
        return result
    }

    companion object {
        private const val TAG = "VerifyLoginUseCase"
    }
}
