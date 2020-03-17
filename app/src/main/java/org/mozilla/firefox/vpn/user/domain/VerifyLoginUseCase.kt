package org.mozilla.firefox.vpn.user.domain

import kotlinx.coroutines.delay
import org.mozilla.firefox.vpn.report.doReport
import org.mozilla.firefox.vpn.service.LoginInfo
import org.mozilla.firefox.vpn.service.LoginResult
import org.mozilla.firefox.vpn.service.LoginTokenExpired
import org.mozilla.firefox.vpn.service.LoginTokenNotFound
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result

class VerifyLoginUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(info: LoginInfo, retry: Boolean): Result<LoginResult> {
        var result = verifyLogin(info)
        if (!retry) {
            return result
        }

        while (result is Result.Fail) {
            // Nothing we can do to invalid/expired token. User will have to close the custom tab
            // and click login button again to retrieve new token
            when (result.exception) {
                is LoginTokenNotFound, is LoginTokenExpired -> return result
            }

            delay(info.pollInterval * 1000L)
            result = verifyLogin(info)
        }
        return result
    }

    private suspend fun verifyLogin(info: LoginInfo) = userRepository
        .verifyLogin(info)
        .doReport(tag = TAG)

    companion object {
        private const val TAG = "VerifyLoginUseCase"
    }
}
