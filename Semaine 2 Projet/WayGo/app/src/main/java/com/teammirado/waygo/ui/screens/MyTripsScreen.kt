package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip

@Composable
fun MyTripsScreen(
    onTripClick: (Trip) -> Unit,
    onChatClick: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var reservedTrips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var publishedTrips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedTab) {
        isLoading = true
        errorMessage = null
        try {
            if (selectedTab == 0) {
                reservedTrips = RetrofitClient.apiService.getMyBookings()
            } else {
                publishedTrips = RetrofitClient.apiService.getMyPublishedTrips()
            }
        } catch (e: Exception) {
            errorMessage = "Erreur : ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    // Structure stable sans Scaffold expérimental
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC))) {
        
        // Header manuel stable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF003399)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mes Trajets",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Onglets manuels stables
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White)
        ) {
            val tabModifier = Modifier.weight(1f).fillMaxHeight()
            
            Box(
                modifier = tabModifier.clickable { selectedTab = 0 },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Réservés", 
                        color = if (selectedTab == 0) Color(0xFF003399) else Color.Gray,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                    if (selectedTab == 0) {
                        Box(modifier = Modifier.width(60.dp).height(3.dp).background(Color(0xFF003399)))
                    }
                }
            }

            Box(
                modifier = tabModifier.clickable { selectedTab = 1 },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Publiés", 
                        color = if (selectedTab == 1) Color(0xFF003399) else Color.Gray,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                    if (selectedTab == 1) {
                        Box(modifier = Modifier.width(60.dp).height(3.dp).background(Color(0xFF003399)))
                    }
                }
            }
        }

        // Liste des trajets
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF003399))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                val tripsToShow = if (selectedTab == 0) reservedTrips else publishedTrips

                if (tripsToShow.isEmpty()) {
                    Text(
                        text = "Aucun trajet pour le moment",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tripsToShow) { trip ->
                            MyTripItem(
                                trip = trip,
                                onDetailClick = { onTripClick(trip) },
                                onChatClick = { onChatClick(trip.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyTripItem(trip: Trip, onDetailClick: () -> Unit, onChatClick: () -> Unit) {
    // Utilisation de Surface pour la carte (stable)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${trip.departure} → ${trip.arrival}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = trip.departureTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF003399)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Conducteur : ${trip.driverName}", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDetailClick) {
                    Text("Détails")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onChatClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003399)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat")
                }
            }
        }
    }
}
