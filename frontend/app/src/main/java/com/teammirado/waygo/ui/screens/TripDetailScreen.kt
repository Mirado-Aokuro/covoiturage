package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teammirado.waygo.data.model.Trip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(trip: Trip?, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Détails du trajet") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Retour") }
                }
            )
        }
    ) { innerPadding ->
        if (trip != null) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("${trip.departure} -> ${trip.arrival}", style = MaterialTheme.typography.headlineMedium)
                HorizontalDivider()
                Text("Conducteur: ${trip.driverName}", style = MaterialTheme.typography.titleMedium)
                Text("Date: ${trip.date} à ${trip.time}")
                Text("Prix: ${trip.price} €", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text("Places disponibles: ${trip.seatsAvailable}")
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { /* Action de réservation */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Réserver ce trajet")
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Text("Trajet non trouvé", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
