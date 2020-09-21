/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.ext

import android.net.Uri
import org.mozilla.firefox.vpn.AuthCode

private const val CODE_QUERY_PARAM = "code"

private val ALLOWED_CODE_CHARS = (('0'..'9') + ('a'..'f')).toSet()

fun Uri.toCode(): AuthCode? {
    val code = getQueryParameter(CODE_QUERY_PARAM)

    return if (
        code == null ||
        code.length != 80 ||
        code.any { char -> !ALLOWED_CODE_CHARS.contains(char) }
    ) {
        null
    } else {
        code
    }
}
