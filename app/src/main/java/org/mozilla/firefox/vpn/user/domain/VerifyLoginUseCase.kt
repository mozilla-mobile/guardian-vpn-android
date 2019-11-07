package org.mozilla.firefox.vpn.user.domain

import android.util.Log
import kotlinx.coroutines.delay
import org.mozilla.firefox.vpn.service.ExpiredException
import org.mozilla.firefox.vpn.service.IllegalTimeFormatException
import org.mozilla.firefox.vpn.service.LoginInfo
import org.mozilla.firefox.vpn.service.LoginResult
import org.mozilla.firefox.vpn.user.data.*
import org.mozilla.firefox.vpn.util.TimeFormat
import org.mozilla.firefox.vpn.util.TimeFormatException
import org.mozilla.firefox.vpn.util.TimeUtil
import org.mozilla.firefox.vpn.util.Result

class VerifyLoginUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(info: LoginInfo): Result<LoginResult> {
        var result = userRepository.verifyLogin(info)

        val expiresDate = TimeUtil.parseOrNull(info.expiresOn, TimeFormat.Iso8601)
            ?: return Result.Fail(IllegalTimeFormatException)

        while (result !is Result.Success) {
            Log.d(TAG, "verify login fail, result=$result")

            delay(info.pollInterval * 1000L)

            val currentDate = TimeUtil.now()
            if (currentDate.after(expiresDate)) {
                return Result.Fail(ExpiredException(currentDate.toString(), expiresDate.toString()))
            }

            result = userRepository.verifyLogin(info)
        }

        Log.d(TAG, "verify login success, result=$result")
        return result
    }

    companion object {
        private const val TAG = "VerifyLoginUseCase"
    }
}
