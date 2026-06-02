package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip

@Composable
fun TripDetailScreen(tripId: Int, onBack: () -> Unit, onBook: () -> Unit) {
    var trip by remember { mutableStateOf<Trip?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tripId) {
        try {
            trip = RetrofitClient.apiService.getTripDetails(tripId)
        } catch (e: Exception) {
            errorMessage = "Erreur : ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Top Bar stable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF003399))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Text(
                text = "Détails du trajet",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF003399))
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = Color.Red, modifier = Modifier.align(Alignment.Center).padding(16.dp))
            } else if (trip != null) {
                val t = trip!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("${t.departure} → ${t.arrival}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    
                    Text("Conducteur : ${t.driverName}", style = MaterialTheme.typography.titleMedium)
                    Text("Départ : ${t.departureTime}")
                    Text("Arrivée : ${t.arrivalTime}")
                    Text(
                        text = "Prix : ${t.price} €",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF007A33),
                        fontWeight = FontWeight.Bold
                    )
                    Text("Places disponibles : ${t.seatsAvailable}")

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onBook,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003399)),
                        enabled = t.seatsAvailable > 0
                    ) {
                        Text("Réserver ce trajet", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
