package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teammirado.waygo.data.MockData
import com.teammirado.waygo.data.model.Trip

@Composable
fun HomeScreen(onTripClick: (Trip) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Way Go - Accueil") })
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text(
                "Trajets disponibles",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn {
                items(MockData.trips) { trip ->
                    TripItem(trip, onClick = { onTripClick(trip) })
                }
            }
        }
    }
}

@Composable
fun TripItem(trip: Trip, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${trip.departure} -> ${trip.arrival}", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${trip.price} €")
                Text("${trip.seatsAvailable} places")
            }
            Text("Conducteur: ${trip.driverName}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
