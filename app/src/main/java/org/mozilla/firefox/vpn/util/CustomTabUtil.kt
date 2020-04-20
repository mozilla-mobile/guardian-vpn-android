package org.mozilla.firefox.vpn.util

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

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
        val customTabCandidates = listOf(
            "org.mozilla.fenix.nightly",
            "org.mozilla.fenix",
            "org.mozilla.fennec_aurora",
            "org.mozilla.firefox_beta",
            "org.mozilla.firefox",
            "org.mozilla.focus",
            "com.android.chrome"
        )
    }
}

suspend fun Fragment.launchUrl(url: String): Boolean {
    return withContext(Dispatchers.Main) {
        val activity = activity ?: return@withContext false
        val session = initCustomTabService(activity) ?: return@withContext false
        launchUrl(activity, session, url)
        return@withContext true
    }
}

suspend fun Activity.launchUrl(url: String): Boolean {
    return withContext(Dispatchers.Main) {
        val session = initCustomTabService(this@launchUrl) ?: return@withContext false
        launchUrl(this@launchUrl, session, url)
        return@withContext true
    }
}

private suspend fun initCustomTabService(
    context: Context
) = suspendCancellableCoroutine<CustomTabsSession?> {
    val pkg = getTargetPackage(context) ?: return@suspendCancellableCoroutine it.resume(null)

    val isSuccess = CustomTabsClient.bindCustomTabsService(context, pkg, object : CustomTabsServiceConnection() {

        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            if (it.isActive) {
                client.warmup(0)
                it.resume(client.newSession(null))
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    })

    if (!isSuccess) {
        return@suspendCancellableCoroutine it.resume(null)
    }
}

private fun launchUrl(context: Context, session: CustomTabsSession, url: String) {
    CustomTabsIntent.Builder(session).build().launchUrl(context, Uri.parse(url))
}

private fun getTargetPackage(context: Context): String? {
    return CustomTabsClient.getPackageName(context, LoginCustomTab.customTabCandidates, true)
}
