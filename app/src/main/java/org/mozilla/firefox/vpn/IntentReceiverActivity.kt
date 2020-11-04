/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CompletableDeferred
import org.mozilla.firefox.vpn.ext.toCode

/**
 * Code that is included in the `verify` request that is used to retrieve an [AuthToken].
 */
typealias AuthCode = String

/**
 * Abstraction that handles incoming [Intent]s for use elsewhere in the app.  After
 * processing an [Intent], this activity will finish itself.
 */
open class IntentReceiverActivity : AppCompatActivity() {

    companion object {

        /**
         * Set a [CompletableDeferred] that will be completed with an [AuthCode] the next
         * time one is received.  Note that this is not guaranteed to ever complete.
         *
         * This is static because the alternative was a similarly bad practice: weaving
         * the reference through many layers of Android framework code.
         */
        fun setAuthCodeReceivedDeferred(authCodeReceived: CompletableDeferred<AuthCode>) {
            _authCodeReceived = authCodeReceived
        }
        private var _authCodeReceived: CompletableDeferred<AuthCode> = CompletableDeferred()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.data?.toCode()?.let { authCode ->
            _authCodeReceived.complete(authCode)
        }

        finish()
    }
}
