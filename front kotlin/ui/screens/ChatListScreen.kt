package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip
import com.teammirado.waygo.data.model.User

@Composable
fun ChatListScreen(
    userRole: String?,
    onChatClick: (Int, Int) -> Unit // ✅ tripId, passengerId
) {
    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            currentUser = RetrofitClient.apiService.getCurrentUser()
            // Pour l'instant, on liste les trajets liés à l'utilisateur
            trips = if (userRole == "driver") {
                RetrofitClient.apiService.getMyPublishedTrips()
            } else {
                RetrofitClient.apiService.getMyBookings()
            }
        } catch (e: Exception) {
            errorMessage = "Erreur de chargement des discussions"
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC))) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF003399)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mes discussions",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF003399))
            }
        } else if (errorMessage != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(errorMessage!!, color = Color.Red)
            }
        } else if (trips.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.LightGray
                )
                Spacer(Modifier.height(16.dp))
                Text("Aucune discussion active", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trips) { trip ->
                    // Si conducteur, il faut théoriquement lister les passagers du trajet.
                    // Pour simplifier ici, on clique sur le trajet pour voir les passagers (dans TripDetail)
                    // OU on ouvre le chat par défaut. 
                    // Pour un passager, passengerId = son propre ID.
                    val passengerId = if (userRole == "driver") -1 else (currentUser?.id ?: -1)
                    
                    ChatItem(trip) {
                        if (userRole == "driver") {
                            // Idéalement, rediriger vers une liste de passagers ou TripDetail
                            // Ici on passe -1 pour indiquer qu'on doit choisir le passager
                            onChatClick(trip.id, -1) 
                        } else {
                            onChatClick(trip.id, passengerId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(trip: Trip, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EAF6)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = trip.driverName?.take(1) ?: "C",
                    color = Color(0xFF003399),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.driverName ?: "Conducteur",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${trip.departure} → ${trip.arrival}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                tint = Color(0xFF003399).copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
