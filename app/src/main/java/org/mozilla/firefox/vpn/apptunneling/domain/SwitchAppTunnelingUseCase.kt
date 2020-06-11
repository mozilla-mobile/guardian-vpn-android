package org.mozilla.firefox.vpn.apptunneling.domain

import org.mozilla.firefox.vpn.apptunneling.data.AppTunnelingRepository

class SwitchAppTunnelingUseCase(
    private val appTunnelingRepository: AppTunnelingRepository
) {

    operator fun invoke(isChecked: Boolean) {
        appTunnelingRepository.switchAppTunneling(isChecked)
    }
}
