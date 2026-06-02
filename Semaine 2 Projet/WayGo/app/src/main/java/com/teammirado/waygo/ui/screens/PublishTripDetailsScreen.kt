package com.teammirado.waygo.ui.screens

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
import androidx.compose.ui.unit.dp
import com.teammirado.waygo.data.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun PublishTripDetailsScreen(
    departure: String, // Format "lat,lng" ou nom de ville
    arrival: String,   // Format "lat,lng" ou nom de ville
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    var depCity by remember { mutableStateOf("") }
    var arrCity by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("3") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Finaliser la publication") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage != null) {
                Text(errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            Text("Itinéraire", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = depCity,
                onValueChange = { depCity = it },
                label = { Text("Ville de départ") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            OutlinedTextField(
                value = arrCity,
                onValueChange = { arrCity = it },
                label = { Text("Ville d'arrivée") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            HorizontalDivider()

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (AAAA-MM-JJ)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Heure de départ (HH:MM)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Prix par place (€)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            OutlinedTextField(
                value = seats,
                onValueChange = { seats = it },
                label = { Text("Nombre de places") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val depCoords = departure.split(",")
                            val arrCoords = arrival.split(",")
                            
                            RetrofitClient.apiService.publishTrip(
                                mapOf(
                                    "departure_city" to depCity,
                                    "arrival_city" to arrCity,
                                    "departure_latitude" to (depCoords.getOrNull(0)?.toDoubleOrNull() ?: 0.0),
                                    "departure_longitude" to (depCoords.getOrNull(1)?.toDoubleOrNull() ?: 0.0),
                                    "arrival_latitude" to (arrCoords.getOrNull(0)?.toDoubleOrNull() ?: 0.0),
                                    "arrival_longitude" to (arrCoords.getOrNull(1)?.toDoubleOrNull() ?: 0.0),
                                    "date" to date,
                                    "departure_time" to time,
                                    "price" to (price.toDoubleOrNull() ?: 0.0),
                                    "seats_available" to (seats.toIntOrNull() ?: 1)
                                )
                            )
                            onConfirm()
                        } catch (e: Exception) {
                            errorMessage = "Erreur : ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading && depCity.isNotBlank() && arrCity.isNotBlank() && date.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Publier mon trajet")
                }
            }
        }
    }
}
