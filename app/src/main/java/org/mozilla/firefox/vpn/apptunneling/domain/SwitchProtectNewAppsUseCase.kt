package org.mozilla.firefox.vpn.apptunneling.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.apptunneling.data.AppTunnelingRepository

class SwitchProtectNewAppsUseCase(
    private val appTunnelingRepository: AppTunnelingRepository
) {

    suspend operator fun invoke(isChecked: Boolean) = withContext(Dispatchers.IO) {
        appTunnelingRepository.switchProtectNewApps(isChecked)
    }
}
