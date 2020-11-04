package org.mozilla.firefox.vpn.service

import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.mozilla.firefox.vpn.AuthCode
import org.mozilla.firefox.vpn.BuildConfig
import org.mozilla.firefox.vpn.const.ENDPOINT
import org.mozilla.firefox.vpn.crypto.CodeChallenge
import org.mozilla.firefox.vpn.crypto.CodeVerifier
import org.mozilla.firefox.vpn.user.data.AuthToken
import org.mozilla.firefox.vpn.user.data.SessionManager
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.mapError
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GuardianService {

    @POST("/api/v2/vpn/login/verify/")
    suspend fun verifyLogin(@Body postData: PostData): Response<LoginResult>

    data class PostData(
        @SerializedName("code") val code: AuthCode,
        @SerializedName("code_verifier") val codeVerifier: CodeVerifier
    )

    @GET("api/v1/vpn/account")
    suspend fun getUserInfo(
        @Header(TimeoutInterceptor.CONNECT_TIMEOUT) connectTimeout: String,
        @Header(TimeoutInterceptor.READ_TIMEOUT) readTimeout: String
    ): Response<User>

    @GET("api/v1/vpn/servers")
    suspend fun getServers(): Response<ServerList>

    @GET("api/v1/vpn/versions")
    suspend fun getVersions(): Response<Versions>

    @POST("api/v1/vpn/device")
    suspend fun addDevice(@Body body: DeviceRequestBody): Response<DeviceInfo>

    @DELETE("api/v1/vpn/device/{pubkey}")
    suspend fun removeDevice(@Path("pubkey") pubkey: String): Response<Unit>

    companion object {
        const val HOST_GUARDIAN = ENDPOINT
        const val HOST_FXA = "$HOST_GUARDIAN/r/vpn/account"
        const val HOST_FEEDBACK = "$HOST_GUARDIAN/r/vpn/client/feedback"
        const val HOST_SUPPORT = "$HOST_GUARDIAN/r/vpn/support"
        const val HOST_CONTACT = "$HOST_GUARDIAN/r/vpn/contact"
        const val HOST_TERMS = "$HOST_GUARDIAN/r/vpn/terms"
        const val HOST_PRIVACY = "$HOST_GUARDIAN/r/vpn/privacy"

        fun getLoginUrl(
            codeChallenge: CodeChallenge,
            challengeMethod: String = "S256"
        ) = "$HOST_GUARDIAN/api/v2/vpn/login/android?" +
                "code_challenge=$codeChallenge&code_challenge_method=$challengeMethod"
    }
}

fun GuardianService.Companion.newInstance(sessionManager: SessionManager): GuardianService {
    val logLevel = if (BuildConfig.DEBUG) {
        // Warning: this will log headers and full body. Do not use for production builds.
        HttpLoggingInterceptor.Level.BODY
    } else {
        // Logs request URL and response code only.
        HttpLoggingInterceptor.Level.BASIC
    }
    val client = OkHttpClient.Builder()
        .addInterceptor {
            val original = it.request()
            val request = original.newBuilder()
                .header("User-Agent", getUserAgent())
                .method(original.method(), original.body())
            if (original.isHttps) {
                request.addHeader("Authorization", "Bearer ${sessionManager.getAuthToken()}")
            }
            it.proceed(request.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = logLevel
        })
        .addInterceptor(TimeoutInterceptor())
        .connectionPool(ConnectionPool(0, 1, TimeUnit.MILLISECONDS))
        .build()

    val gson = GsonBuilder()
        .registerTypeAdapter(Versions::class.java, VersionsDeserializer())
        .create()

    return Retrofit.Builder()
        .baseUrl(HOST_GUARDIAN)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()
        .create(GuardianService::class.java)
}

suspend fun GuardianService.getUserInfo(
    connectTimeout: Long = 0,
    readTimeout: Long = 0
) = getUserInfo(connectTimeout.toString(), readTimeout.toString())

private class TimeoutInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()

        var connectTimeout = chain.connectTimeoutMillis()
        var readTimeout = chain.readTimeoutMillis()
        var writeTimeout = chain.writeTimeoutMillis()

        val connectNew = request.header(CONNECT_TIMEOUT)
        val readNew = request.header(READ_TIMEOUT)
        val writeNew = request.header(WRITE_TIMEOUT)

        if (!connectNew.isNullOrEmpty()) {
            connectTimeout = Integer.valueOf(connectNew)
        }
        if (!readNew.isNullOrEmpty()) {
            readTimeout = Integer.valueOf(readNew)
        }
        if (!writeNew.isNullOrEmpty()) {
            writeTimeout = Integer.valueOf(writeNew)
        }

        val builder = request.newBuilder()
        builder.removeHeader(CONNECT_TIMEOUT)
        builder.removeHeader(READ_TIMEOUT)
        builder.removeHeader(WRITE_TIMEOUT)

        return chain
            .withConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .withWriteTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            .proceed(builder.build())
    }

    companion object {
        const val CONNECT_TIMEOUT = "CONNECT_TIMEOUT"
        const val READ_TIMEOUT = "READ_TIMEOUT"
        const val WRITE_TIMEOUT = "WRITE_TIMEOUT"
    }
}

private fun getUserAgent(): String {
    val os = "Android ${Build.VERSION.RELEASE}"
    val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "no-support-abi"
    return "FirefoxPrivateNetworkVPN/${BuildConfig.VERSION_NAME} ($os; $abi)"
}

data class LoginResult(
    @SerializedName("token")
    val token: AuthToken
)

data class User(
    @SerializedName("email")
    val email: String,

    @SerializedName("display_name")
    val displayName: String,

    @SerializedName("avatar")
    val avatar: String,

    @SerializedName("subscriptions")
    val subscription: Subscription,

    @SerializedName("devices")
    val devices: List<DeviceInfo>,

    @SerializedName("max_devices")
    val maxDevices: Int
)

data class Subscription(
    @SerializedName("vpn")
    val vpn: VpnInfo
)

data class VpnInfo(
    @SerializedName("active")
    val active: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("renews_on")
    val renewsOn: String
)

data class DeviceInfo(
    @SerializedName("name")
    val name: String,

    @SerializedName("pubkey")
    val pubKey: String,

    @SerializedName("ipv4_address")
    val ipv4Address: String,

    @SerializedName("ipv6_address")
    val ipv6Address: String,

    @SerializedName("created_at")
    val createdAt: String
)

data class ServerList(
    @SerializedName("countries")
    val countries: List<Country>
)

data class Country(
    @SerializedName("name")
    val name: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("cities")
    val cities: List<City>
)

data class City(
    @SerializedName("name")
    val name: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("servers")
    val servers: List<Server>
)

data class Server(
    @SerializedName("hostname")
    val hostName: String,

    @SerializedName("ipv4_addr_in")
    val ipv4Address: String,

    @SerializedName("weight")
    val weight: Int,

    @SerializedName("include_in_country")
    val includeInCountry: Boolean,

    @SerializedName("public_key")
    val publicKey: String,

    @SerializedName("port_ranges")
    val portRanges: List<List<Int>>,

    @SerializedName("ipv4_gateway")
    val ipv4Gateway: String,

    @SerializedName("ipv6_gateway")
    val ipv6Gateway: String
)

internal class VersionsDeserializer : JsonDeserializer<Versions> {
    @Throws(JsonParseException::class)
    override fun deserialize(je: JsonElement, type: Type, jdc: JsonDeserializationContext): Versions {
        val gson = Gson()

        return Versions(
            je.asJsonObject.keySet()
                .map { it to gson.fromJson(je.asJsonObject.get(it), PlatformVersion::class.java) }
                .toMap()
        )
    }
}

data class Versions(
    val map: Map<String, PlatformVersion>
)

data class PlatformVersion(
    @SerializedName("latest")
    val latest: Version,

    @SerializedName("minimum")
    val minimum: Version
)

data class Version(
    @SerializedName("version")
    val version: String,

    @SerializedName("released_on")
    val releasedOn: String,

    @SerializedName("message")
    val message: String
)

data class DeviceRequestBody(
    val name: String,
    val pubkey: String
)

data class ErrorBody(
    @SerializedName("code")
    val code: Int,

    @SerializedName("errno")
    val errno: Int,

    @SerializedName("error")
    val error: String
)

inline fun <reified T : Any> Response<T>.resolveBody(): Result<T> {
    return if (this.isSuccessful) {
        body()?.let { Result.Success(it) } ?: Result.Success(Unit as T)
    } else {
        Result.Fail(ErrorCodeException(this.code(), this.errorBody()))
    }
}

fun <T : Any> Result<T>.handleError(code: Int, function: (response: ResponseBody?) -> Exception): Result<T> {
    return this.mapError {
        if (it is ErrorCodeException && it.code == code) {
            function(it.errorBody)
        } else {
            it
        }
    }
}

fun ResponseBody.toErrorBody(): ErrorBody? {
    return try {
        Gson().fromJson(string(), ErrorBody::class.java)
    } catch (e: JsonSyntaxException) {
        null
    }
}

fun ErrorBody.toUnauthorizedError(): UnauthorizedException? {
    return when (errno) {
        120 -> InvalidToken
        121 -> UserNotFound
        122 -> DeviceNotFound
        123 -> NoActiveSubscription
        124 -> LoginTokenNotFound
        125 -> LoginTokenExpired
        126 -> LoginTokenUnverified
        else -> null
    }
}

fun ErrorBody.toDeviceApiError(): DeviceApiError? {
    return when (errno) {
        100 -> MissingPubKey
        101 -> MissingName
        102 -> InvalidPubKey
        103 -> PubKeyUsed
        104 -> KeyLimitReached
        105 -> PubKeyNotFound
        else -> null
    }
}

object EmptyBodyException : RuntimeException()
object IllegalTimeFormatException : RuntimeException()

open class DeviceApiError : RuntimeException()
object MissingPubKey : DeviceApiError()
object MissingName : DeviceApiError()
object InvalidPubKey : DeviceApiError()
object PubKeyUsed : DeviceApiError()
object KeyLimitReached : DeviceApiError()
object PubKeyNotFound : DeviceApiError()

open class UnauthorizedException : RuntimeException()
object InvalidToken : UnauthorizedException()
object UserNotFound : UnauthorizedException()
object DeviceNotFound : UnauthorizedException()
object NoActiveSubscription : UnauthorizedException()
object LoginTokenNotFound : UnauthorizedException()
object LoginTokenExpired : UnauthorizedException()
object LoginTokenUnverified : UnauthorizedException()

object BrowserClosedWithoutLogin : RuntimeException()

data class ExpiredException(val currentTime: String, val expireTime: String) : RuntimeException()
class ErrorCodeException(val code: Int, val errorBody: ResponseBody?) : RuntimeException()

object NetworkException : RuntimeException()

open class UnknownException(msg: String) : RuntimeException(msg)
data class UnknownErrorBody(val body: ResponseBody?) : UnknownException("${body?.string()}")
