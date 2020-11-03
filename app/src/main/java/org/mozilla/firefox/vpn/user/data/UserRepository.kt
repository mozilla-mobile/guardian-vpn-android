package org.mozilla.firefox.vpn.user.data

import java.net.UnknownHostException
import org.mozilla.firefox.vpn.AuthCode
import org.mozilla.firefox.vpn.crypto.CodeVerifier
import org.mozilla.firefox.vpn.report.doReport
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.service.LoginResult
import org.mozilla.firefox.vpn.service.NetworkException
import org.mozilla.firefox.vpn.service.Subscription
import org.mozilla.firefox.vpn.service.UnauthorizedException
import org.mozilla.firefox.vpn.service.UnknownErrorBody
import org.mozilla.firefox.vpn.service.UnknownException
import org.mozilla.firefox.vpn.service.User
import org.mozilla.firefox.vpn.service.Versions
import org.mozilla.firefox.vpn.service.getUserInfo
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

private const val TAG = "UserRepository"

class UserRepository(
    private val guardianService: GuardianService,
    private val sessionManager: SessionManager
) {

    /**
     *
     */
    suspend fun verifyLogin(
        authCode: AuthCode,
        codeVerifier: CodeVerifier
    ): Result<LoginResult> = try {
        val response =
            guardianService.verifyLogin(GuardianService.PostData(authCode, codeVerifier))

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

    fun saveUserInfo(user: UserInfo) = sessionManager.saveUserInfo(user)

    fun getUserInfo(): UserInfo? = sessionManager.getUserInfo()

    /**
     * Remove stored user info and auth token.
     */
    fun invalidateSession() = sessionManager.invalidateSession()

    fun saveAuthToken(token: AuthToken) = sessionManager.saveAuthToken(token)

    /**
     * @return Result.Success(user) or Result.Fail(UnauthorizedException|NetworkException|Otherwise)
     */
    suspend fun refreshUserInfo(connectTimeout: Long = 0, readTimeout: Long = 0): Result<UserInfo> {
        return try {
            val response = guardianService.getUserInfo(connectTimeout, readTimeout)
            response.resolveBody()
                .doReport(tag = TAG)
                .mapValue {
                    UserInfo(
                        user = it,
                        latestUpdateTime = System.currentTimeMillis()
                    ).apply { saveUserInfo(this) }
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

val UserInfo.isSubscribed: Boolean
    get() {
        return this.user.subscription.isSubscribed
    }

val Subscription.isSubscribed: Boolean
    get() {
        val now = TimeUtil.now()
        val renewDate = try {
            TimeUtil.parse(vpn.renewsOn, TimeFormat.Iso8601)
        } catch (e: TimeFormatException) {
            GLog.e("[isSubscribed] illegal renewDate format: $e")
            return false
        }
        GLog.i("[isSubscribed] current=$now")
        GLog.i("[isSubscribed] renewOn=$renewDate")

        return vpn.active && now.before(renewDate)
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
