/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.lang.RuntimeException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildActivity
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestGuardianApp::class, sdk = [WORKING_SDK])
class IntentReceiverActivityTest {

    private val code = "d60b4de6f4a8a6e2228e82b328729d9cc1666b96a1f7a5202fdc563c925bb7a3ea3f4efa1ef3c37d"
    private val intentUri = Uri.parse(
        "https://stage-vpn.guardian.nonprod.cloudops.mozgcp.net" +
                "/vpn/client/login/success?" +
                "code=$code" +
                "#Intent;category=android.intent.category.BROWSABLE;" +
                "launchFlags=0x14000000;" +
                "component=org.mozilla.firefox.vpn.debug/org.mozilla.firefox.vpn.IntentReceiverActivity;" +
                "i.org.chromium.chrome.browser.referrer_id=18;" +
                "S.com.android.browser.application_id=com.android.chrome;end"
    )
    private val intent = Intent("android.intent.action.VIEW", intentUri)

    private lateinit var activity: ActivityController<IntentReceiverActivity>
    private lateinit var first: CompletableDeferred<AuthCode>
    private lateinit var second: CompletableDeferred<AuthCode>

    @Before
    fun setup() {
        first = CompletableDeferred()
        second = CompletableDeferred()
    }

    @Test
    fun `WHEN no intent is sent THEN nextLogin should not complete`() {
        assertFalse(first.isCompleted)

        IntentReceiverActivity.setAuthCodeReceivedDeferred(first)
        activity = buildActivity(IntentReceiverActivity::class.java).setup()

        assertFalse(first.isCompleted)
    }

    @Test
    fun `WHEN intent is sent THEN nextLogin should complete`() = runBlockingTest {
        assertFalse(first.isCompleted)

        IntentReceiverActivity.setAuthCodeReceivedDeferred(first)
        buildActivity(IntentReceiverActivity::class.java, intent).setup()

        assertTrue(first.isCompleted)
        assertEquals(code, first.await())

        // new deferred should be set
        IntentReceiverActivity.setAuthCodeReceivedDeferred(second)
        assertNotEquals(first, second)
        assertFalse(second.isCompleted)
    }

    @Test
    fun `GIVEN intent was sent WHEN activity is recreated several times THEN only one token should be emitted`() = runBlockingTest {
        assertFalse(first.isCompleted)

        IntentReceiverActivity.setAuthCodeReceivedDeferred(first)
        val controller =
            buildActivity(IntentReceiverActivity::class.java, intent).setup()

        assertTrue(first.isCompleted)
        assertEquals(code, first.await())

        IntentReceiverActivity.setAuthCodeReceivedDeferred(second)
        assertFalse(second.isCompleted)

        controller.pause().stop().start().resume()

        controller.pause().stop().start().resume()

        assertFalse(second.isCompleted)
    }

    @Test
    fun `GIVEN code is included WHEN other query params are included THEN they should be ignored in favor of code`() = runBlockingTest {
            val intentUri = Uri.parse(
                "https://stage-vpn.guardian.nonprod.cloudops.mozgcp.net" +
                        "/vpn/client/login/success?" +
                        "code=$code&" +
                        "token=myToken&" +
                        "secret=mySecret&" +
                        "lotteryTicketNumber=5" +
                        "#Intent;category=android.intent.category.BROWSABLE;" +
                        "launchFlags=0x14000000;" +
                        "component=org.mozilla.firefox.vpn.debug/org.mozilla.firefox.vpn.IntentReceiverActivity;" +
                        "i.org.chromium.chrome.browser.referrer_id=18;" +
                        "S.com.android.browser.application_id=com.android.chrome;end"
            )
            val intent = Intent("android.intent.action.VIEW", intentUri)

            IntentReceiverActivity.setAuthCodeReceivedDeferred(first)
            assertFalse(first.isCompleted)

            buildActivity(IntentReceiverActivity::class.java, intent).setup()

            assertTrue(first.isCompleted)
            assertEquals(code, first.await())
        }

    @Test
    fun `GIVEN code is not included WHEN other query params are included THEN nextAuthCodeReceived should not complete`() {
        val intentUri = Uri.parse(
            "https://stage-vpn.guardian.nonprod.cloudops.mozgcp.net" +
                    "/vpn/client/login/success?" +
                    "token=myToken&" +
                    "secret=mySecret&" +
                    "lotteryTicketNumber=5" +
                    "#Intent;category=android.intent.category.BROWSABLE;" +
                    "launchFlags=0x14000000;" +
                    "component=org.mozilla.firefox.vpn.debug/org.mozilla.firefox.vpn.IntentReceiverActivity;" +
                    "i.org.chromium.chrome.browser.referrer_id=18;" +
                    "S.com.android.browser.application_id=com.android.chrome;end"
        )
        val intent = Intent("android.intent.action.VIEW", intentUri)

        IntentReceiverActivity.setAuthCodeReceivedDeferred(first)
        assertFalse(first.isCompleted)

        buildActivity(IntentReceiverActivity::class.java, intent).setup()

        assertFalse(first.isCompleted)
    }

    @Test
    fun `valid codes should pass`() {
        val failures = listOf(
            "d60b4de6f4a8a6e2228e82b328729d9cc1666b96a1f7a5202fdc563c925bb7a3ea3f4efa1ef3c37d",
            "d6e9d39217dca1c1e09f742cd9d43705436f9b508eb460191d3743a62ad4bbedd6f64db531243b8b",
            "94261d5d6baf934f4285ff7c89f0c855e82647a8fa5a44f6e4ce00ce41ea8ba22fb10f74c7bcf206",
            "b1fe04adfef209ede45d5144cdbb9ab87924bded27b1a2310989da597fadf7b559886d07e9ca2394",
            "572e46b7773c5fa358211aa5338f8ddbda8375e9047d662cb97e49803efd91883627a10a52e9ac1a",
            "a9598960552e7fdd2d6ea9c403b762b198d318de7dc4f361c52ab2d6a2fd82b83417c3e33ce2c48f",
            "a250663294300db88f5a065b96cb9ff257feb095173d643fb7560d39ba615cdd4d43682d1c9d5063",
            "28aa232db5769b9510c428cb28fd563817a17d05301ff8f58b0f992de7576fcc8b1a4bf624d0d1c8"
        ).filter { !validateAuthCode(it, shouldPass = true) }

        if (failures.isNotEmpty()) {
            println("${failures.size} valid codes failed validation: $failures")
            throw RuntimeException()
        }
    }

    @Test
    fun `invalid codes should not pass`() {
        // Codes should always be 80 characters of lowercase hexadecimal
        val successes = listOf(
            "d60b4de6f4a8a6e2228e82b328729d9cc1666b96a1f7a5202fdc563c925bb7a3ea3f4efa1ef3c37", // 79 chars
            "d60b4de6f4a8a6e2228e82b328729d9cc1666b96a1f7a5202fdc563c925bb7a3ea3f4efa1ef3c37a8", // 81 chars
            "d60b4de6f4a8a6e2228e82b328729d9cc1-666b96a1f7a5202fdc563c925bb7a3ea3f4efa1ef3c37", // includes dash
            "60b4de6f4a8a6e2228E82b328729d9cc1666b96A1f7a5202fdc563c925bb7a3e~a3f4efa1ef3c37", // includes tilde
            "d60b4d e6f4a8a6e2228e82b328729d9cc1666b96a1f7a5202fdc563c925bb7a3ea3f4efa1ef3c37", // includes space
            "d60b4de6f4a8a6e2228e82b328729d9cc1666b96a1f7a.5202fdc563c925bb7a3ea3f4efa1ef3c37", // includes .
            "d60b4de6f4a8a6e2228e82b328729d9cc1666b96a1f7a5202fdc563c92/5bb7a3ea3f4efa1ef3c37", // includes /
            "60b4de6f4a8a6e2228g82b328729d9cc1666b96A1f7a5202fdc563c925bb7a3ea3f4efa1ef3c37d", // includes non-hexadecimal char (g)
            "60b4de6f4a8a6e2228E82b328729d9cc1666b96A1f7a5202fdc563c925bb7a3ea3f4efa1ef3c37d" // includes some uppercase
        ).filter { !validateAuthCode(it, shouldPass = false) }

        if (successes.isNotEmpty()) {
            println("${successes.size} invalid codes passed validation: $successes")
            throw RuntimeException()
        }
    }

    private fun validateAuthCode(code: String, shouldPass: Boolean): Boolean {
        val intentUri = Uri.parse(
            "https://stage-vpn.guardian.nonprod.cloudops.mozgcp.net" +
                    "/vpn/client/login/success?" +
                    "code=$code&" +
                    "#Intent;category=android.intent.category.BROWSABLE;" +
                    "launchFlags=0x14000000;" +
                    "component=org.mozilla.firefox.vpn.debug/org.mozilla.firefox.vpn.IntentReceiverActivity;" +
                    "i.org.chromium.chrome.browser.referrer_id=18;" +
                    "S.com.android.browser.application_id=com.android.chrome;end"
        )
        val intent = Intent("android.intent.action.VIEW", intentUri)

        val authCodeReceived = CompletableDeferred<AuthCode>()
        IntentReceiverActivity.setAuthCodeReceivedDeferred(authCodeReceived)
        assertFalse(authCodeReceived.isCompleted)

        buildActivity(IntentReceiverActivity::class.java, intent).setup()

        return runBlocking {
            shouldPass == (authCodeReceived.isCompleted && code == authCodeReceived.await())
        }
    }
}
