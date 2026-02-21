package com.ec.keepalive.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ec.keepalive.R
import kotlin.random.Random
import androidx.core.net.toUri

object NotificationHelper {
    private const val CHANNEL_ID = "keepalive_reminders"
    private const val CHANNEL_NAME = "KeepAlive Reminder"
    private const val SUMMARY_ID = Int.MAX_VALUE

    private const val GROUP_KEY = "KEEPALIVE_REMINDERS_GROUP"

    private fun createNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
                CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }
    fun sendNotification(context: Context, title: String, message: String, id: Int, phone: String?) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.foreground_large)
            .setContentTitle(title) // Title is contact name, as desired
            .setContentText(message) // Message is generic reminder, as desired
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(GROUP_KEY)
            .setAutoCancel(true)

        if(phone != null) {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.fromParts("tel", phone, null)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                id,
                dialIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.setContentIntent(pendingIntent)
        }

        val notification = builder.build()

        notificationManager.notify(id, notification)

        KLog.d("Push Notification Alert! $id")
    }

    fun sendNotificationSummary(context: Context, count: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(context)

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(context.getString(R.string.notify_summary_title))
            .setSummaryText(context.getString(R.string.notify_summary_text,count ))

        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.foreground_large)
            .setContentTitle(context.getString(R.string.notify_summary_title))
            .setContentText(context.getString(R.string.notify_summary_text, count))
            .setStyle(inboxStyle)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(SUMMARY_ID, summaryNotification)

        KLog.d("Push Notification Summary Alert! $SUMMARY_ID")

    }
}