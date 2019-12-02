package org.mozilla.firefox.vpn.util

import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable.tint(color: Int): Drawable {
    return DrawableCompat.wrap(this.mutate()).apply {
        DrawableCompat.setTint(this, color)
    }
}
