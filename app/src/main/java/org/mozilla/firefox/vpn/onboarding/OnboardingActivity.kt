package org.mozilla.firefox.vpn.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.main.MainActivity
import org.mozilla.firefox.vpn.util.LoginCustomTab
import org.mozilla.firefox.vpn.util.observerUntilOnDestroy
import org.mozilla.firefox.vpn.util.viewModel

class OnboardingActivity : AppCompatActivity() {

    private val component by lazy {
        OnboardingComponentImpl(guardianComponent)
    }

    private val viewModel by viewModel { component.viewModel }

    private var shouldLaunchMainPage = false

    private lateinit var customTab: LoginCustomTab

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        customTab = LoginCustomTab(this)

        viewModel.prepareLoginFlow()

        viewModel.loginInfo.observe(this, Observer {
            customTab.mayLaunchUrl(it.loginUrl)
        })

        viewModel.toast.observe(this, Observer {
            Toast.makeText(this, it.resolve(this), Toast.LENGTH_SHORT).show()
        })

        viewModel.promptLogin.observe(this, Observer {
            customTab.launchUrl(it)
        })

        viewModel.launchMainPage.observerUntilOnDestroy(this, Observer {
            // Originally, after receiving this event, we should launch main activity directly. However,
            // this will make custom tab being pushed to the background, and after the user leaves the
            // main activity, he will see the custom tab, which is undesired.

            // To close custom tab first, current approach launches OnboardingActivity again with
            // FLAG_ACTIVITY_CLEAR_TOP to clear the custom tab, and since onNewIntent() will be called
            // in this case, we launch main activity there

            shouldLaunchMainPage = true
            startActivity(getStartIntent(this@OnboardingActivity).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (shouldLaunchMainPage) {
            startActivity(MainActivity.getStartIntent(this))
            finish()
        }
    }

    fun startLoginFlow() {
        viewModel.startLoginFlow()
    }

    companion object {
        fun getStartIntent(context: Context) = Intent(context, OnboardingActivity::class.java)
    }
}
