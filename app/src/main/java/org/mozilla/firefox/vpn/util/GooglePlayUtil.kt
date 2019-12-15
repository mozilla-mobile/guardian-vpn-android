package org.mozilla.firefox.vpn.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object GooglePlayUtil {

    fun launchPlayStore(context: Context) {
        val intent = try {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=${context.packageName}")
            )
        } catch (e: ActivityNotFoundException) {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            )
        }
        context.startActivity(intent)
    }
}
