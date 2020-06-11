package org.mozilla.firefox.vpn.util

import android.content.SharedPreferences
import androidx.core.content.edit

fun SharedPreferences.putStringSetSafe(key: String, value: Set<String>) {
    this.edit {
        remove(key)
        apply()
        putStringSet(key, value)
    }
}
