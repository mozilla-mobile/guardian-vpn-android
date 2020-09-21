/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

/**
 * PKCE code verifier.
 */
typealias CodeVerifier = String
/**
 * PKCE code challenge.
 */
typealias CodeChallenge = String

private const val CHALLENGE_FLAGS = Base64.URL_SAFE or Base64.NO_WRAP

/**
 * Contains utility functions for generating secure codes.
 */
object AuthCodeHelper {

    /**
     * Returns a cryptographically random key. Sent during token request as part of PKCE.
     */
    fun generateCodeVerifier(random: Random = SecureRandom().asKotlinRandom()): CodeVerifier {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '_', '.', '~')

        val size = (43..128).random(random)

        val sb = StringBuilder()
        for (i in 1..size) {
            sb.append(allowedChars.random(random))
        }
        return sb.toString()
    }

    /**
     * Returns a SHA-256 encoded hash based on [verifier]. Used to retrieve a token as
     * part of PKCE.
     */
    @Synchronized // MessageDigest is not thread-safe
    fun generateCodeChallenge(verifier: CodeVerifier): CodeChallenge {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(bytes, 0, bytes.size)
        val digest = messageDigest.digest()

        return Base64.encodeToString(digest, CHALLENGE_FLAGS)
    }
}
