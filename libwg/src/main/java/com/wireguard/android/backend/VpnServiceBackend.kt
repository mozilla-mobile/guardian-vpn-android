/*
 * Copyright © 2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend

import android.os.ParcelFileDescriptor

class VpnServiceBackend : BackendNative {

    private val delegate: VpnServiceDelegate

    constructor(delegate: VpnServiceDelegate) {
        this.delegate = delegate
    }

    fun tunnelUp(tunnel: Tunnel, tunFd: ParcelFileDescriptor, config: String) {

        val handle  = wgTurnOn(tunnel.name, tunFd.detachFd(), config)

        val socketV4 = wgGetSocketV4(handle)
        val socketV6 = wgGetSocketV6(handle)

        delegate.protect(socketV4)
        delegate.protect(socketV6)

        tunnel.tunnelHandle = handle
    }

    fun tunnelDown(tunnel: Tunnel) {
        val socket = tunnel.tunnelHandle ?: return
        wgTurnOff(socket)
        tunnel.tunnelHandle = null
    }

    fun getVersion(): String {
        return wgVersion()
    }

    fun getConfig(tunnel: Tunnel): String? {
        return tunnel.tunnelHandle?.let { wgGetConfig(it) }
    }

    interface VpnServiceDelegate {
        fun protect(socket: Int): Boolean
    }
}