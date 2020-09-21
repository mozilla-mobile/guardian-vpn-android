/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn

/**
 * SDK level that works with Robolectric.
 *
 * Android Studio is (as of 9/8/20) packaged with JDK 1.8, while Android API 29+ requires
 * Java 9 or newer. Aiming Robolectric tests at API 28 avoids this problem.
 */
const val WORKING_SDK = 28
