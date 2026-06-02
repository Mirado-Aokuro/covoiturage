package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Paramètres") },
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
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Préférences", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            ListItem(
                headlineContent = { Text("Notifications Push") },
                supportingContent = { Text("Recevoir des alertes pour les nouveaux messages") },
                trailingContent = {
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }
            )
            
            ListItem(
                headlineContent = { Text("Mode Sombre") },
                trailingContent = {
                    Switch(checked = darkModeEnabled, onCheckedChange = { darkModeEnabled = it })
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("Compte", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            TextButton(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Modifier le mot de passe", modifier = Modifier.fillMaxWidth())
            }
            
            TextButton(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Supprimer mon compte", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
