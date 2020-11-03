/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.user.domain

import kotlinx.coroutines.CoroutineScope
import org.mozilla.firefox.vpn.crypto.AuthCodeHelper
import org.mozilla.firefox.vpn.user.data.AuthToken
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.util.Result
import org.mozilla.firefox.vpn.util.flatMapValue
import org.mozilla.firefox.vpn.util.mapValue

class GetTokenUseCase(
    private val userRepository: UserRepository,
    private val getAuthCodeUseCase: GetAuthCodeUseCase
) {

    /**
     * Begin login flow and await either a token or failure.
     *
     * This is not guaranteed to complete if the user 1) never finishes signing in and 2) never
     * cancels their login flow.
     */
    suspend operator fun invoke(scope: CoroutineScope): Result<AuthToken> {
        val codeVerifier = AuthCodeHelper.generateCodeVerifier()
        val codeChallenge = AuthCodeHelper.generateCodeChallenge(codeVerifier)

        return getAuthCodeUseCase(codeChallenge, scope)
            .flatMapValue { secret -> userRepository.verifyLogin(secret, codeVerifier) }
            .mapValue { loginResult -> loginResult.token }
    }
}
