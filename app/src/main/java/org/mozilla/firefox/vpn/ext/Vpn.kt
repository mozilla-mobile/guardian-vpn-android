/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package org.mozilla.firefox.vpn.ext

import android.content.Context
import android.util.Log
import org.mozilla.firefox.vpn.apptunneling.domain.GetAppTunnelingSwitchStateUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetExcludeAppUseCase
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.main.vpn.ConnectionConfig
import org.mozilla.firefox.vpn.servers.domain.FilterStrategy
import org.mozilla.firefox.vpn.servers.domain.GetSelectedServerUseCase
import org.mozilla.firefox.vpn.servers.domain.GetServersUseCase
import org.mozilla.firefox.vpn.util.Result

suspend fun Context.connectVpnIfPossible(logTag: String) {
    Log.d(logTag, "connectVpnIfPossible...")

    val vpnManager = this.guardianComponent.vpnManager
    if (!vpnManager.isGranted()) {
        Log.d(logTag, "Not granted")
        return
    }

    if (vpnManager.isConnected()) {
        Log.d(logTag, "Already connected")
        return
    }

    val serverRepository = this.guardianComponent.serverRepo
    val currentDevice = CurrentDeviceUseCase(
        this.guardianComponent.deviceRepo,
        this.guardianComponent.userRepo,
        this.guardianComponent.userStateResolver
    )()

    if (currentDevice == null) {
        Log.d(logTag, "No current device")
        return
    }

    val selectedServerInfoResult = when (
        val servers = GetServersUseCase(serverRepository).invoke(FilterStrategy.ByCity)
    ) {
        is Result.Success -> {
            GetSelectedServerUseCase(serverRepository).invoke(FilterStrategy.ByCity, servers.value)
        }
        is Result.Fail -> {
            Log.d(logTag, "Couldn't get servers")
            return
        }
    }

    val selectedServer = when (selectedServerInfoResult) {
        is Result.Success -> selectedServerInfoResult.value
        is Result.Fail -> {
            Log.d(logTag, "Couldn't get selected server")
            return
        }
    }

    val getAppTunnelingSwitchStateUseCase = GetAppTunnelingSwitchStateUseCase(this.guardianComponent.appTunnelingRepo)
    val getExcludeAppUseCase = GetExcludeAppUseCase(this.guardianComponent.appTunnelingRepo)
    val excludeApps = if (getAppTunnelingSwitchStateUseCase()) {
        getExcludeAppUseCase().toList()
    } else {
        emptyList()
    }

    Log.d(logTag, "vpnManager.connect...")
    vpnManager.connect(selectedServer, ConnectionConfig(currentDevice, excludeApps = excludeApps))
}
