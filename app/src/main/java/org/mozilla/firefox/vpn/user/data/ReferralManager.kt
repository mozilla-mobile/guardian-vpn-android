package org.mozilla.firefox.vpn.user.data

import android.content.Context
import android.content.SharedPreferences
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import org.mozilla.firefox.vpn.util.BuildConfigExt
import org.mozilla.firefox.vpn.util.GLog

class ReferralManager(context: Context, private val prefs: SharedPreferences) {

    companion object {
        private const val TAG = "ReferralManager"
        private const val SIR_PREF_REFERRAL = "sir_pref_referral"
        private const val DEFAULT_REFERRAL = ""
    }

    private var referrerClient: InstallReferrerClient =
        InstallReferrerClient.newBuilder(context).build()

    suspend fun getUserReferral() = suspendCancellableCoroutine<String> { cont ->
        if (!BuildConfigExt.isFlavorPreview() && !BuildConfigExt.isBuildTypeDebug()) {
            GLog.d(TAG, "skip referral info in production-build")
            if (cont.isActive) {
                cont.resume("")
                return@suspendCancellableCoroutine
            }
        }
        val cacheReferral = prefs.getString(SIR_PREF_REFERRAL, null)
        if (cacheReferral != null) {
            GLog.d(TAG, "referral already submitted")
            if (cont.isActive) {
                cont.resume(cacheReferral)
                return@suspendCancellableCoroutine
            }
        }
        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        val response: ReferrerDetails = referrerClient.installReferrer
                        val referrerUrl: String = response.installReferrer
                        prefs.edit().putString(SIR_PREF_REFERRAL, referrerUrl).apply()
                        if (cont.isActive) {
                            GLog.d(TAG, "onInstallReferrerSetupFinished")
                            cont.resume(referrerUrl)
                            return
                        }
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // API not available on the current Play Store app or there's no Play Store
                        GLog.w(TAG, "FEATURE_NOT_SUPPORTED")
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Connection couldn't be established.
                        GLog.w(TAG, "SERVICE_UNAVAILABLE")
                    }
                }
                if (cont.isActive) {
                    cont.resume(DEFAULT_REFERRAL)
                    return
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                if (cont.isActive) {
                    GLog.w(TAG, "onInstallReferrerServiceDisconnected")
                    cont.resume(DEFAULT_REFERRAL)
                    return
                }
            }
        })
    }
}
