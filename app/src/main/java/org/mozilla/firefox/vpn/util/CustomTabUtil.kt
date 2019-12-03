package org.mozilla.firefox.vpn.util

import android.content.ComponentName
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class LoginCustomTab(private val activity: AppCompatActivity) : DefaultLifecycleObserver {

    private var client: CustomTabsClient? = null
    private var session: CustomTabsSession? = null
    private var connection: CustomTabsServiceConnection? = null

    init {
        GLog.d(TAG, "observe lifecycle")
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        GLog.d(TAG, "onCreate: warmUp")
        warmUp()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        GLog.d(TAG, "onDestroy: unbind")
        connection?.let { activity.unbindService(it) }
        activity.lifecycle.removeObserver(this)
        super.onDestroy(owner)
    }

    fun mayLaunchUrl(url: String) {
        GLog.d(TAG, "prepare url: $url")
        session?.mayLaunchUrl(Uri.parse(url), null, null)
    }

    fun launchUrl(url: String) {
        GLog.d(TAG, "launchUrl: $url")
        CustomTabsIntent.Builder(session).build().launchUrl(activity, Uri.parse(url))
    }

    private fun warmUp() {
        val pkg = getTargetPackage() ?: return
        connection = connection ?: createConnection()
        GLog.d(TAG, "bindService: pkg=$pkg")
        CustomTabsClient.bindCustomTabsService(activity, pkg, connection!!)
    }

    private fun createConnection(): CustomTabsServiceConnection {
        return object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                GLog.d(TAG, "service connected")
                client.warmup(0)
                this@LoginCustomTab.session = client.newSession(null)
                this@LoginCustomTab.client = client
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                GLog.d(TAG, "service disconnected")
                this@LoginCustomTab.client = null
                this@LoginCustomTab.session = null
            }
        }
    }

    private fun getTargetPackage(): String? {
        return CustomTabsClient.getPackageName(activity, customTabCandidates, true)
    }

    companion object {
        private const val TAG = "LoginCustomTab"
        private val customTabCandidates = listOf("org.mozilla.fenix", "com.android.chrome")
    }
}

fun Fragment.launchUrl(url: String) {
    CustomTabsIntent.Builder().apply {
        enableUrlBarHiding()
    }.build().launchUrl(requireContext(), url.toUri())
}
