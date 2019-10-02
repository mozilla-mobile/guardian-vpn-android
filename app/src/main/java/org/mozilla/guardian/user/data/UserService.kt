package org.mozilla.guardian.user.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface UserService {
    @POST("api/v1/vpn/login")
    suspend fun getLoginInfo(): LoginInfo

    @GET
    suspend fun verifyLogin(@Url verifyUrl: String): Response<LoginResult>
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

sealed class Result<out T : Any, out E : Any> {
    data class Success<out T : Any>(val result: T) : Result<T, Nothing>()
    data class Fail<out E : Any>(val message: E) : Result<Nothing, E>()
}

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
