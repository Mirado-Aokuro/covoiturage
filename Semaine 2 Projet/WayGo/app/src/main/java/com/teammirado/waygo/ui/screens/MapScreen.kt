package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip

@Composable
fun MapScreen(onTripClick: (Trip) -> Unit) {
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val initialPos = LatLng(48.8566, 2.3522) // Paris par défaut
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 6f)
    }

    LaunchedEffect(Unit) {
        try {
            trips = RetrofitClient.apiService.getTrips()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                trips.forEach { trip ->
                    if (trip.departureLatitude != null && trip.departureLongitude != null) {
                        Marker(
                            state = MarkerState(position = LatLng(trip.departureLatitude, trip.departureLongitude)),
                            title = "${trip.departure} -> ${trip.arrival}",
                            snippet = "${trip.price} € - Par ${trip.driverName}",
                            onInfoWindowClick = { onTripClick(trip) }
                        )
                    }
                }
            }
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
