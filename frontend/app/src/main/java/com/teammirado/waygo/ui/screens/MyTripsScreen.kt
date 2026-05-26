package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teammirado.waygo.data.MockData

@Composable
fun MyTripsScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Mes Trajets") })
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text(
                "Vos réservations",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn {
                // Pour le mock, on affiche les mêmes trajets que l'accueil
                items(MockData.trips.take(2)) { trip ->
                    TripItem(trip, onClick = { /* Détail de ma réservation */ })
                }
            }
        }
    }
}
