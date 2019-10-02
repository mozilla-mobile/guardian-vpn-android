package org.mozilla.guardian.splash

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*

import org.mozilla.guardian.R
import org.mozilla.guardian.main.MainActivity
import org.mozilla.guardian.onboarding.OnboardingActivity
import org.mozilla.guardian.user.data.UserRepository

class SplashActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splash_root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        userRepository = UserRepository(applicationContext)

        userRepository.getToken()?.let {
            startActivity(MainActivity.getStartIntent(this))
        } ?: run {
            startActivity(OnboardingActivity.getStartIntent(this))
        }

        finish()
    }
}
