package org.mozilla.firefox.vpn.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.isDeviceLimitReached
import org.mozilla.firefox.vpn.main.vpn.domain.VpnManagerStateProvider
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.main.vpn.domain.VpnStateProvider
import org.mozilla.firefox.vpn.*
import org.mozilla.firefox.vpn.onboarding.OnboardingActivity

class MainActivity : AppCompatActivity() {

    private var currentNavController: LiveData<NavController>? = null

    private val userStates: UserStates by lazy {
        UserStates(guardianComponent.userStateResolver)
    }

    private val vpnStateProvider: VpnStateProvider by lazy {
        VpnManagerStateProvider(guardianComponent.vpnManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState

        userStates.stateObservable.observe(this, Observer {
            if (it is UserState.Login) {
                startActivity(OnboardingActivity.getLogoutIntent(this))
                finish()
            } else {
                nav_view.setItemLocked(R.id.settings, it.isDeviceLimitReached())
            }
        })

        vpnStateProvider.stateObservable.observe(this, Observer {
            when (it) {
                VpnState.Connected -> updateVpnIcon(R.drawable.ic_vpn_connected)
                VpnState.Disconnected -> updateVpnIcon(R.drawable.ic_vpn_disconnected)
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
