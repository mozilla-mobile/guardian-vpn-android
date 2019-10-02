package org.mozilla.guardian.user.domain

import android.util.Log
import kotlinx.coroutines.delay
import org.mozilla.guardian.user.data.LoginInfo
import org.mozilla.guardian.user.data.LoginResult
import org.mozilla.guardian.user.data.Result
import org.mozilla.guardian.user.data.UserRepository
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class VerifyLoginUseCase(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(info: LoginInfo): Result<LoginResult, String> {
        var result = userRepository.verifyLogin(info)

        val utcTimeZone = TimeZone.getTimeZone("UTC")
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = utcTimeZone
        }

        val expiresDate = try {
            format.parse(info.expiresOn)
        } catch (e: ParseException) {
            return Result.Fail("illegal expiredOn format")
        }

        while (result !is Result.Success) {
            Log.d(TAG, "verify login fail, result=$result")

            delay(info.pollInterval * 1000L)

            val currentDate = Calendar.getInstance(utcTimeZone)
            if (currentDate.after(expiresDate)) {
                return Result.Fail("expired, expire=$expiresDate, current=$currentDate")
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
