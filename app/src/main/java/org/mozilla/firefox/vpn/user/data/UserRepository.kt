package org.mozilla.firefox.vpn.user.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson

class UserRepository(private val appContext: Context) {

    private val guardianService = GuardianService.newInstance()

    suspend fun getLoginInfo(): LoginInfo {
        return guardianService.getLoginInfo()
    }

    suspend fun verifyLogin(info: LoginInfo): Result<LoginResult> {
        val response = guardianService.verifyLogin(info.verificationUrl)

        return if (response.isSuccessful) {
            response.body()?.let {
                Result.Success(it)
            } ?: Result.Fail(UnknownException("empty response body"))
        } else {
            when (val code = response.code()) {
                401 -> Result.Fail(UnauthorizedException)
                else -> Result.Fail(UnknownException("Unknown status code $code"))
            }
        }
    }

    fun createUserInfo(user: UserInfo) {
        val json = Gson().toJson(user)
        PreferenceManager.getDefaultSharedPreferences(appContext).edit()
            .putString(PREF_USER_INFO, json)
            .apply()
    }

    fun getUserInfo() : UserInfo? {
        return PreferenceManager.getDefaultSharedPreferences(appContext).getString(PREF_USER_INFO, null)?.let {
            Gson().fromJson(it, UserInfo::class.java)
        }
    }

    suspend fun refreshUserInfo(): Result<User> {
        val userInfo = getUserInfo() ?: return Result.Fail(UnauthorizedException)
        val token = userInfo.token

        val response = guardianService.getUserInfo("Bearer $token")
        return if (response.isSuccessful) {
            response.body()?.let {
                createUserInfo(userInfo.copy(user = it))
                Result.Success(it)
            } ?: Result.Fail(UnknownException("empty response body"))
        } else {
            when (val code = response.code()) {
                401 -> Result.Fail(UnauthorizedException)
                else -> Result.Fail(UnknownException("Unknown status code $code"))
            }
        }
    }

    companion object {
        private const val PREF_USER_INFO = "user_info"
    }
}

data class UserInfo(
    val user: User,
    val token: String,
    val latestUpdateTime: Long
)
