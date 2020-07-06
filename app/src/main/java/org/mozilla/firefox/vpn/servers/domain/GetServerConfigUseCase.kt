package org.mozilla.firefox.vpn.servers.domain

import com.wireguard.config.Config
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.main.vpn.ConnectionConfig
import org.mozilla.firefox.vpn.servers.data.ServerInfo

fun CurrentDevice.toWgInterface(
    includedApps: List<String> = emptyList(),
    excludeApps: List<String> = emptyList(),
    serverInfo: ServerInfo
): Interface {
    return Interface.Builder().apply {
        setKeyPair(KeyPair(Key.fromBase64(privateKeyBase64)))
        addAddress(InetNetwork.parse(device.ipv4Address))
        addDnsServer(InetNetwork.parse(serverInfo.server.ipv4Gateway).address)
        excludeApplications(excludeApps)
        includeApplications(includedApps)
    }.build()
}

fun ServerInfo.toWgPeer(): Peer {
    return Peer.Builder().apply {
        setPublicKey(Key.fromBase64(server.publicKey))
        val endpoint = "${server.ipv4Address}:${server.portRanges.random().random()}"
        parseEndpoint(endpoint)
        setPersistentKeepalive(60)
        parseAllowedIPs("0.0.0.0/0")
    }.build()
}

fun ConnectionConfig.create(
    serverInfo: ServerInfo
): Config {
    return Config.Builder().apply {
        setInterface(currentDevice.toWgInterface(includedApps, excludeApps, serverInfo))
        addPeers(listOf(serverInfo.toWgPeer()))
    }.build()
}
