package org.mozilla.firefox.vpn

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

interface CoreComponent {
    val app: Application
    val prefs: SharedPreferences
    val encryptedPrefs: SharedPreferences
}

class CoreComponentImpl(
    override val app: Application
) : CoreComponent {

    override val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(app)
    }

    override val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            app.packageName + "_encrypted_preferences",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            app,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
