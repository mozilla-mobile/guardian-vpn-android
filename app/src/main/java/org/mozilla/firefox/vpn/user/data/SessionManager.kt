package org.mozilla.firefox.vpn.user.data

import android.content.SharedPreferences
import com.google.gson.Gson
import org.mozilla.firefox.vpn.user.domain.AuthToken

class SessionManager(private val prefs: SharedPreferences) {

    fun saveUserInfo(user: UserInfo) {
        val json = Gson().toJson(user)
        prefs.edit()
            .putString(PREF_USER_INFO, json)
            .apply()
    }

    fun getUserInfo(): UserInfo? {
        return prefs.getString(PREF_USER_INFO, null)?.let {
            Gson().fromJson(it, UserInfo::class.java)
        }
    }

    fun saveAuthToken(token: AuthToken) {
        prefs.edit()
            .putString(PREF_AUTH_TOKEN, token)
            .apply()
    }

    fun getAuthToken(): AuthToken? = prefs.getString(PREF_AUTH_TOKEN, null)

    fun invalidateSession() {
        prefs.edit()
            .remove(PREF_USER_INFO)
            .remove(PREF_AUTH_TOKEN)
            .apply()
    }

    companion object {
        private const val PREF_USER_INFO = "user_info"
        private const val PREF_AUTH_TOKEN = "auth_token"
    }
}
