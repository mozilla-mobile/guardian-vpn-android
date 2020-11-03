/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.user.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.firefox.vpn.WORKING_SDK
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [WORKING_SDK])
class SessionManagerTest {
    @Test
    fun `basic operations`() {
        val testContext: Context = ApplicationProvider.getApplicationContext()
        val prefs = testContext.getSharedPreferences("test", Context.MODE_PRIVATE)
        val sessionManager = SessionManager(prefs)
        assertNull(sessionManager.getAuthToken())
        assertNull(sessionManager.getUserInfo())

        // can be called when there's no data
        sessionManager.invalidateSession()

        val userInfo = v2UserInfo()
        sessionManager.saveAuthToken("someCoolToken")
        sessionManager.saveUserInfo(userInfo)

        assertEquals("someCoolToken", sessionManager.getAuthToken())
        assertEquals(userInfo, sessionManager.getUserInfo())

        sessionManager.invalidateSession()
        assertNull(sessionManager.getAuthToken())
        assertNull(sessionManager.getUserInfo())

        // can repopulate data after invalidating
        sessionManager.saveAuthToken("someCoolToken")
        sessionManager.saveUserInfo(userInfo)

        assertEquals("someCoolToken", sessionManager.getAuthToken())
        assertEquals(userInfo, sessionManager.getUserInfo())
    }

    @Test
    fun `basic operations with v1 on disk`() {
        val testContext: Context = ApplicationProvider.getApplicationContext()
        val prefs = testContext.getSharedPreferences("test", Context.MODE_PRIVATE)
        val v1UserInfo = v1UserInfo()
        prefs.edit()
            .putString(PREF_USER_INFO, Gson().toJson(v1UserInfo))
            .apply()

        val sessionManager = SessionManager(prefs)
        val expectedUserInfo = UserInfo(user = v1UserInfo.user, latestUpdateTime = v1UserInfo.latestUpdateTime)
        assertEquals(v1UserInfo.token, sessionManager.getAuthToken())
        assertEquals(expectedUserInfo, sessionManager.getUserInfo())

        sessionManager.invalidateSession()
        assertNull(sessionManager.getAuthToken())
        assertNull(sessionManager.getUserInfo())

        sessionManager.saveAuthToken("anotherToken")
        val newUser = expectedUserInfo.copy(user = expectedUserInfo.user.copy(email = "another@email.com"))
        sessionManager.saveUserInfo(newUser)

        assertEquals("anotherToken", sessionManager.getAuthToken())
        assertEquals(newUser, sessionManager.getUserInfo())

        sessionManager.invalidateSession()
        assertNull(sessionManager.getAuthToken())
        assertNull(sessionManager.getUserInfo())
    }

    @Test
    fun `basic operations with v2 on disk`() {
        val testContext: Context = ApplicationProvider.getApplicationContext()
        val prefs = testContext.getSharedPreferences("test", Context.MODE_PRIVATE)
        val v2UserInfo = v2UserInfo()
        val testToken = "someTestTokenV2"
        prefs.edit()
            .putString(PREF_USER_INFO, Gson().toJson(v2UserInfo))
            .putString(PREF_AUTH_TOKEN, testToken)
            .apply()

        val sessionManager = SessionManager(prefs)
        val expectedUserInfo = UserInfo(user = v2UserInfo.user, latestUpdateTime = v2UserInfo.latestUpdateTime)
        assertEquals(testToken, sessionManager.getAuthToken())
        assertEquals(expectedUserInfo, sessionManager.getUserInfo())

        sessionManager.invalidateSession()
        assertNull(sessionManager.getAuthToken())
        assertNull(sessionManager.getUserInfo())

        sessionManager.saveAuthToken("anotherToken")
        val newUser = expectedUserInfo.copy(user = expectedUserInfo.user.copy(email = "another@email.com"))
        sessionManager.saveUserInfo(newUser)

        assertEquals("anotherToken", sessionManager.getAuthToken())
        assertEquals(newUser, sessionManager.getUserInfo())

        sessionManager.invalidateSession()
        assertNull(sessionManager.getAuthToken())
        assertNull(sessionManager.getUserInfo())
    }
}
