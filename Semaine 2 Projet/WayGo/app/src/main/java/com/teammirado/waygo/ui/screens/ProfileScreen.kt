package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.User

@Composable
fun ProfileScreen(onSettingsClick: () -> Unit, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Passager, 1: Conducteur
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            user = RetrofitClient.apiService.getCurrentUser()
        } catch (e: Exception) {
            errorMessage = "Erreur chargement profil : ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mon Profil") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF003399))
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(errorMessage!!, color = Color.Red)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Profil
                ProfileHeader(user)

                // Onglets Passager / Conducteur
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Passager", modifier = Modifier.padding(16.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Conducteur", modifier = Modifier.padding(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    PassengerSection(user)
                } else {
                    DriverSection(user)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Section Paramètres rapides
                SettingsSection(onSettingsClick)

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Déconnexion")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(user: User?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                color = Color.LightGray
            ) {
                if (user?.avatarUrl != null) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        modifier = Modifier.padding(20.dp),
                        tint = Color.White
                    )
                }
            }
            // Badge édition
            FloatingActionButton(
                onClick = { /* Editer photo */ },
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd),
                containerColor = Color(0xFF003399),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(user?.name ?: "Utilisateur", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        
        if (user?.isVerified == true) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Identité vérifiée", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
            }
        }
    }
}

@Composable
fun PassengerSection(user: User?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        ProfileStatRow(Icons.Default.DirectionsCar, "Trajets effectués", user?.tripsCount?.toString() ?: "0")
        ProfileStatRow(Icons.Default.Star, "Note moyenne", "${user?.rating ?: 0.0}/5")
        ProfileStatRow(Icons.Default.Favorite, "Préférences", user?.preferences ?: "Non spécifiées")
    }
}

@Composable
fun DriverSection(user: User?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Véhicule", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF003399))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(user?.vehicleModel ?: "Aucun véhicule", fontWeight = FontWeight.Bold)
                    Text(user?.vehiclePlate ?: "Plaque non renseignée", color = Color.Gray)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        ProfileStatRow(Icons.Default.Route, "Trajets publiés", user?.tripsCount?.toString() ?: "0")
        
        OutlinedButton(
            onClick = { /* Gérer véhicule */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Modifier mon véhicule")
        }
    }
}

@Composable
fun SettingsSection(onSettingsClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Paramètres", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        SettingsItem(Icons.Default.Notifications, "Notifications", "Alertes et messages", onSettingsClick)
        SettingsItem(Icons.Default.Lock, "Sécurité", "Mot de passe et compte", onSettingsClick)
        SettingsItem(Icons.AutoMirrored.Filled.Help, "Aide & Support", "FAQ et contact", onSettingsClick)
    }
}

@Composable
fun ProfileStatRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF003399))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
