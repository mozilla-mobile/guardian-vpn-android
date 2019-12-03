package org.mozilla.firefox.vpn.servers.domain

import android.app.Application
import com.wireguard.config.Config
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import java.net.InetAddress
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.servers.data.ServerInfo

class GetServerConfigUseCase(
    appContext: Application,
    private val deviceRepository: DeviceRepository
) {
    private val pkgName = appContext.packageName

    operator fun invoke(serverInfo: ServerInfo, device: CurrentDevice): Config {
        val wgInterface = device.toWgInterface(listOf(pkgName))

        return Config.Builder().apply {
            setInterface(wgInterface)
            addPeers(listOf(serverInfo.toWgPeer()))
        }.build()
    }
}

fun CurrentDevice.toWgInterface(excludeApps: List<String> = emptyList()): Interface {
    return Interface.Builder().apply {
        setKeyPair(KeyPair(Key.fromBase64(privateKeyBase64)))
        addAddress(InetNetwork.parse(device.ipv4Address))
        addDnsServer(InetAddress.getByAddress(byteArrayOf(1, 1, 1, 1)))
        excludeApps.forEach { excludeApplication(it) }
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

fun CurrentDevice.createConfig(serverInfo: ServerInfo): Config {
    return Config.Builder().apply {
        setInterface(toWgInterface())
        addPeers(listOf(serverInfo.toWgPeer()))
    }.build()
}

fun Config.from(currentDevice: CurrentDevice, serverInfo: ServerInfo): Config {
    return Config.Builder().apply {
        setInterface(currentDevice.toWgInterface())
        addPeers(listOf(serverInfo.toWgPeer()))
    }.build()
}
