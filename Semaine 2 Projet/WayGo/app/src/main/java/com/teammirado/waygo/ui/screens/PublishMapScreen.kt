package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.LatLng

@Composable
fun PublishMapScreen(onPointsSelected: (LatLng, LatLng) -> Unit, onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(1) } // 1: Départ, 2: Arrivée
    var departurePos by remember { mutableStateOf<LatLng?>(null) }
    var arrivalPos by remember { mutableStateOf<LatLng?>(null) }
    
    val cameraPositionState = rememberCameraPositionState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (step == 1) "Lieu de départ" else "Lieu d'arrivée") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    if (step == 1) departurePos = latLng else arrivalPos = latLng
                }
            ) {
                departurePos?.let { Marker(state = MarkerState(position = it), title = "Départ") }
                arrivalPos?.let { Marker(state = MarkerState(position = it), title = "Arrivée") }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            ) {
                Text(
                    text = if (step == 1) "Cliquez sur la carte pour définir le départ" else "Cliquez sur la carte pour définir l'arrivée",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    if (step == 1 && departurePos != null) {
                        step = 2
                    } else if (step == 2 && arrivalPos != null) {
                        onPointsSelected(departurePos!!, arrivalPos!!)
                    }
                },
                enabled = (step == 1 && departurePos != null) || (step == 2 && arrivalPos != null),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(if (step == 1) "Confirmer le départ" else "Confirmer l'arrivée")
            }
        }
    }
}
