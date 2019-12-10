package org.mozilla.firefox.vpn.util

import android.content.Context
import android.content.res.ColorStateList
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.util.Locale

class StringResource {
    var id: Int? = null
    var str: String? = null

    constructor(str: String) {
        this.str = str
    }

    constructor(id: Int) {
        this.id = id
    }

    fun resolve(context: Context): String? {
        return str?.let { it } ?: id?.let { context.getString(it).apply { str = this } }
    }
}

fun Context.color(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

fun Context.colorStateList(@ColorRes id: Int): ColorStateList? {
    return ContextCompat.getColorStateList(this, id)
}

fun Context.getCountryFlag(countryCode: String): Int {
    val countryLetter = countryCode.toUpperCase(Locale.getDefault())
    val firstLetter = Character.codePointAt(countryLetter, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryLetter, 1) - 0x41 + 0x1F1E6
    val name = "ic_${Integer.toHexString(firstLetter)}_${Integer.toHexString(secondLetter)}"
    return this.resources.getIdentifier(name, "drawable", this.packageName)
}
