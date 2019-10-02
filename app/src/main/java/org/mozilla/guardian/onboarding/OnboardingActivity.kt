package org.mozilla.guardian.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.guardian.R

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
    }

    companion object {
        fun getStartIntent(context: Context) = Intent(context, OnboardingActivity::class.java)
    }
}
