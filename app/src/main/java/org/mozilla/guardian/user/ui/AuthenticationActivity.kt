package org.mozilla.guardian.user.ui

import android.content.ComponentName
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.mozilla.guardian.user.data.LoginInfo
import org.mozilla.guardian.user.data.UserRepository
import org.mozilla.guardian.user.domain.VerifyLoginUseCase
import org.mozilla.guardian.user.domain.GetLoginInfoUseCase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * This activity is just for demonstrating the authentication flow
 */
class AuthenticationActivity : AppCompatActivity() {

    private val userRepository = UserRepository()

    private val getLoginInfo = GetLoginInfoUseCase(userRepository)
    private val verifyLogin = VerifyLoginUseCase(userRepository)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // this should be called after
        GlobalScope.launch(Dispatchers.IO) {
            // this should be called as early as possible
            prepareCustomTab()

            // this should be called after prepareCustomTab is finished
            getLoginInfo().let { info ->
                launchLoginCustomTab(info.loginUrl)
                verifyLogin(info)
            }
        }
    }

    private suspend fun prepareCustomTab(): Unit = suspendCoroutine { cont ->
        CustomTabsClient.bindCustomTabsService(
            this@AuthenticationActivity,
            "com.android.chrome",
            object : CustomTabsServiceConnection() {

                override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                    client.warmup(0L)
                    cont.resume(Unit)
                }

                override fun onServiceDisconnected(p0: ComponentName) {}
            }
        )
    }

    private fun launchLoginCustomTab(url: String) {
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(this, Uri.parse(url))
    }
}
