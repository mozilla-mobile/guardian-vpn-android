/*
 * Copyright © 2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.wireguard.config.Config
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyFormatException

abstract class WireGuardVpnService : android.net.VpnService(), ServiceProxy {

    abstract val tunnelManager: TunnelManager<*>

    private var currentTunnel: Tunnel? = null

    private val backend: VpnServiceBackend = VpnServiceBackend(
            object: VpnServiceBackend.VpnServiceDelegate{
                override fun protect(socket: Int): Boolean {
                    return this@WireGuardVpnService.protect(socket)
                }
            }
    )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.getStringExtra(EXTRA_COMMAND) ?: "") {
            COMMAND_TURN_ON -> turnOn()
            COMMAND_TURN_OFF -> turnOff()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun turnOn() {
        val tunnel = tunnelManager.tunnel ?: return
        val config = tunnel.config

        Builder()
                .applyConfig(config)
                .establish()
                ?.let {
                    backend.tunnelUp(tunnel, it, config.toWgUserspaceString())
                    currentTunnel = tunnel
                    tunnelManager.stateListener?.onServiceUp(this)
                }
    }

    private fun turnOff() {
        tunnelManager.tunnel?.let { backend.tunnelDown(it) }
        tunnelManager.stateListener?.onServiceDown(false)
    }

    override fun onRevoke() {
        tunnelManager.tunnel?.let { backend.tunnelDown(it) }
        tunnelManager.stateListener?.onServiceDown(true)
        super.onRevoke()
    }

    override fun getStatistic(tunnel: Tunnel): Statistics {
        val stats = Statistics()

        val config = backend.getConfig(tunnel) ?: return stats
        var key: Key? = null
        var rx: Long = 0
        var tx: Long = 0
        for (line in config.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (line.startsWith("public_key=")) {
                key?.let { stats.add(it, rx, tx) }
                rx = 0
                tx = 0
                key = try {
                    Key.fromHex(line.substring(11))
                } catch (ignored: KeyFormatException) {
                    null
                }

            } else if (line.startsWith("rx_bytes=")) {
                if (key == null)
                    continue
                rx = try {
                    java.lang.Long.parseLong(line.substring(9))
                } catch (ignored: NumberFormatException) {
                    0
                }

            } else if (line.startsWith("tx_bytes=")) {
                if (key == null)
                    continue
                tx = try {
                    java.lang.Long.parseLong(line.substring(9))
                } catch (ignored: NumberFormatException) {
                    0
                }

            }
        }
        key?.let { stats.add(it, rx, tx) }
        return stats
    }

    companion object {
        const val EXTRA_COMMAND = "command"
        const val COMMAND_TURN_ON = "turn_on"
        const val COMMAND_TURN_OFF = "turn_off"
    }
}

fun VpnService.Builder.applyConfig(config: Config): VpnService.Builder {
    config.`interface`.apply {
        excludedApplications.forEach { addDisallowedApplication(it) }
        includedApplications.forEach { addAllowedApplication(it) }
        addresses.forEach { addAddress(it.address, it.mask) }
        dnsServers.forEach { addDnsServer(it.hostAddress) }
        setMtu(mtu.orElse(1280))
    }

    config.peers.flatMap { it.allowedIps }.forEach { addRoute(it.address, it.mask) }

    setBlocking(true)
    return this
}

fun Tunnel.isUp(): Boolean {
    return this.state == Tunnel.State.Up
}

class TunnelManager<T : WireGuardVpnService>(private val serviceClass: Class<T>) {
    var tunnel: Tunnel? = null
    var stateListener: VpnServiceStateListener? = null

    fun turnOn(context: Context, tunnel: Tunnel, listener: VpnServiceStateListener) {
        this.tunnel = tunnel
        this.stateListener = listener
        context.startService(Intent(context, serviceClass).apply {
            putExtra(WireGuardVpnService.EXTRA_COMMAND, WireGuardVpnService.COMMAND_TURN_ON)
        })
    }

    fun turnOff(context: Context) {
        context.startService(Intent(context, serviceClass).apply {
            putExtra(WireGuardVpnService.EXTRA_COMMAND, WireGuardVpnService.COMMAND_TURN_OFF)
        })
    }

    fun isConnected(tunnel: Tunnel): Boolean {
        return tunnel.isUp()
    }
}

interface ServiceProxy {
    fun getStatistic(tunnel: Tunnel): Statistics
}

interface VpnServiceStateListener {
    fun onServiceUp(proxy: ServiceProxy)
    fun onServiceDown(isRevoked: Boolean)
}
