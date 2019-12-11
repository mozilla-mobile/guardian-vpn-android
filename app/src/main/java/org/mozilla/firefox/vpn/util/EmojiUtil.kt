package org.mozilla.firefox.vpn.util

import android.content.Context
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import java.lang.Exception
import java.util.Locale

object EmojiUtil {

    fun init(context: Context) {
        val config = BundledEmojiCompatConfig(context)
        EmojiCompat.init(config)
    }

    fun loadEmoji(codePoint: String): CharSequence {
        return try {
            EmojiCompat.get().process(codePoint)
        } catch (e: Exception) {
            ""
        }
    }

    fun getCountryFlagCodePoint(countryCode: String): String {
        val countryLetter = countryCode.toUpperCase(Locale.getDefault())
        val firstLetter = Character.codePointAt(countryLetter, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(countryLetter, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }
}
