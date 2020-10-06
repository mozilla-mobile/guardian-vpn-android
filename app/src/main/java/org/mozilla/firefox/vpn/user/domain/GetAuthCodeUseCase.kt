/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.user.domain

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import org.mozilla.firefox.vpn.AuthCode
import org.mozilla.firefox.vpn.IntentReceiverActivity
import org.mozilla.firefox.vpn.crypto.CodeChallenge
import org.mozilla.firefox.vpn.onboarding.Bus
import org.mozilla.firefox.vpn.service.BrowserClosedWithoutLogin
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.service.NetworkException
import org.mozilla.firefox.vpn.service.UnknownException
import org.mozilla.firefox.vpn.util.LoginCustomTab
import org.mozilla.firefox.vpn.util.Result

class GetAuthCodeUseCase(
    private val bus: Bus
) {

    /**
     * Navigates the user to FxA login, then retrieves the auth code from the response.
     *
     * Note: this will block until the user either completes their login or closes the
     * custom tab.  Programmatically, this is a very long time.  Maybe don't use the main
     * thread.
     */
    suspend operator fun invoke(
        codeChallenge: CodeChallenge,
        scope: CoroutineScope
    ): Result<AuthCode> {

        val authCodeReceived = CompletableDeferred<AuthCode>().also {
            IntentReceiverActivity.setAuthCodeReceivedDeferred(it)
        }

        val loginUrl = GuardianService.getLoginUrl(codeChallenge)
        bus.promptLogin.postValue(loginUrl)

        return try {
            while (!authCodeReceived.isCompleted) {
                scope.ensureActive() // Yield to check for cancellation
                /* block */
            }

            when {
                authCodeReceived.isCompleted -> Result.Success(authCodeReceived.await())
                else -> Result.Fail(UnknownException("Get secret failed unexpectedly"))
            }
        } finally {
            authCodeReceived.cancel()

            Result.Fail(NetworkException)
        }
    }
}
