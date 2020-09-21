/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.crypto

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.firefox.vpn.TestGuardianApp
import org.mozilla.firefox.vpn.WORKING_SDK
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestGuardianApp::class, sdk = [WORKING_SDK])
class AuthCodeHelperTest {

    private lateinit var deterministicRandom: Random

    @Before
    fun setup() {
        deterministicRandom = Random(seed = 42)
    }

    @Test
    fun `code_challenges generated from the same code_verifier will always match`() {
        val verifier = "i'm a code verifier!"

        val challenge = AuthCodeHelper.generateCodeChallenge(verifier)

        AuthCodeHelper.generateCodeChallenge("some")
        AuthCodeHelper.generateCodeChallenge("other")
        AuthCodeHelper.generateCodeChallenge("verifiers")

        val challenge2 = AuthCodeHelper.generateCodeChallenge(verifier)

        assertEquals(challenge, challenge2)
    }

    @Test
    fun `code_challenges generated different code_verifiers should not match`() {
        val challenge1 = AuthCodeHelper.generateCodeChallenge("I'm the first verifier!")
        val challenge2 = AuthCodeHelper.generateCodeChallenge("I'm totally unrelated to the other one")

        assertNotEquals(challenge1, challenge2)
    }

    @Test
    fun `deterministicRandom is deterministic`() {
        val verifier = AuthCodeHelper.generateCodeVerifier(deterministicRandom)
        assertEquals(
            "qpUrMTIZVoB31IKqc.rznkjs9RiK.qdLWXktKUrqXnm-To8VSRb7iMELP4WH4vDknyPD3AuPDlHDJu8y8~DDb8G-Rdljl3r9rxsB5b",
            verifier
        )
    }

    @Test
    fun `code_challenges should contain a trailing '=' for padding`() {
        val verifier = AuthCodeHelper.generateCodeVerifier(deterministicRandom)
        val challenge = AuthCodeHelper.generateCodeChallenge(verifier)
        assertEquals("=", challenge.takeLast(1))
    }

    @Test
    fun `code_verifiers should not contain a trailing '='`() {
        val verifier = AuthCodeHelper.generateCodeVerifier(deterministicRandom)
        assertNotEquals("=", verifier.takeLast(1))
    }

    @Test
    fun `code_challenges should be 44 characters long`() {
        val verifier = AuthCodeHelper.generateCodeVerifier(deterministicRandom)
        val challenge = AuthCodeHelper.generateCodeChallenge(verifier)
        assertEquals(44, challenge.length)
    }

    @Test
    fun `code_challenge should sha256 hash its input`() {
        // This uses hardcoded values provided by the backend team for manual testing
        val verifier = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val codeChallenge = AuthCodeHelper.generateCodeChallenge(verifier)

        assertEquals("1uxomN6H3axuWzYRcIp6ocLSmCkzScwabCmaHbcUnTg=", codeChallenge)
    }

    @Test
    fun `code_challenge hashing should match server expectation`() {
        val verifier = "pqpUrMTIZVoB31IKqc.rznkjs9RiK.qdLWXktKUrqXn"
        val codeChallenge = AuthCodeHelper.generateCodeChallenge(verifier)

        assertEquals("o23Jen9tjz5kkUrEuwWiPr-XaNszrAKusCj80QxipsU=", codeChallenge)
    }

    @Test
    fun `code_challenge hashing should handle all expected characters`() {
        val verifier = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        val codeChallenge = AuthCodeHelper.generateCodeChallenge(verifier)

        assertEquals("RZ77XZltYSfl0BLxuGd8pHGJ4EoMoVDVuSWHgNq3RY8=", codeChallenge)
    }
}
