/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.user.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.firefox.vpn.WORKING_SDK
import org.mozilla.firefox.vpn.service.Subscription
import org.mozilla.firefox.vpn.service.User
import org.mozilla.firefox.vpn.service.VpnInfo
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [WORKING_SDK])
class DataMigrationTest {
    @Test
    fun `no data on disk`() {
        val testContext: Context = getApplicationContext()
        val prefs = testContext.getSharedPreferences("test", Context.MODE_PRIVATE)
        val sessionManager = SessionManager(prefs)

        assertNull(sessionManager.getAuthToken())
        assertNull(sessionManager.getUserInfo())

        // everything remains the same upon a restart
        val sessionManager2 = SessionManager(prefs)
        assertNull(sessionManager2.getAuthToken())
        assertNull(sessionManager2.getUserInfo())
    }

    @Test
    fun `v1 schema version recorded, but no data`() {
        val testContext: Context = getApplicationContext()
        val prefs = testContext.getSharedPreferences("test", Context.MODE_PRIVATE)
        prefs.edit().putInt(PREF_SCHEMA_VERSION, 1).apply()

        val sessionManager = SessionManager(prefs)

        assertNull(sessionManager.getAuthToken())
        assertNull(sessionManager.getUserInfo())

        // everything remains the same upon a restart
        val sessionManager2 = SessionManager(prefs)
        assertNull(sessionManager2.getAuthToken())
        assertNull(sessionManager2.getUserInfo())
    }

    @Test
    fun `v2 schema version recorded, but no data`() {
        val testContext: Context = getApplicationContext()
        val prefs = testContext.getSharedPreferences("test", Context.MODE_PRIVATE)
        prefs.edit().putInt(PREF_SCHEMA_VERSION, 2).apply()

        val sessionManager = SessionManager(prefs)

        assertNull(sessionManager.getAuthToken())
        assertNull(sessionManager.getUserInfo())

        // everything remains the same upon a restart
        val sessionManager2 = SessionManager(prefs)
        assertNull(sessionManager2.getAuthToken())
        assertNull(sessionManager2.getUserInfo())
    }

    @Test
    fun `v1 on disk`() {
        val testContext: Context = getApplicationContext()
        val prefs = testContext.getSharedPreferences("test", Context.MODE_PRIVATE)
        val v1UserInfo = v1UserInfo()
        prefs.edit().putString(PREF_USER_INFO, Gson().toJson(v1UserInfo)).apply()

        val sessionManager = SessionManager(prefs)

        val expectedUserInfo = UserInfo(user = v1UserInfo.user, latestUpdateTime = v1UserInfo.latestUpdateTime)
        assertEquals(expectedUserInfo, sessionManager.getUserInfo())
        assertEquals(v1UserInfo.token, sessionManager.getAuthToken())

        // everything remains the same upon a restart
        val sessionManager2 = SessionManager(prefs)
        assertEquals(expectedUserInfo, sessionManager2.getUserInfo())
        assertEquals(v1UserInfo.token, sessionManager2.getAuthToken())
    }

    @Test
    fun `v2 on disk`() {
        val testContext: Context = getApplicationContext()
        val prefs = testContext.getSharedPreferences("test", Context.MODE_PRIVATE)
        val v2UserInfo = v2UserInfo()
        val testToken = "someCoolTestToken"
        prefs.edit()
            .putString(PREF_USER_INFO, Gson().toJson(v2UserInfo))
            .putString(PREF_AUTH_TOKEN, testToken)
            .apply()

        val sessionManager = SessionManager(prefs)

        val expectedUserInfo = UserInfo(user = v2UserInfo.user, latestUpdateTime = v2UserInfo.latestUpdateTime)
        assertEquals(expectedUserInfo, sessionManager.getUserInfo())
        assertEquals(testToken, sessionManager.getAuthToken())

        // everything remains the same upon a restart
        val sessionManager2 = SessionManager(prefs)
        assertEquals(expectedUserInfo, sessionManager2.getUserInfo())
        assertEquals(testToken, sessionManager2.getAuthToken())
    }
}

internal fun v1UserInfo(): DataMigration.UserInfoV1 {
    return DataMigration.UserInfoV1(
        user = User(
            email = "test@mozilla.com",
            displayName = "Test User v1",
            avatar = "http://someAvatar.url",
            subscription = Subscription(
                vpn = VpnInfo(
                    active = true,
                    createdAt = "someOldTimestamp",
                    renewsOn = "somewhatNewerTimestamp"
                )
            ),
            devices = listOf(),
            maxDevices = 2
        ),
        token = "testTokenEfhwfu7t8",
        latestUpdateTime = 123456L
    )
}

internal fun v2UserInfo(): DataMigration.UserInfoV2 {
    return DataMigration.UserInfoV2(
        user = User(
            email = "test@mozilla.com",
            displayName = "Test User v2",
            avatar = "http://someAvatar.url",
            subscription = Subscription(
                vpn = VpnInfo(
                    active = true,
                    createdAt = "someOldTimestamp",
                    renewsOn = "somewhatNewerTimestamp"
                )
            ),
            devices = listOf(),
            maxDevices = 2
        ),
        latestUpdateTime = 123456L
    )
}
