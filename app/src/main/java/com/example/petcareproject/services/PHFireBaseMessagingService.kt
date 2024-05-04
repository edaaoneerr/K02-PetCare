package com.example.petcareproject.services

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.petcareproject.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class PHFirebaseMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle FCM messages here. E.g., create a notification:
        println("PHFirebaseMessagingService")
        println(remoteMessage)
        val channelID = "Default Channel"  // Make sure this is the same ID used everywhere
        val channelName = "Human Readable Name"
        if (remoteMessage.notification != null) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(this, channelID)
                .setContentTitle(remoteMessage.notification!!.title)
                .setContentText(remoteMessage.notification!!.body)
                .setSmallIcon(R.drawable.ic_notification)  // Ensure you have this icon in your drawable resources
                .build()
            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Log and optionally send token to your app server.
        println("FCM" + "New token: $token")

    }


}

