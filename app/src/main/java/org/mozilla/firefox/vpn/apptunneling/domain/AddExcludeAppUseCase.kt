package org.mozilla.firefox.vpn.apptunneling.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.apptunneling.data.AppTunnelingRepository

class AddExcludeAppUseCase(
    private val appTunnelingRepository: AppTunnelingRepository
) {

    suspend operator fun invoke(packageName: String) = withContext(Dispatchers.IO) {
        appTunnelingRepository.addPackageExcluded(setOf(packageName))
    }

    suspend operator fun invoke(packageNameSet: Set<String>) = withContext(Dispatchers.IO) {
        appTunnelingRepository.addPackageExcluded(packageNameSet)
    }
}
