package com.teammirado.waygo.data.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var tripId: String? = null
    private val db = FirebaseFirestore.getInstance()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            tripId?.let { id ->
                val data = mapOf(
                    "lat" to location.latitude,
                    "lng" to location.longitude,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("trips_location").document(id).set(data)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        if (action == "STOP") {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        tripId = intent?.getStringExtra("TRIP_ID")
        startLocationUpdates()
        createNotification()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun createNotification() {
        val channelId = "location_channel"
        val manager = getSystemService(NotificationManager::class.java)
        
        val channel = NotificationChannel(
            channelId, 
            "Suivi Trajet", 
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("WayGo - Suivi en cours")
            .setContentText("Votre position est partagée avec les passagers.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        super.onDestroy()
    }
}
