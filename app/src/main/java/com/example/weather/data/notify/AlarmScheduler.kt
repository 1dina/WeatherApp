package com.example.weather.data.notify

import android.app.PendingIntent

interface AlarmScheduler {
    fun createPendingIntent(reminderItem: ReminderItem): PendingIntent

    fun schedule(reminderItem: ReminderItem)

    fun cancel(reminderItem: ReminderItem)
}