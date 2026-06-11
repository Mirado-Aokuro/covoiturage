package com.teammirado.waygo.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun HomeScreen(
    userRole: String? = null,
    departureFilter: String? = null,
    arrivalFilter: String? = null,
    dateFilter: String? = null,
    driverFilter: String? = null,
    onTripClick: (Trip) -> Unit, 
    onPublishClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChatClick: (Int) -> Unit
) {
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    // Animation pour l'apparition de la liste
    val listVisible = remember { mutableStateOf(false) }

    LaunchedEffect(departureFilter, arrivalFilter, dateFilter, driverFilter) {
        isLoading = true
        listVisible.value = false
        try {
            trips = RetrofitClient.apiService.getTrips(departureFilter, arrivalFilter, dateFilter, driverFilter)
            listVisible.value = true
        } catch (e: Exception) {
            errorMessage = "Oups, petit souci réseau..."
        } finally {
            isLoading = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) mapView.onResume()
            else if (event == Lifecycle.Event.ON_PAUSE) mapView.onPause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
        // La Carte en fond
        AndroidView<MapView>(
            factory = { 
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(6.0)
                    controller.setCenter(GeoPoint(-18.8792, 47.5079))
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.overlays.removeAll(view.overlays.filterIsInstance<Marker>())
                trips.forEach { trip ->
                    val lat = trip.departureLatitude
                    val lng = trip.departureLongitude
                    if (lat != null && lng != null) {
                        val marker = Marker(view)
                        marker.position = GeoPoint(lat, lng)
                        marker.title = trip.departureCity
                        marker.setOnMarkerClickListener { _, _ ->
                            onTripClick(trip)
                            true
                        }
                        view.overlays.add(marker)
                    }
                }
            }
        )

        // Overlay Design
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopBar()
            
            if (userRole != "driver") {
                HomeSearchBar(departureFilter, arrivalFilter, onSearchClick)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Panneau des trajets animé
            AnimatedVisibility(
                visible = listVisible.value || isLoading,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                SuggestedTripsSection(
                    title = if (!departureFilter.isNullOrBlank()) "Résultats de recherche" else "Trajets du moment",
                    trips = trips,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onTripClick = onTripClick,
                    onChatClick = onChatClick
                )
            }
        }

        if (userRole == "driver") {
            FloatingActionButton(
                onClick = onPublishClick,
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).padding(bottom = 80.dp),
                containerColor = Color(0xFF003399),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Publier un trajet", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun HomeTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.0f))))
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(44.dp).shadow(4.dp, CircleShape),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF003399))
            ) {
                AsyncImage(
                    model = "https://i.pravatar.cc/100",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = "WayGo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF003399),
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = { },
                modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
            ) {
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
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 6.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = Color(0xFF003399))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (!dep.isNullOrBlank()) "$dep → $arr" else "Où souhaitez-vous aller ?",
                style = MaterialTheme.typography.bodyLarge,
                color = if (!dep.isNullOrBlank()) Color.Black else Color.Gray,
                modifier = Modifier.weight(1f)
            )
            Box(Modifier.size(32.dp).background(Color(0xFFF0F4FF), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Tune, null, tint = Color(0xFF003399), modifier = Modifier.size(18.dp))
            }
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
    onChatClick: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = Color.White,
        tonalElevation = 4.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .width(40.dp).height(5.dp)
                    .background(Color.LightGray.copy(alpha = 0.4f), CircleShape)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(24.dp)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF003399), strokeWidth = 5.dp)
                }
            } else if (trips.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucun trajet trouvé pour le moment", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(trips) { trip -> 
                        HomeTripItem(
                            trip = trip, 
                            onClick = { onTripClick(trip) },
                            onChatClick = { onChatClick(trip.id) }
                        ) 
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTripItem(trip: Trip, onClick: () -> Unit, onChatClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFBFBFF)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = "https://i.pravatar.cc/150?u=${trip.driverId}",
                    contentDescription = null,
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(trip.driverName ?: "Conducteur", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                        Text(" ${trip.driverRating ?: 5.0} • ${trip.seatsAvailable ?: 1} places", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
                IconButton(
                    onClick = { onChatClick() },
                    modifier = Modifier.background(Color(0xFFF0F4FF), CircleShape).size(36.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color(0xFF003399), modifier = Modifier.size(18.dp))
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("${trip.departureCity}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(14.dp).padding(vertical = 2.dp), tint = Color.LightGray)
                    Text("${trip.arrivalCity}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                
                Surface(
                    color = Color(0xFF007A33),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${trip.price} €",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
