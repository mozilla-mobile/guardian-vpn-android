package org.mozilla.firefox.vpn.update

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.mozilla.firefox.vpn.service.Version

class UpdateManager(appContext: Context) {
    private val appUpdateManager = AppUpdateManagerFactory.create(appContext)

    suspend fun getLatestUpdate() = suspendCoroutine<Version?> { cont ->
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    cont.resume(Version(info.availableVersionCode().toString(), "", ""))
                } else {
                    cont.resume(null)
                }
            }
            .addOnFailureListener {
                cont.resume(null)
            }
    }
}
