package com.teammirado.waygo.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.UpdateProfileRequest
import com.teammirado.waygo.data.model.User
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun ProfileScreen(
    userRole: String? = null,
    onSettingsClick: () -> Unit, 
    onLogout: () -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    
    var showVehicleDialog by remember { mutableStateOf(false) }
    var vehicleModel by remember { mutableStateOf("") }
    var vehiclePlate by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val blueColor = Color(0xFF003399)

    fun loadProfile() {
        scope.launch {
            isLoading = true
            try {
                user = RetrofitClient.apiService.getCurrentUser()
                vehicleModel = user?.vehicleModel ?: ""
                vehiclePlate = user?.vehiclePlate ?: ""
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur de chargement", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isUploading = true
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestFile)
                        RetrofitClient.apiService.uploadAvatar(body)
                        loadProfile()
                        Toast.makeText(context, "Photo mise à jour", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erreur upload", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mon Profil", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres", tint = blueColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = blueColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFF7F9FC))
            ) {
                ProfileHeaderModern(
                    user = user, 
                    isUploading = isUploading,
                    onEditPhoto = { launcher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- SECTION STATISTIQUES ---
                ProfileSectionCard(title = "Mes Statistiques") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        StatItem(label = "Trajets", value = user?.tripsCount?.toString() ?: "0", icon = Icons.Default.History)
                        Box(Modifier.width(1.dp).height(40.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                        StatItem(label = "Note", value = "${user?.rating ?: 5.0}", icon = Icons.Default.Star, color = Color(0xFFFFB300))
                    }
                }

                // --- SECTION VÉHICULE (Si Conducteur) ---
                if (user?.role == "driver") {
                    ProfileSectionCard(
                        title = "Mon Véhicule",
                        action = {
                            TextButton(onClick = { showVehicleDialog = true }) {
                                Text("Modifier", color = blueColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).background(blueColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsCar, null, tint = blueColor)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(user?.vehicleModel ?: "Non renseigné", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(user?.vehiclePlate ?: "Plaque d'immatriculation", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // --- OPTIONS COMPTE ---
                ProfileSectionCard(title = "Préférences") {
                    ProfileOptionRow(Icons.Default.Favorite, "Mes préférences voyage", user?.preferences ?: "Standard")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.2f))
                    ProfileOptionRow(Icons.Default.Notifications, "Notifications", "Activées")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- BOUTON DÉCONNEXION ---
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(Modifier.width(12.dp))
                    Text("SE DÉCONNECTER", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showVehicleDialog = false },
            title = { Text("Véhicule", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = vehicleModel,
                        onValueChange = { vehicleModel = it },
                        label = { Text("Modèle (ex: Toyota)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = vehiclePlate,
                        onValueChange = { vehiclePlate = it },
                        label = { Text("Plaque (ex: 1234 TAA)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            RetrofitClient.apiService.updateProfile(UpdateProfileRequest(vehicleModel = vehicleModel, vehiclePlate = vehiclePlate))
                            loadProfile()
                            showVehicleDialog = false
                            Toast.makeText(context, "Profil mis à jour", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = blueColor)) { Text("Enregistrer") }
            },
            dismissButton = { TextButton(onClick = { showVehicleDialog = false }) { Text("Annuler") } }
        )
    }
}

@Composable
fun ProfileHeaderModern(user: User?, isUploading: Boolean, onEditPhoto: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF7F9FC))))
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Surface(
                    modifier = Modifier.size(110.dp).shadow(12.dp, CircleShape),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(3.dp, Color.White)
                ) {
                    if (isUploading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
                        }
                    } else if (user?.avatarUrl != null) {
                        AsyncImage(model = user.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = Color.White)
                        }
                    }
                }
                
                IconButton(
                    onClick = onEditPhoto,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF003399), CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp), tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(user?.name ?: "Utilisateur", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(user?.email ?: "", color = Color.Gray, fontSize = 14.sp)
            
            Surface(
                color = Color(0xFF003399).copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = if (user?.role == "driver") "Conducteur vérifié" else "Membre Passager",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF003399),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileSectionCard(title: String, action: @Composable (() -> Unit)? = null, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
            action?.invoke()
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector, color: Color = Color(0xFF003399)) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileOptionRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Text(value, color = Color.Gray, fontSize = 13.sp)
    }
}
