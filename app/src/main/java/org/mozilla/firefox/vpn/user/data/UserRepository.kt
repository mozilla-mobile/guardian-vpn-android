package org.mozilla.firefox.vpn.user.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import org.mozilla.firefox.vpn.service.*
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.onSuccess

class UserRepository(private val appContext: Context) {

    private val guardianService = GuardianService.newInstance()

    suspend fun getLoginInfo(): Result<LoginInfo> {
        return guardianService.getLoginInfo().resolveBody()
    }

    suspend fun verifyLogin(info: LoginInfo): Result<LoginResult> {
        val response = guardianService.verifyLogin(info.verificationUrl)
        return response.resolveBody()
            .handleError(401) {
                it?.toErrorBody()
                    ?.toUnauthorizedError()
                    ?: UnknownErrorBody(it)
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
        val userInfo = getUserInfo() ?: return Result.Fail(UnauthorizedException())
        val token = userInfo.token

        val response = guardianService.getUserInfo("Bearer $token")
        return response.resolveBody()
            .onSuccess { createUserInfo(userInfo.copy(user = it)) }
            .handleError(401) {
                it?.toErrorBody()
                    ?.toUnauthorizedError()
                    ?: UnknownErrorBody(it)
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
