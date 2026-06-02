package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchTripScreen(onSearch: (String, String, String) -> Unit) {
    var departure by remember { mutableStateOf("") }
    var arrival by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Rechercher un trajet") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = departure,
                onValueChange = { departure = it },
                label = { Text("Ville de départ") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = arrival,
                onValueChange = { arrival = it },
                label = { Text("Ville d'arrivée") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (JJ/MM/AAAA)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onSearch(departure, arrival, date) },
                modifier = Modifier.fillMaxWidth(),
                enabled = departure.isNotBlank() && arrival.isNotBlank()
            ) {
                Text("Trouver un trajet")
            }
        }
    }
}
