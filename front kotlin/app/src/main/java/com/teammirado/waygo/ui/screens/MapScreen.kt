package com.teammirado.waygo.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.firestore.FirebaseFirestore
import com.teammirado.waygo.data.service.LocationService
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@SuppressLint("MissingPermission")
@Composable
fun RealTimeTrackingMap(
    tripId: Int,
    isDriver: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val mapView = remember { MapView(context) }
    val driverMarker = remember { Marker(mapView) }
    
    var driverLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // Démarrage/Arrêt du service de localisation pour le conducteur
    if (isDriver) {
        DisposableEffect(tripId) {
            val intent = Intent(context, LocationService::class.java).apply {
                putExtra("TRIP_ID", tripId.toString())
            }
            context.startForegroundService(intent)
            
            onDispose {
                val stopIntent = Intent(context, LocationService::class.java).apply {
                    action = "STOP"
                }
                context.startService(stopIntent)
            }
        }
    }

    // Écoute des changements de position sur Firestore (pour tout le monde)
    LaunchedEffect(tripId) {
        db.collection("trips_location").document(tripId.toString())
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val lat = snapshot.getDouble("lat") ?: 0.0
                    val lng = snapshot.getDouble("lng") ?: 0.0
                    driverLocation = GeoPoint(lat, lng)
                }
            }
    }

    AndroidView(
        factory = {
            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(16.0)
                overlays.add(driverMarker)
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            driverLocation?.let {
                driverMarker.position = it
                driverMarker.title = "Conducteur"
                view.controller.animateTo(it)
            }
        }
    )
}
