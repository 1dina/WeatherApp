package com.example.weather.data.notify

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.weather.R
import com.example.weather.ui.main.MainActivity

class RunnerNotifier(
    private val notificationManager: NotificationManager,
    private val context: Context,
    private val cityName: String,
    private val tomTemp: String
) : Notifier(notificationManager) {

    override val notificationChannelId: String = "runner_channel_id"
    override val notificationChannelName: String = "Running Notification"
    override val notificationId: Int = 200
    override fun buildNotification(): Notification {

        return NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle(getNotificationTitle()).setContentText(getNotificationMessage())
            .setSmallIcon(R.drawable.baseline_cloud_24)
            .setContentIntent(
                PendingIntent.getActivity(
                    context, // Context from onReceive method.
                    0,
                    Intent(
                        context,
                        MainActivity::class.java
                    ), // Activity you want to launch onClick.
                    PendingIntent.FLAG_IMMUTABLE
                )
            ).setAutoCancel(true).build()
    }

    override fun getNotificationTitle(): String {
        return "Attention!! "
    }

    override fun getNotificationMessage(): String {

        return "Don't forget to check weather today :) "
    }
}