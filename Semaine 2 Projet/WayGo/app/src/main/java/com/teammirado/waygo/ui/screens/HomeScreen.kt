package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    departureFilter: String? = null,
    arrivalFilter: String? = null,
    dateFilter: String? = null,
    onTripClick: (Trip) -> Unit, 
    onPublishClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(48.8566, 2.3522), 5f)
    }

    LaunchedEffect(departureFilter, arrivalFilter, dateFilter) {
        isLoading = true
        errorMessage = null
        try {
            trips = RetrofitClient.apiService.getTrips(departureFilter, arrivalFilter, dateFilter)
            if (trips.isNotEmpty() && trips[0].departureLatitude != null && trips[0].departureLongitude != null) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(trips[0].departureLatitude!!, trips[0].departureLongitude!!), 8f
                )
            }
        } catch (e: Exception) {
            errorMessage = "Erreur connexion : ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            trips.forEach { trip ->
                val lat = trip.departureLatitude
                val lng = trip.departureLongitude
                if (lat != null && lng != null) {
                    Marker(
                        state = MarkerState(position = LatLng(lat, lng)),
                        title = "${trip.departure} → ${trip.arrival}",
                        snippet = "${trip.price} €",
                        onInfoWindowClick = { onTripClick(trip) }
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopBar()
            Spacer(modifier = Modifier.height(16.dp))
            HomeSearchBar(departureFilter, arrivalFilter, onSearchClick)
            Spacer(modifier = Modifier.weight(1f))
            
            SuggestedTripsSection(
                title = if (departureFilter != null) "Résultats" else "Trajets suggérés",
                trips = trips,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onTripClick = onTripClick,
                onRetry = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            trips = RetrofitClient.apiService.getTrips(departureFilter, arrivalFilter, dateFilter)
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        }

        FloatingActionButton(
            onClick = onPublishClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp),
            containerColor = Color(0xFF003399),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, null)
        }
    }
}

@Composable
fun HomeTopBar() {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray))
            Text(
                text = "CoVoiturage", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold, 
                color = Color(0xFF003399)
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.Notifications, null, tint = Color(0xFF003399))
            }
        }
    }
}

@Composable
fun HomeSearchBar(dep: String?, arr: String?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (dep != null) "$dep → $arr" else "Où allez-vous ?",
                color = if (dep != null) Color.Black else Color.Gray,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.Tune, null, tint = Color.Gray)
        }
    }
}

@Composable
fun SuggestedTripsSection(
    title: String,
    trips: List<Trip>,
    isLoading: Boolean,
    errorMessage: String?,
    onTripClick: (Trip) -> Unit,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.55f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Box(Modifier.width(40.dp).height(4.dp).background(Color.LightGray, CircleShape).align(Alignment.CenterHorizontally))
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF003399))
                }
            } else if (errorMessage != null) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(errorMessage, color = Color.Red)
                    TextButton(onClick = onRetry) { Text("Réessayer") }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(trips) { trip -> HomeTripItem(trip, onClick = { onTripClick(trip) }) }
                }
            }
        }
    }
}

@Composable
fun HomeTripItem(trip: Trip, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F9FF),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray)) {
                        AsyncImage(
                            model = "https://i.pravatar.cc/150?u=${trip.driverName}",
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(trip.driverName, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Text(" ${trip.driverRating}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                        }
                    }
                }
                Text(
                    text = "${trip.price} €",
                    fontWeight = FontWeight.Bold, 
                    color = Color(0xFF007A33), 
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("${trip.departure} → ${trip.arrival}", style = MaterialTheme.typography.bodyLarge)
            
            if (trip.amenities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    trip.amenities.take(3).forEach { amenity ->
                        AmenityTag(text = amenity)
                    }
                }
            }
        }
    }
}

@Composable
fun AmenityTag(text: String) {
    Surface(color = Color(0xFFE8EAF6), shape = RoundedCornerShape(8.dp)) {
        Text(text = text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall, color = Color(0xFF3F51B5))
    }
}
