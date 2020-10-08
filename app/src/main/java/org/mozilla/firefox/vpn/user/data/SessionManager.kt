package org.mozilla.firefox.vpn.user.data

import android.content.SharedPreferences
import com.google.gson.Gson
import org.mozilla.firefox.vpn.user.domain.AuthToken

class SessionManager(
    private val prefs: SharedPreferences,
    private val encryptedPrefs: SharedPreferences
) {

    fun saveUserInfo(user: UserInfo) {
        val json = Gson().toJson(user)
        encryptedPrefs.edit()
            .putString(PREF_USER_INFO, json)
            .apply()
    }

    fun getUserInfo(): UserInfo? {
        prefs.getString(PREF_USER_INFO, null)?.let {
            migratePrefs(PREF_USER_INFO, it)
        }

        return encryptedPrefs.getString(PREF_USER_INFO, null)?.let {
            Gson().fromJson(it, UserInfo::class.java)
        }
    }

    fun saveAuthToken(token: AuthToken) {
        encryptedPrefs.edit()
            .putString(PREF_AUTH_TOKEN, token)
            .apply()
    }

    fun getAuthToken(): AuthToken? {
        prefs.getString(PREF_AUTH_TOKEN, null)?.let {
            migratePrefs(PREF_AUTH_TOKEN, it)
        }

        return encryptedPrefs.getString(PREF_AUTH_TOKEN, null)
    }

    fun invalidateSession() {
        encryptedPrefs.edit()
            .remove(PREF_USER_INFO)
            .remove(PREF_AUTH_TOKEN)
            .apply()
    }

    private fun migratePrefs(key: String, value: String) {
        prefs.edit()
            .remove(key)
            .apply()

        encryptedPrefs.edit()
            .putString(key, value)
            .apply()
    }

    companion object {
        private const val PREF_USER_INFO = "user_info"
        private const val PREF_AUTH_TOKEN = "auth_token"
    }
}
