package org.mozilla.guardian.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.guardian.R
import org.mozilla.guardian.device.data.DeviceRepository
import org.mozilla.guardian.device.domain.AddDeviceUseCase
import org.mozilla.guardian.user.data.Result
import org.mozilla.guardian.user.data.UserRepository

class MainActivity : AppCompatActivity() {

    private val addDevice: AddDeviceUseCase by lazy {
        AddDeviceUseCase(
            DeviceRepository(applicationContext),
            UserRepository(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
    }

    companion object {
        private const val TAG = "MainActivity"

        fun getStartIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
