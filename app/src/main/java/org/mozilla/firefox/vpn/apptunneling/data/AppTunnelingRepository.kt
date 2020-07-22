package org.mozilla.firefox.vpn.apptunneling.data

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import org.mozilla.firefox.vpn.BuildConfig
import org.mozilla.firefox.vpn.apptunneling.hasPermission
import org.mozilla.firefox.vpn.apptunneling.isSystemApp
import org.mozilla.firefox.vpn.util.putStringSetSafe

class AppTunnelingRepository(
    private val packageManager: PackageManager,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val PREF_KEY_STRINGSET_EXCLUDE = "key_stringset_apptunneling_exclude_list"
        const val PREF_KEY_USE_APP_TUNNELING = "key_use_app_tunneling"
    }

    fun getPackages(
        includeInternalApps: Boolean,
        joinBrowserApps: Boolean = true
    ): List<ApplicationInfo> {
        val applicationInfoList = packageManager.getInstalledApplications(0)
        val browserApps = if (joinBrowserApps) { resolveBrowserApps() } else { emptyList() }

        return applicationInfoList
            .asSequence()
            .filter { includeInternalApps || !it.isSystemApp() }
            .filter { it.packageName != BuildConfig.APPLICATION_ID }
            .filter { it.hasPermission(packageManager, android.Manifest.permission.INTERNET) }
            .plus(browserApps)
            .distinctBy { it.packageName }
            .sortedBy { it.loadLabel(packageManager).toString() }
            .toList()
    }

    fun getPackageExcluded(): Set<String> {
        return sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet()
    }

    fun removePackageExcluded(packageNameSet: Set<String>) {
        val packageSet =
            sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet<String>()
        if (packageNameSet.isNotEmpty()) {
            packageSet.removeAll(packageNameSet)
        }
        sharedPreferences.putStringSetSafe(PREF_KEY_STRINGSET_EXCLUDE, packageSet)
    }

    fun addPackageExcluded(packageNameSet: Set<String>) {
        val packageSet =
            sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet<String>()

        if (packageNameSet.isNotEmpty()) {
            packageSet.addAll(packageNameSet)
        }
        sharedPreferences.putStringSetSafe(PREF_KEY_STRINGSET_EXCLUDE, packageSet)
    }

    fun getAppTunnelingSwitchState(): Boolean {
        return sharedPreferences.getBoolean(PREF_KEY_USE_APP_TUNNELING, false)
    }

    fun switchAppTunneling(isChecked: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_KEY_USE_APP_TUNNELING, isChecked).apply()
    }

    private fun resolveBrowserApps(): List<ApplicationInfo> {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mozilla.org/"))
            .apply { addCategory(Intent.CATEGORY_BROWSABLE) }
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .map { it.activityInfo.applicationInfo }
    }
}
