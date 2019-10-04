package org.mozilla.firefox.vpn.onboarding

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.device.domain.AddDeviceUseCase
import org.mozilla.firefox.vpn.main.MainActivity
import org.mozilla.firefox.vpn.user.data.LoginResult
import org.mozilla.firefox.vpn.user.data.Result
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.user.domain.GetLoginInfoUseCase
import org.mozilla.firefox.vpn.user.domain.VerifyLoginUseCase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OnboardingActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository

    private lateinit var getLoginInfo: GetLoginInfoUseCase
    private lateinit var verifyLogin: VerifyLoginUseCase

    private val addDevice: AddDeviceUseCase by lazy {
        AddDeviceUseCase(
            DeviceRepository(applicationContext),
            userRepository
        )
    }

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
            is Result.Fail -> Toast.makeText(this, "${verifyResult.exception}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processLoginResult(loginResult: LoginResult) {

        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                addDevice()
            }
            when (result) {
                is Result.Success -> Log.d(TAG, "add device ${result.value}")
                is Result.Fail -> Log.d(TAG, "add device failed: ${result.exception}")
            }

            // TODO: Better way to clear custom tab
            startActivity(getStartIntent(this@OnboardingActivity).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }
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
        private const val TAG = "OnboardingActivity"
        fun getStartIntent(context: Context) = Intent(context, OnboardingActivity::class.java)
    }
}
