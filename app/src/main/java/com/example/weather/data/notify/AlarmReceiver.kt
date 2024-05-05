package com.example.weather.data.notify

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            setRepeatingAlarm(
                context,
                0,  // Use a unique request code
                TimeUnit.DAYS.toMillis(1)
            )
            val runnerNotifier = RunnerNotifier(
                notificationManager,
                context, "it", "tomTemp"
            )
            runnerNotifier.showNotification()

        }


    }

    fun setRepeatingAlarm(context: Context, requestCode: Int, interval: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)  // Define your AlarmReceiver
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set a repeating alarm with the desired interval
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)  // 12 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            // If the current time is past 10 PM, set for the next day
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DATE, 1)  // Schedule for the next day
            }
        }


        // Set a repeating alarm with the desired interval
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,  // Use RTC_WAKEUP if you want the alarm to wake the device
            calendar.timeInMillis,
            interval,  // Repeat interval in milliseconds
            pendingIntent
        )
        Log.i("MyAlarmReceiver", "Repeating alarm set.")

    }
}
