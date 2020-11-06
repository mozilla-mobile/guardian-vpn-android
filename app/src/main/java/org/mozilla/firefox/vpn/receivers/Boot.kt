/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.ext.connectVpnIfPossible

/**
 * Boot broadcast receiver for auto-starting VPN service on pre-N devices.
 * This receiver is disabled for N+ devices. On these, Always-On VPN mode exists at the system-level,
 * and is the system-approved way of doing this.
 * In fact, newer Androids won't even allow us to do background work (such as starting a service) on boot.
 */
class LegacyBootBroadcastReceiver : BroadcastReceiver() {
    private val logTag: String = "LegacyBootBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(logTag, "onReceive")
        // Only run on pre-24.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(logTag, "Receiver disabled.")
            return
        }
        if (context == null) {
            Log.d(logTag, "No context, can't proceed.")
            return
        }
        // 'boot' intents are protected (i.e. only OS is allowed to emit them), but it's still a
        // good idea to make sure everything's in order.
        if (intent == null || intent.action != Intent.ACTION_BOOT_COMPLETED) {
            Log.w(logTag, "Intent not allowed.")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            tryToConnect(context)
        }
    }

    private suspend fun tryToConnect(context: Context) = context.connectVpnIfPossible(logTag)
}
