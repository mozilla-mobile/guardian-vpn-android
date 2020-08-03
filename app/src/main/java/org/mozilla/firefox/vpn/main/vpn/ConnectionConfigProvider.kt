package org.mozilla.firefox.vpn.main.vpn

import org.mozilla.firefox.vpn.apptunneling.domain.GetAppTunnelingSwitchStateUseCase
import org.mozilla.firefox.vpn.apptunneling.domain.GetExcludeAppUseCase
import org.mozilla.firefox.vpn.device.domain.CurrentDeviceUseCase

interface ConnectionConfigProvider {
    suspend fun getCurrentConnectionConfig(): ConnectionConfig?
}

class ConnectionConfigProviderImpl(
    private val currentDeviceUseCase: CurrentDeviceUseCase,
    private val getAppTunnelingSwitchStateUseCase: GetAppTunnelingSwitchStateUseCase,
    private val excludeAppUseCase: GetExcludeAppUseCase
) : ConnectionConfigProvider {
    override suspend fun getCurrentConnectionConfig(): ConnectionConfig? {
        return currentDeviceUseCase.invoke()?.let {
            val excludeAppsList = if (getAppTunnelingSwitchStateUseCase()) {
                excludeAppUseCase().toList()
            } else {
                emptyList()
            }
            ConnectionConfig(it, excludeApps = excludeAppsList)
        }
    }
}