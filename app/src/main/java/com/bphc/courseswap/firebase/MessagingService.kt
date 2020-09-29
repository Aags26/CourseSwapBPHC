package com.bphc.courseswap.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bphc.courseswap.MatchedUserActivity
import com.bphc.courseswap.R
import com.bphc.courseswap.app.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessagingService() : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("TOKEN", p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.d("FCM", p0.data.toString())
        sendNotification(p0)
    }

    private fun sendNotification(message: RemoteMessage) {

        val intent = Intent(this, MatchedUserActivity::class.java)
        intent.putExtra(Constants.USER_EMAIL, message.data[Constants.USER_EMAIL])
        intent.putExtra(Constants.USER_PHONE, message.data[Constants.USER_PHONE])
        intent.putExtra(Constants.USER_ASSIGNED, message.data[Constants.USER_ASSIGNED])
        intent.putExtra(Constants.USER_DESIRED, message.data[Constants.USER_DESIRED])

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = Constants.NOTIFICATION_CHANNEL_ID
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(message?.notification?.title)
            .setContentText(message?.notification?.body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

}