package org.mozilla.firefox.vpn.splash

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.data.DeviceRepository
import org.mozilla.firefox.vpn.main.MainActivity
import org.mozilla.firefox.vpn.main.domain.AppState
import org.mozilla.firefox.vpn.main.domain.AppStateUseCase
import org.mozilla.firefox.vpn.onboarding.OnboardingActivity
import org.mozilla.firefox.vpn.user.data.UserRepository

class SplashActivity : AppCompatActivity() {

    private val getAppState: AppStateUseCase by lazy {
        AppStateUseCase(UserRepository(applicationContext), DeviceRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splash_root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        when (getAppState()) {
            AppState.Login -> startActivity(OnboardingActivity.getStartIntent(this))
            else -> startActivity(MainActivity.getStartIntent(this))
        }

        finish()
    }
}
