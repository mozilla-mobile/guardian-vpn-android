package org.mozilla.guardian.user.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRepository {

    private val userService = Retrofit.Builder()
            .baseUrl(HOST_GUARDIAN)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserService::class.java)

    suspend fun getLoginInfo(): LoginInfo {
        return userService.getLoginInfo()
    }

    suspend fun verifyLogin(info: LoginInfo): Result<LoginResult, String> {
        val response = userService.verifyLogin(info.verificationUrl)
        return if (response.isSuccessful) {
            Result.Success(response.body()!!)
        } else {
            when (val code = response.code()) {
                401 -> Result.Fail("Unauthorized")
                else -> Result.Fail("response $code")
            }
        }
    }

    companion object {
        private const val HOST_GUARDIAN = "https://stage.guardian.nonprod.cloudops.mozgcp.net"
    }
}
