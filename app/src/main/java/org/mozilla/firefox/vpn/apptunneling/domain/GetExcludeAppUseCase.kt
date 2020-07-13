package org.mozilla.firefox.vpn.apptunneling.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.apptunneling.data.AppTunnelingRepository

class GetExcludeAppUseCase(
    private val appTunnelingRepository: AppTunnelingRepository
) {

    suspend operator fun invoke(forceUpdate: Boolean = false) = withContext(Dispatchers.IO) {
        if (forceUpdate) {
            val packages = appTunnelingRepository.getPackages(true).map { it.packageName }.toSet()
            val cachePackages = appTunnelingRepository.getCachedPackages()

            if (cachePackages.isNotEmpty()) {
                val addedPackages = packages.filterNot { cachePackages.contains(it) }.toSet()
                val deletedPackages = cachePackages.filterNot { packages.contains(it) }.toSet()

                if (!appTunnelingRepository.isProtectingNewApps()) {
                    appTunnelingRepository.addPackageExcluded(addedPackages)
                }
                appTunnelingRepository.removePackageExcluded(deletedPackages)
            }
            appTunnelingRepository.cachePackages(packages)
        }
        appTunnelingRepository.getPackageExcluded()
    }
}
