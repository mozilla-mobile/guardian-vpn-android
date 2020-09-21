/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.FlakyTest
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.firefox.vpn.splash.SplashActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class PkceRegressionTest {

    @Rule
    @JvmField
    val splashActivityTestRule = ActivityTestRule(SplashActivity::class.java)

    @Rule
    @JvmField
    val intentReceiverActivityTestRule = ActivityTestRule(IntentReceiverActivity::class.java)

    @Test
    @FlakyTest
    /**
     * Flaky for two reasons.  1) this needs to touch the network, 2) Espresso tests are just flaky.
     */
    fun pkce_regression_test() {
        // Simulate response from user logging in at:
        // "https://stage-vpn.guardian.nonprod.cloudops.mozgcp.net/api/v2/vpn/login/android?code_challenge=fx-O4_N_sfGrXxLgDkByfVNgZUPCI1s5PqWp8k1fG8M=&code_challenge_method=S256"
        val authCode = "d60b4de6f4a8a6e2228e82b328729d9cc1666b96a1f7a5202fdc563c925bb7a3ea3f4efa1ef3c37d"
        val intentUri = Uri.parse(
            "https://stage-vpn.guardian.nonprod.cloudops.mozgcp.net" +
                    "/vpn/client/login/success?" +
                    "code=$authCode" +
                    "#Intent;category=android.intent.category.BROWSABLE;" +
                    "launchFlags=0x14000000;" +
                    "component=org.mozilla.firefox.vpn.debug/org.mozilla.firefox.vpn.IntentReceiverActivity;" +
                    "i.org.chromium.chrome.browser.referrer_id=18;" +
                    "S.com.android.browser.application_id=com.android.chrome;end"
        )
        val intent = Intent("android.intent.action.VIEW", intentUri)

        val receivedCode = CompletableDeferred<AuthCode>()
        IntentReceiverActivity.setAuthCodeReceivedDeferred(receivedCode)

        // IntentReceiverActivity launched with the above auth code
        intentReceiverActivityTestRule.launchActivity(intent)

        // assert an auth code was received
        runBlocking {
            withTimeout(5_000) {
                assertNotNull(receivedCode.await())
            }
        }

        runBlocking { delay(1_000) }

        // assert onboarding screen still shown, login did not proceed
        onView(withId(R.id.auth_btn)).check(matches(isDisplayed()))
    }
}
