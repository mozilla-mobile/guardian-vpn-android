package org.mozilla.firefox.vpn.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.main.MainActivity
import org.mozilla.firefox.vpn.onboarding.OnboardingActivity
import org.mozilla.firefox.vpn.util.viewModel

class SplashActivity : AppCompatActivity() {

    private val component by lazy {
        SplashComponentImpl(guardianComponent)
    }

    private val viewModel by viewModel { component.viewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.showOnboarding.observe(this, Observer {
            startActivity(OnboardingActivity.getStartIntent(this))
            finish()
        })

        viewModel.showLoggedOutOnboarding.observe(this, Observer {
            startActivity(OnboardingActivity.getLogoutIntent(this))
            finish()
        })

        viewModel.showMainPage.observe(this, Observer {
            startActivity(MainActivity.getStartIntent(this@SplashActivity))
            finish()
        })
    }
}
