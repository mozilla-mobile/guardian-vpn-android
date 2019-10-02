package org.mozilla.guardian.onboarding

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.mozilla.guardian.R
import org.mozilla.guardian.main.MainActivity
import org.mozilla.guardian.user.data.LoginResult
import org.mozilla.guardian.user.data.Result
import org.mozilla.guardian.user.data.UserRepository
import org.mozilla.guardian.user.domain.GetLoginInfoUseCase
import org.mozilla.guardian.user.domain.VerifyLoginUseCase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OnboardingActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository

    private lateinit var getLoginInfo: GetLoginInfoUseCase
    private lateinit var verifyLogin: VerifyLoginUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // TODO: Do not instantiate directly
        userRepository = UserRepository(applicationContext)
        getLoginInfo = GetLoginInfoUseCase(userRepository)
        verifyLogin = VerifyLoginUseCase(userRepository)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        userRepository.getToken()?.let {
            startActivity(MainActivity.getStartIntent(this))
            finish()
        }
    }

    fun startLoginFlow() {
        // TODO: 1. Do not use GlobalScope
        // TODO: 2. Performance tuning
        GlobalScope.launch(Dispatchers.IO) {
            prepareCustomTab()

            val info = getLoginInfo()
            launchLoginCustomTab(info.loginUrl)
            processVerifyResult(verifyLogin(info))
        }
    }

    private fun processVerifyResult(verifyResult: Result<LoginResult>) {
        when (verifyResult) {
            is Result.Success -> processLoginResult(verifyResult.value)
            is Result.Fail -> Toast.makeText(
                this,
                "${verifyResult.exception}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun processLoginResult(loginResult: LoginResult) {
        // TODO: Better way to clear custom tab
        startActivity(getStartIntent(this).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
    }

    private suspend fun prepareCustomTab(): Unit = suspendCoroutine { cont ->
        CustomTabsClient.bindCustomTabsService(
            this,
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

    companion object {
        fun getStartIntent(context: Context) = Intent(context, OnboardingActivity::class.java)
    }
}
