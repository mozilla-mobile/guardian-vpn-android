package org.mozilla.firefox.vpn.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.UserStates
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.isDeviceLimitReached

class MainActivity : AppCompatActivity() {

    private var currentNavController: LiveData<NavController>? = null

    private val userStates: UserStates by lazy {
        UserStates(guardianComponent.userStateResolver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState

        userStates.stateObservable.observe(this, Observer {
            nav_view.setItemLocked(R.id.settings, it.isDeviceLimitReached())
        })
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
