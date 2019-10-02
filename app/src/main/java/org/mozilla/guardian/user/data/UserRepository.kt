package org.mozilla.guardian.user.data

import android.content.Context
import androidx.preference.PreferenceManager

class UserRepository(private val appContext: Context) {

    private val guardianService = GuardianService.newInstance()

    suspend fun getLoginInfo(): LoginInfo {
        return guardianService.getLoginInfo()
    }

    suspend fun verifyLogin(info: LoginInfo): Result<LoginResult> {
        val response = guardianService.verifyLogin(info.verificationUrl)

        return if (response.isSuccessful) {
            response.body()?.let {
                saveToken(it.token)
                Result.Success(it)
            } ?: Result.Fail(UnknownException("empty response body"))
        } else {
            when (val code = response.code()) {
                401 -> Result.Fail(UnauthorizedException)
                else -> Result.Fail(UnknownException("Unknown status code $code"))
            }
        }
    }

    fun getToken(): String? {
        val pref = PreferenceManager.getDefaultSharedPreferences(appContext)
        return pref.getString(PREF_ACCESS_TOKEN, null)
    }

    private fun saveToken(token: String) {
        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
            .putString(PREF_ACCESS_TOKEN, token)
            .apply()
    }

    companion object {
        private const val PREF_ACCESS_TOKEN = "access_token"
    }
}
