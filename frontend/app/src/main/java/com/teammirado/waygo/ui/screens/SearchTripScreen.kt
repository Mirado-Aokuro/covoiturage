package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchTripScreen(onSearch: (String, String) -> Unit) {
    var departure by remember { mutableStateOf("") }
    var arrival by remember { mutableStateOf("") }

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
                label = { Text("Départ") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = arrival,
                onValueChange = { arrival = it },
                label = { Text("Arrivée") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSearch(departure, arrival) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rechercher")
            }
        }
    }
}
