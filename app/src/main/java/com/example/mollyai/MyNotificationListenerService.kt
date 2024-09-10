package com.example.mollyai

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MyNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Called when a new notification is posted
        Log.d("NotificationListener", "Notification Posted from: ${sbn.packageName}")

        // Get notification details
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")

        Log.d("NotificationListener", "Title: $title, Text: $text")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Called when a notification is removed
        Log.d("NotificationListener", "Notification Removed from: ${sbn.packageName}")
    }
}
