package org.mozilla.firefox.vpn.servers.domain

import android.app.Application
import com.wireguard.config.*
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import java.net.InetAddress

class GetServerConfigUseCase(
    appContext: Application,
    private val deviceRepository: DeviceRepository
) {
    private val pkgName = appContext.packageName

    operator fun invoke(serverInfo: ServerInfo): Config? {
        val currentDevice = deviceRepository.getDevice() ?: return null
        val device = currentDevice.device
        val privateKey = currentDevice.privateKeyBase64

        val wgInterface = Interface.Builder().apply {
            val ipv4Address = device.ipv4Address
            setKeyPair(KeyPair(Key.fromBase64(privateKey)))
            addAddress(InetNetwork.parse(ipv4Address))
            addDnsServer(InetAddress.getByAddress(byteArrayOf(1, 1, 1, 1)))
            excludeApplication(pkgName)
        }.build()

        return Config.Builder().apply {
            setInterface(wgInterface)

            val peers = ArrayList<Peer>(1)
            peers.add(Peer.Builder().apply {
                setPublicKey(Key.fromBase64(serverInfo.server.publicKey))
                val endpoint = "${serverInfo.server.ipv4Address}:${serverInfo.server.portRanges.random().random()}"
                parseEndpoint(endpoint)
                setPersistentKeepalive(60)
                parseAllowedIPs("0.0.0.0/0")
            }.build())

            addPeers(peers)
        }.build()
    }
}