/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.ext

import com.hadilq.liveevent.LiveEvent

/**
 * Adds an event on to the [LiveEvent]. This makes usage more idiomatic by letting
 * [LiveEvent]s be called as functions.
 *
 * EXAMPLE
 *
 * With this extension:
 * ```
 * promptLogin(info.loginUrl)
 * ```
 *
 * Without this extension:
 * ```
 * promptLogin.value = info.loginUrl
 * ```
 */
operator fun <T> LiveEvent<T>.invoke(value: T) {
    this.value = value
}

/**
 * Adds an event on to the [LiveEvent]. This makes usage more idiomatic by letting
 * [LiveEvent]s be called as functions.
 *
 * EXAMPLE
 *
 * With this extension:
 * ```
 * promptLogin()
 * ```
 *
 * Without this extension:
 * ```
 * promptLogin.value = Unit
 * ```
 */
operator fun LiveEvent<Unit>.invoke() {
    this.value = Unit
}
