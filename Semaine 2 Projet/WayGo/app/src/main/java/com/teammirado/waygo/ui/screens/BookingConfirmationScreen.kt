package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip
import kotlinx.coroutines.launch

@Composable
fun BookingConfirmationScreen(
    tripId: Int,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    var trip by remember { mutableStateOf<Trip?>(null) }
    var selectedSeats by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }
    var isBooking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(tripId) {
        try {
            trip = RetrofitClient.apiService.getTripDetails(tripId)
        } catch (e: Exception) {
            errorMessage = "Erreur chargement trajet : ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(80.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Réserver votre place",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    trip?.let {
                        Text(
                            text = "${it.departure} → ${it.arrival}",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "avec ${it.driverName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Nombre de places", fontWeight = FontWeight.SemiBold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        IconButton(onClick = { if (selectedSeats > 1) selectedSeats-- }, enabled = !isBooking) {
                            Text("-", fontSize = 24.sp)
                        }
                        Text(
                            text = selectedSeats.toString(),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = { if (selectedSeats < (trip?.seatsAvailable ?: 1)) selectedSeats++ }, enabled = !isBooking) {
                            Text("+", fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total à payer", fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format("%.2f €", (trip?.price ?: 0.0) * selectedSeats),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF003399),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isBooking = true
                                try {
                                    RetrofitClient.apiService.createBooking(
                                        mapOf("trip_id" to tripId, "seats" to selectedSeats)
                                    )
                                    onConfirm()
                                } catch (e: Exception) {
                                    errorMessage = "Erreur réservation : ${e.localizedMessage}"
                                } finally {
                                    isBooking = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003399)),
                        enabled = !isBooking
                    ) {
                        if (isBooking) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Confirmer la réservation")
                        }
                    }
                    
                    TextButton(onClick = onBack, enabled = !isBooking) {
                        Text("Annuler", color = Color.Gray)
                    }
                }
            }
        }
    }
}
