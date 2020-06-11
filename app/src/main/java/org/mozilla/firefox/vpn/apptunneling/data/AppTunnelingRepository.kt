package org.mozilla.firefox.vpn.apptunneling.data

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import org.mozilla.firefox.vpn.BuildConfig
import org.mozilla.firefox.vpn.util.putStringSetSafe

class AppTunnelingRepository(
    private val packageManager: PackageManager,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val PREF_KEY_STRINGSET_EXCLUDE = "key_stringset_apptunneling_exclude_list"
        const val PREF_KEY_USE_APP_TUNNELING = "key_use_app_tunneling"
    }

    fun getPackages(includeInternalApps: Boolean): List<ApplicationInfo> {
        val applicationInfoList = packageManager.getInstalledApplications(0)
        val packageList = applicationInfoList.filter {
            PERMISSION_GRANTED == packageManager.checkPermission(
                android.Manifest.permission.INTERNET,
                it.packageName
            )
        }.filter {
            includeInternalApps or ((it.flags and ApplicationInfo.FLAG_SYSTEM) == 0)
        }.filter {
            it.packageName != BuildConfig.APPLICATION_ID
        }

        return packageList
    }

    fun getPackageExcludes(): Set<String> {
        val packageSet =
            sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet<String>()
        return packageSet
    }

    fun removePackageExcludes(packageNameSet: Set<String>) {
        val packageSet =
            sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet<String>()
        if (packageNameSet.isNotEmpty()) {
            packageSet.removeAll(packageNameSet)
        }
        sharedPreferences.putStringSetSafe(PREF_KEY_STRINGSET_EXCLUDE, packageSet)
    }

    fun removePackageExcludes(packageName: String) {
        val packageSet =
            sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet<String>()

        if (packageName.isNotEmpty()) {
            packageSet.remove(packageName)
        }
        sharedPreferences.putStringSetSafe(PREF_KEY_STRINGSET_EXCLUDE, packageSet)
    }

    fun addPackageExcludes(packageNameSet: Set<String>) {
        val packageSet =
            sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet<String>()

        if (packageNameSet.isNotEmpty()) {
            packageSet.addAll(packageNameSet)
        }
        sharedPreferences.putStringSetSafe(PREF_KEY_STRINGSET_EXCLUDE, packageSet)
    }

    fun addPackageExcludes(packageName: String) {
        val packageSet =
            sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet<String>()

        if (packageName.isNotEmpty()) {
            packageSet.add(packageName)
        }
        sharedPreferences.putStringSetSafe(PREF_KEY_STRINGSET_EXCLUDE, packageSet)
    }

    fun getAppTunnelingSwitchState(): Boolean {
        return sharedPreferences.getBoolean(PREF_KEY_USE_APP_TUNNELING, false)
    }

    fun switchAppTunneling(isChecked: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_KEY_USE_APP_TUNNELING, isChecked).apply()
    }
}
