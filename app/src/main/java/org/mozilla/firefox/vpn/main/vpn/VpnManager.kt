package org.mozilla.firefox.vpn.main.vpn

import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.servers.data.ServerInfo

interface VpnManager : VpnStateProvider {

    fun isGranted(): Boolean

    suspend fun connect(server: ServerInfo, currentDevice: CurrentDevice)

    suspend fun switch(oldServer: ServerInfo, newServer: ServerInfo, currentDevice: CurrentDevice)

    suspend fun disconnect()

    suspend fun shutdownConnection()

    fun isConnected(): Boolean

    fun getDuration(): Long
}
