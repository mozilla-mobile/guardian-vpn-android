package org.mozilla.firefox.vpn

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestGuardianApp::class, sdk = [WORKING_SDK])
class EncryptedSharedPreferencesTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val encryptedPrefs =
        EncryptedSharedPreferences.create(
            "testPrefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    @Test
    fun `verify string storage`() {
        encryptedPrefs.edit()
            .putString("key", "value")
            .apply()

        assertEquals("value", encryptedPrefs.getString("key", "default"))
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            FakeAndroidKeyStore.setup
        }
    }
}
