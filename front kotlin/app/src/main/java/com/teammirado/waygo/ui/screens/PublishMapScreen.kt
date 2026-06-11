package com.teammirado.waygo.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun PublishMapScreen(onPointsSelected: (Double, Double, Double, Double) -> Unit, onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var departurePos by remember { mutableStateOf<GeoPoint?>(null) }
    var arrivalPos by remember { mutableStateOf<GeoPoint?>(null) }
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    val blueColor = Color(0xFF003399)

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Itinéraire", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = blueColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            AndroidView<MapView>(
                factory = { 
                    mapView.apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(6.0)
                        controller.setCenter(GeoPoint(-18.8792, 47.5079))

                        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                if (step == 1) departurePos = p else arrivalPos = p
                                return true
                            }
                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        })
                        overlays.add(eventsOverlay)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.overlays.removeAll(view.overlays.filterIsInstance<Marker>())
                    departurePos?.let {
                        val marker = Marker(view)
                        marker.position = it
                        marker.title = "Départ"
                        marker.icon = context.getDrawable(android.R.drawable.ic_menu_myplaces)
                        view.overlays.add(marker)
                    }
                    arrivalPos?.let {
                        val marker = Marker(view)
                        marker.position = it
                        marker.title = "Arrivée"
                        marker.icon = context.getDrawable(android.R.drawable.ic_menu_directions)
                        view.overlays.add(marker)
                    }
                }
            )

            // --- OVERLAY D'INSTRUCTIONS MODERNE ---
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (step == 1) Icons.Default.LocationOn else Icons.Default.Flag,
                            contentDescription = null,
                            tint = if (step == 1) blueColor else Color(0xFFD32F2F)
                        )
                        Spacer(Modifier.width(12.dp))
                        AnimatedContent(targetState = step, label = "instruction") { s ->
                            Text(
                                text = if (s == 1) "Où commence le voyage ?" else "Où s'arrête le voyage ?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
                
                // Petit Stepper visuel
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(24.dp, 4.dp).clip(CircleShape).background(if (step >= 1) blueColor else Color.LightGray))
                    Box(Modifier.size(24.dp, 4.dp).clip(CircleShape).background(if (step >= 2) blueColor else Color.LightGray))
                }
            }

            // --- BOUTON DE VALIDATION ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (step == 1 && departurePos != null) {
                            step = 2
                        } else if (step == 2 && arrivalPos != null && departurePos != null) {
                            onPointsSelected(
                                departurePos!!.latitude, departurePos!!.longitude,
                                arrivalPos!!.latitude, arrivalPos!!.longitude
                            )
                        }
                    },
                    enabled = (step == 1 && departurePos != null) || (step == 2 && arrivalPos != null),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = blueColor,
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (step == 1) "VALIDER LE DÉPART" else "CONFIRMER L'ARRIVÉE",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
