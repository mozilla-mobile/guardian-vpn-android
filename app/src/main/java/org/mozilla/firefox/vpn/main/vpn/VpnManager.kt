package org.mozilla.firefox.vpn.main.vpn

import com.wireguard.config.Config
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.servers.data.ServerInfo

interface VpnManager : VpnStateProvider {

    fun isGranted(): Boolean

    suspend fun connect(server: ServerInfo, serverConfig: Config)

    suspend fun switch(oldServer: ServerInfo, newServer: ServerInfo, serverConfig: Config)

    suspend fun disconnect()

    suspend fun shutdownConnection()

    fun isConnected(): Boolean

    fun getDuration(): Long
}
