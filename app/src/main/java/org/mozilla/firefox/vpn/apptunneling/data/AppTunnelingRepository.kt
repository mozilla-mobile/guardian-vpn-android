package org.mozilla.firefox.vpn.apptunneling.data

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.edit

class AppTunnelingRepository(
    private val packageManager: PackageManager,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val PREF_KEY_STRINGSET_EXCLUDE = "key_stringset_apptunneling_exclude_list"
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
        sharedPreferences.edit(true) {
            putStringSet(PREF_KEY_STRINGSET_EXCLUDE, packageSet)
        }
    }

    fun removePackageExcludes(packageName: String) {
        val packageSet =
            sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet<String>()

        if (packageName.isNotEmpty()) {
            packageSet.remove(packageName)
        }
        sharedPreferences.edit(true) {
            putStringSet(PREF_KEY_STRINGSET_EXCLUDE, packageSet)
        }
    }

    fun addPackageExcludes(packageNameSet: Set<String>) {
        val packageSet =
            sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet<String>()

        if (packageNameSet.isNotEmpty()) {
            packageSet.addAll(packageNameSet)
        }
        sharedPreferences.edit(true) {
            putStringSet(PREF_KEY_STRINGSET_EXCLUDE, packageSet)
        }
    }

    fun addPackageExcludes(packageName: String) {
        val packageSet =
            sharedPreferences.getStringSet(PREF_KEY_STRINGSET_EXCLUDE, null) ?: HashSet<String>()

        if (packageName.isNotEmpty()) {
            packageSet.add(packageName)
        }
        sharedPreferences.edit(true) {
            putStringSet(PREF_KEY_STRINGSET_EXCLUDE, packageSet)
        }
    }
}
