package org.mozilla.firefox.vpn.user.data

import android.content.SharedPreferences
import com.google.gson.Gson
import org.mozilla.firefox.vpn.service.*
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.onSuccess
import java.net.UnknownHostException

class UserRepository(
    private val guardianService: GuardianService,
    private val prefs: SharedPreferences
) {

    /**
     * @return Result.Success(loginInfo) or Result.Fail(NetworkException|Otherwise)
     */
    suspend fun getLoginInfo(): Result<LoginInfo> {
        return try {
            guardianService.getLoginInfo().resolveBody()
        } catch (e: UnknownHostException) {
            Result.Fail(NetworkException)
        } catch (e: Exception) {
            Result.Fail(UnknownException("Unknown exception=$e"))
        }
    }

    /**
     * @return Result.Success(loginResult) or Result.Fail(UnauthorizedException|NetworkException|Otherwise)
     */
    suspend fun verifyLogin(info: LoginInfo): Result<LoginResult> {
        val response = guardianService.verifyLogin(info.verificationUrl)
        return try {
            response.resolveBody()
                .handleError(401) {
                    it?.toErrorBody()
                        ?.toUnauthorizedError()
                        ?: UnknownErrorBody(it)
                }
        } catch (e: UnknownHostException) {
            Result.Fail(NetworkException)
        } catch (e: Exception) {
            Result.Fail(UnknownException("Unknown exception=$e"))
        }
    }

    fun createUserInfo(user: UserInfo) {
        val json = Gson().toJson(user)
        prefs.edit()
            .putString(PREF_USER_INFO, json)
            .apply()
    }

    fun getUserInfo() : UserInfo? {
        return prefs.getString(PREF_USER_INFO, null)?.let {
            Gson().fromJson(it, UserInfo::class.java)
        }
    }

    /**
     * @return Result.Success(user) or Result.Fail(UnauthorizedException|NetworkException|Otherwise)
     */
    suspend fun refreshUserInfo(): Result<User> {
        val userInfo = getUserInfo() ?: return Result.Fail(UnauthorizedException())
        val token = userInfo.token

        val response = guardianService.getUserInfo("Bearer $token")
        return try {
            response.resolveBody()
                .onSuccess { createUserInfo(userInfo.copy(user = it)) }
                .handleError(401) {
                    it?.toErrorBody()
                        ?.toUnauthorizedError()
                        ?: UnknownErrorBody(it)
                }
        } catch (e: UnknownHostException) {
            Result.Fail(NetworkException)
        } catch (e: Exception) {
            Result.Fail(UnknownException("Unknown exception=$e"))
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
