package org.mozilla.firefox.vpn.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavController
import kotlinx.android.synthetic.main.activity_main.force_update
import kotlinx.android.synthetic.main.activity_main.nav_view
import kotlinx.android.synthetic.main.view_force_update.description
import kotlinx.android.synthetic.main.view_force_update.manage_btn
import kotlinx.android.synthetic.main.view_force_update.sign_out_btn
import kotlinx.android.synthetic.main.view_force_update.update_btn
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.onboarding.OnboardingActivity
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.util.GooglePlayUtil
import org.mozilla.firefox.vpn.util.launchUrl
import org.mozilla.firefox.vpn.util.viewModel

class MainActivity : AppCompatActivity() {

    private val component by lazy {
        MainComponentImpl(guardianComponent)
    }

    private val viewModel by viewModel {
        component.viewModel
    }

    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState

        viewModel.lockSetting.observe(this, Observer {
            nav_view.setItemLocked(R.id.settings, it)
        })

        viewModel.vpnIcon.observe(this, Observer {
            updateVpnIcon(it)
        })

        viewModel.launchOnboardingPage.observe(this, Observer {
            val intent = if (it.showLogoutMessage) {
                OnboardingActivity.getLogoutIntent(this)
            } else {
                OnboardingActivity.getStartIntent(this@MainActivity)
            }
            startActivity(intent)
            finish()
        })

        force_update.apply {
            description.text = getString(
                R.string.update_content_1,
                getString(R.string.application_name)
            )

            update_btn.setOnClickListener {
                GooglePlayUtil.launchPlayStore(this@MainActivity)
            }

            manage_btn.setOnClickListener {
                lifecycle.coroutineScope.launch {
                    launchUrl(GuardianService.HOST_FXA)
                }
            }

            sign_out_btn.setOnClickListener {
                viewModel.signOut()
            }
        }

        viewModel.showForceUpdate.observe(this, Observer {
            force_update.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
    }

    private fun updateVpnIcon(resId: Int) {
        nav_view.menu.findItem(R.id.vpn).icon = ContextCompat.getDrawable(this, resId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    override fun onBackPressed() {
        if (nav_view.isLocked(R.id.settings) && isOnMainSetting()) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    private fun isOnMainSetting(): Boolean {
        return currentNavController?.value?.currentDestination?.id == R.id.settings_main
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val navGraphIds = listOf(R.navigation.vpn, R.navigation.settings)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = nav_view.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    companion object {
        fun getStartIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}

fun FragmentActivity.setSupportActionBar(toolbar: Toolbar): ActionBar? {
    return (this as? AppCompatActivity)?.let {
        it.setSupportActionBar(toolbar)
        it.supportActionBar
    }
}

fun FragmentActivity.getSupportActionBar(): ActionBar? {
    return (this as? AppCompatActivity)?.supportActionBar
}
