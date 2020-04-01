/*
 * Copyright © 2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend

import android.os.SystemClock
import android.util.Pair
import com.wireguard.crypto.Key
import java.util.HashMap

class Statistics {
    private var lastTouched = SystemClock.elapsedRealtime()
    private val peerBytes = HashMap<Key, Pair<Long, Long>>()

    private val isStale: Boolean
        get() = SystemClock.elapsedRealtime() - lastTouched > 900

    fun add(key: Key, rx: Long, tx: Long) {
        peerBytes[key] = Pair.create(rx, tx)
        lastTouched = SystemClock.elapsedRealtime()
    }

    fun peers(): Array<Key> {
        return peerBytes.keys.toTypedArray()
    }

    fun peerRx(peer: Key): Long {
        return if (!peerBytes.containsKey(peer)) 0 else peerBytes[peer]?.first ?: 0
    }

    fun peerTx(peer: Key): Long {
        return if (!peerBytes.containsKey(peer)) 0 else peerBytes[peer]?.second ?: 0
    }

    fun totalRx(): Long {
        var rx: Long = 0
        for (`val` in peerBytes.values) {
            rx += `val`.first
        }
        return rx
    }

    fun totalTx(): Long {
        var tx: Long = 0
        for (`val` in peerBytes.values) {
            tx += `val`.second
        }
        return tx
    }
}
