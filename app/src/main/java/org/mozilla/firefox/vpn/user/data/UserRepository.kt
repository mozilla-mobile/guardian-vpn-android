package org.mozilla.firefox.vpn.user.data

import java.net.UnknownHostException
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.service.LoginInfo
import org.mozilla.firefox.vpn.service.LoginResult
import org.mozilla.firefox.vpn.service.NetworkException
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.service.UnknownErrorBody
import org.mozilla.firefox.vpn.service.UnknownException
import org.mozilla.firefox.vpn.service.User
import org.mozilla.firefox.vpn.service.Versions
import org.mozilla.firefox.vpn.service.handleError
import org.mozilla.firefox.vpn.service.resolveBody
import org.mozilla.firefox.vpn.service.toErrorBody
import org.mozilla.firefox.vpn.service.toUnauthorizedError
import org.mozilla.firefox.vpn.util.GLog
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.TimeFormat
import org.mozilla.firefox.vpn.util.TimeFormatException
import org.mozilla.firefox.vpn.util.TimeUtil
import org.mozilla.firefox.vpn.util.mapValue
import org.mozilla.firefox.vpn.util.onError
import org.mozilla.firefox.vpn.util.onSuccess

class UserRepository(
    private val guardianService: GuardianService,
    private val sessionManager: SessionManager
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
        return try {
            val response = guardianService.verifyLogin(info.verificationUrl)
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
        sessionManager.createUserInfo(user)
    }

    fun getUserInfo(): UserInfo? {
        return sessionManager.getUserInfo()
    }

    fun removeUserInfo() {
        sessionManager.removeUserInfo()
    }

    /**
     * @return Result.Success(user) or Result.Fail(UnauthorizedException|NetworkException|Otherwise)
     */
    suspend fun refreshUserInfo(): Result<UserInfo> {
        val userInfo = getUserInfo() ?: return Result.Fail(UnauthorizedException())

        return try {
            val response = guardianService.getUserInfo()
            response.resolveBody()
                .mapValue {
                    userInfo.copy(
                        user = it,
                        latestUpdateTime = System.currentTimeMillis()
                    ).apply { createUserInfo(this) }
                }
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

    suspend fun getVersions(): Result<Versions> {
        return try {
            val response = guardianService.getVersions()
            response.resolveBody()
        } catch (e: UnknownHostException) {
            Result.Fail(NetworkException)
        } catch (e: java.lang.Exception) {
            Result.Fail(UnknownException("Unknown exception=$e"))
        }
    }
}

data class UserInfo(
    val user: User,
    val token: String,
    val latestUpdateTime: Long
)

val UserInfo.isSubscribed: Boolean
    get() {
        val subscription = this.user.subscription
        val now = TimeUtil.now()
        val renewDate = try {
            TimeUtil.parse(subscription.vpn.renewsOn, TimeFormat.Iso8601)
        } catch (e: TimeFormatException) {
            GLog.e("[isSubscribed] illegal renewDate format: $e")
            return false
        }
        GLog.i("[isSubscribed] current=$now")
        GLog.i("[isSubscribed] renewOn=$renewDate")

        return subscription.vpn.active && now.before(renewDate)
    }

val UserInfo.isDeviceLimitReached: Boolean
    get() {
        return user.devices.size >= user.maxDevices
    }

suspend fun <T : Any> Result<T>.checkAuth(
    authorized: (suspend (value: T) -> Unit)? = null,
    unauthorized: (suspend () -> Unit)? = null,
    onError: (suspend (e: Exception) -> Unit)? = null
) {
    this.onSuccess { authorized?.invoke(it) }
        .onError {
            when (it) {
                is UnauthorizedException -> {
                    unauthorized?.invoke()
                }
                else -> onError?.invoke(it)
            }
        }
}
