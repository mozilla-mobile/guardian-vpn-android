/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.user.data

import android.content.SharedPreferences
import com.google.gson.Gson
import org.mozilla.firefox.vpn.service.User

const val PREF_SCHEMA_VERSION = "schema_version"
const val PREF_USER_INFO = "user_info"
const val PREF_AUTH_TOKEN = "auth_info"

/**
 * Token that is attached to all requests made by an authenticated user.
 */
typealias AuthToken = String

typealias UserInfo = DataMigration.UserInfoV2

/**
 * Provides basic data migration logic for different version of our schema data classes.
 */
class DataMigration(private val prefs: SharedPreferences) {
    // Current schema version as it exists in the code.
    private val codeVersion = 2
    // What we actually have on disk.
    private val diskVersion = lazy {
        // NB: if we don't have a recorded version, we derive it.
        // This accounts for users for whom schema changed before this class existed.
        prefs.getInt(PREF_SCHEMA_VERSION, deriveCurrentVersion())
    }

    internal data class UserInfoV1(
        val user: User,
        val token: String,
        val latestUpdateTime: Long
    )

    data class UserInfoV2(
        val user: User,
        val latestUpdateTime: Long
    )

    internal fun migrate() {
        val currentVersion = diskVersion.value
        if (currentVersion == codeVersion) {
            return
        }

        // 1->2
        if (currentVersion == 1) {
            migrate1to2()
        } // else if ... -> on new versions, expand the migrations.
    }

    private fun migrate1to2() {
        val userInfoV1 = prefs.getString(PREF_USER_INFO, null)?.let {
            Gson().fromJson(it, UserInfoV1::class.java)

            // No user data, nothing to migrate.
        } ?: return

        val userInfoV2 = UserInfoV2(user = userInfoV1.user, latestUpdateTime = userInfoV1.latestUpdateTime)
        val authToken = userInfoV1.token

        prefs.edit()
            .putString(PREF_USER_INFO, Gson().toJson(userInfoV2))
            .putString(PREF_AUTH_TOKEN, authToken)
            .putInt(PREF_SCHEMA_VERSION, 2)
            .apply()
    }

    /**
     * Note that this functions assumes that starting with v2, we properly migrated user data
     * and recorded a schema version on disk. So, we're just answering a question:
     * "is our disk on v1, or is it on whatever happens to be the current version"?
     */
    private fun deriveCurrentVersion(): Int {
        val rawUserInfo = prefs.getString(PREF_USER_INFO, null)
            // If there's no user data, we're just operating against current schema.
            ?: return codeVersion

        // If there's a 'token' present in user data, assume we're at version 1.
        if (rawUserInfo.contains("token")) {
            return 1
        }

        return codeVersion
    }
}
