package org.mozilla.guardian.user.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface GuardianService {
    @POST("api/v1/vpn/login")
    suspend fun getLoginInfo(): LoginInfo

    @GET
    suspend fun verifyLogin(@Url verifyUrl: String): Response<LoginResult>

    @GET("api/v1/vpn/account")
    suspend fun getUserInfo(@Header("Authorization") token: String): Response<User>

    @GET("api/v1/vpn/servers")
    suspend fun getServers(@Header("Authorization") token: String): Response<ServerList>

    companion object {
        const val HOST_GUARDIAN = "https://stage.guardian.nonprod.cloudops.mozgcp.net"
    }
}

fun GuardianService.Companion.newInstance(): GuardianService {
    val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    return Retrofit.Builder()
            .baseUrl(HOST_GUARDIAN)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GuardianService::class.java)
}

data class LoginInfo(
    @SerializedName("login_url")
    val loginUrl: String,

    @SerializedName("verification_url")
    val verificationUrl: String,

    @SerializedName("expires_on")
    val expiresOn: String,

    @SerializedName("poll_interval")
    val pollInterval: Int
)

data class LoginResult(
    @SerializedName("user")
    val user: User,

    @SerializedName("token")
    val token: String
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

sealed class Result<out T : Any> {
    data class Success<out T : Any>(val value: T) : Result<T>()
    data class Fail(val exception: Exception) : Result<Nothing>()
}

object UnauthorizedException : RuntimeException()
object IllegalTimeFormatException : RuntimeException()
data class ExpiredException(val currentTime: String, val expireTime: String) : RuntimeException()
data class UnknownException(val msg: String) : RuntimeException()
