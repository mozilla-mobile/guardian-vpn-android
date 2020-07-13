package org.mozilla.firefox.vpn.apptunneling.domain

import org.mozilla.firefox.vpn.apptunneling.data.AppTunnelingRepository

class GetProtectNewAppsSwitchStateUseCase(
    private val appTunnelingRepository: AppTunnelingRepository
) {

    operator fun invoke(): Boolean {
        return appTunnelingRepository.isProtectingNewApps()
    }
}
