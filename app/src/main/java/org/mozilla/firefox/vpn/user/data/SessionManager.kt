/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.user.data

import android.content.SharedPreferences
import com.google.gson.Gson

class SessionManager(private val prefs: SharedPreferences) {
    init {
        DataMigration(prefs).migrate()
    }

    fun saveUserInfo(userInfo: UserInfo) {
        val json = Gson().toJson(userInfo)
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
}
