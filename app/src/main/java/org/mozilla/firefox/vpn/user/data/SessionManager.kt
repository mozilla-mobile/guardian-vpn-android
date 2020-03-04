package org.mozilla.firefox.vpn.user.data

import android.content.SharedPreferences
import com.google.gson.Gson

class SessionManager(private val prefs: SharedPreferences) {

    fun createUserInfo(user: UserInfo) {
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

    fun removeUserInfo() {
        prefs.edit().remove(PREF_USER_INFO).apply()
    }

    companion object {
        private const val PREF_USER_INFO = "user_info"
    }
}
