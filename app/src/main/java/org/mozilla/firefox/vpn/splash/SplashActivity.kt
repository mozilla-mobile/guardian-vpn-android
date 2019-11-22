package org.mozilla.firefox.vpn.splash

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*
import org.mozilla.firefox.vpn.*
import org.mozilla.firefox.vpn.main.MainActivity
import org.mozilla.firefox.vpn.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {

    private val userStates: UserStates by lazy {
        UserStates(guardianComponent.userStateResolver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splash_root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        when (userStates.state) {
            UserState.Login -> startActivity(OnboardingActivity.getStartIntent(this))
            else -> startActivity(MainActivity.getStartIntent(this))
        }

        finish()
    }
}
