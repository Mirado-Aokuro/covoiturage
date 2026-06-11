package com.teammirado.waygo.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teammirado.waygo.MainActivity
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.local.TokenManager
import com.teammirado.waygo.data.model.FcmTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val tokenManager = TokenManager(applicationContext)
        val userAuthToken = tokenManager.getToken()
        
        if (userAuthToken != null) {
            RetrofitClient.authToken = userAuthToken
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.apiService.updateFcmToken(FcmTokenRequest(token))
                } catch (e: Exception) {
                    Log.e("FCM", "Erreur envoi token: ${e.message}")
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "WayGo"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        
        // On récupère le type d'écran cible depuis les données (payload)
        val targetScreen = remoteMessage.data["target_screen"] ?: "home"
        val tripId = remoteMessage.data["tripId"]
        val passengerId = remoteMessage.data["passengerId"]
        val isDriver = remoteMessage.data["isDriver"] == "true"

        sendNotification(title, body, targetScreen, tripId, passengerId, isDriver)
    }

    private fun sendNotification(
        title: String, 
        messageBody: String, 
        targetScreen: String,
        tripId: String?, 
        passengerId: String?,
        isDriver: Boolean
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("target_screen", targetScreen)
            putExtra("tripId", tripId)
            putExtra("passengerId", passengerId)
            putExtra("isDriver", isDriver)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "waygo_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Notifications WayGo",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Canal pour les réservations et messages"
        }
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
