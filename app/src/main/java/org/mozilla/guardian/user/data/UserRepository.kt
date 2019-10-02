package org.mozilla.guardian.user.data

import android.content.Context
import androidx.preference.PreferenceManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRepository(val appContext: Context) {

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
            val loginResult = response.body()!!
            PreferenceManager.getDefaultSharedPreferences(appContext).edit()
                .putString(PREF_ACCESS_TOKEN, loginResult.token)
                .apply()
            Result.Success(loginResult)
        } else {
            when (val code = response.code()) {
                401 -> Result.Fail("Unauthorized")
                else -> Result.Fail("response $code")
            }
        }
    }

    fun getToken(): String? {
        val pref = PreferenceManager.getDefaultSharedPreferences(appContext)
        return pref.getString(PREF_ACCESS_TOKEN, null)
    }

    companion object {
        private const val HOST_GUARDIAN = "https://stage.guardian.nonprod.cloudops.mozgcp.net"

        private const val PREF_ACCESS_TOKEN = "access_token"
    }
}
