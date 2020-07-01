package org.mozilla.firefox.vpn.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.main.MainActivity

object NotificationUtil {

    enum class Channel {
        MEDIUM, URGENT
    }

    private const val MEDIUM_CHANNEL_ID = "medium_channel_id"
    private const val URGENT_CHANNEL_ID = "urgent_channel_id"
    const val DEFAULT_NOTIFICATION_ID = 1000

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context, MEDIUM_CHANNEL_ID, R.string.windows_notification_channel_medium, NotificationManager.IMPORTANCE_LOW)
            createChannel(context, URGENT_CHANNEL_ID, R.string.windows_notification_channel_urgent, NotificationManager.IMPORTANCE_HIGH)
        }
    }

    fun sendNotification(context: Context, builder: NotificationCompat.Builder) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, builder.build())
    }

    fun createBaseBuilder(context: Context, channel: Channel = Channel.MEDIUM): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, getChannelD(channel))
            .setSmallIcon(R.drawable.ic_fpn_white)
            .setColor(context.color(android.R.color.black))
            .setAutoCancel(true)
            .setShowWhen(false)
            .setContentIntent(PendingIntent.getActivity(context, 0, MainActivity.getStartIntent(context), PendingIntent.FLAG_CANCEL_CURRENT))
    }

    fun createImportantBuilder(context: Context): NotificationCompat.Builder {
        return createBaseBuilder(context, Channel.URGENT)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannel(context: Context, channelId: String, channelNameResId: Int, importance: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, context.getString(channelNameResId), importance)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

    private fun getChannelD(channel: Channel): String {
        return when (channel) {
            Channel.MEDIUM -> MEDIUM_CHANNEL_ID
            Channel.URGENT -> URGENT_CHANNEL_ID
        }
    }
}
