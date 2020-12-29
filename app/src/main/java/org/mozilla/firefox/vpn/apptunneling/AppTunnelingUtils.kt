package org.mozilla.firefox.vpn.apptunneling

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

fun ApplicationInfo.isSystemApp(): Boolean {
    // For the apps on our devices, FLAG_SYSTEM is always set if FLAG_UPDATED_SYSTEM_APP is set.
    // Being uncertain if it's possible an app can have only FLAG_UPDATED_SYSTEM_APP being set, we
    // defensively check both bits here
    val systemFlag = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
    return (this.flags and systemFlag) != 0
}

fun ApplicationInfo.isUpdatedSystemApp(): Boolean {
    val updatedSystemFlag = ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
    return (this.flags and updatedSystemFlag) != 0
}

fun ApplicationInfo.hasPermission(packageManager: PackageManager, permission: String): Boolean {
    return PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(permission, this.packageName)
}
