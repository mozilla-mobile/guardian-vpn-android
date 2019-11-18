package org.mozilla.firefox.vpn.util

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import java.lang.Exception
import java.util.*

object EmojiUtil {
    private var isInit = false
    private var mTypeface: Typeface? = null

    fun initEmoji(context: Context) {
        val config = BundledEmojiCompatConfig(context)
        EmojiCompat.init(config)

        try {
            mTypeface = Typeface.createFromAsset(context.assets, "TwemojiMozilla.ttf")
        } catch (e: Exception) {
        }
        isInit = true
    }

    fun process(emoji: String): CharSequence {
        if (!isInit) {
            return ""
        }
        return try {
            EmojiCompat.get().process(emoji)
        } catch (e: Exception) {
            ""
        }
    }

    fun loadEmoji(textView: TextView, emoji: String) {
        if (!isInit) {
            return
        }
        try {
            val emojiChar = process(emoji)
            if (mTypeface != null) {
                textView.typeface = mTypeface
            }
            textView.text = emojiChar
        } catch (e: Exception) {
        }
    }

    fun localeEmoji(countryCode: String): String {
        val countryLetter = countryCode.toUpperCase(Locale.getDefault())
        val firstLetter = Character.codePointAt(countryLetter, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(countryLetter, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }
}