package com.teammirado.waygo.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.CreateBookingRequest
import com.teammirado.waygo.data.model.Trip
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

@Composable
fun BookingConfirmationScreen(
    tripId: Int,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var trip by remember { mutableStateOf<Trip?>(null) }
    var selectedSeats by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    
    val blueColor = Color(0xFF003399)

    LaunchedEffect(tripId) {
        try {
            trip = RetrofitClient.apiService.getTripDetails(tripId)
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur de chargement", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Confirmation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = blueColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = blueColor)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = blueColor.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ConfirmationNumber, null, modifier = Modifier.size(40.dp), tint = blueColor)
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text("Presque arrivé !", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    Text("Vérifiez vos informations avant de réserver", fontSize = 14.sp, color = Color.Gray)

                    Spacer(Modifier.height(32.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Nombre de voyageurs", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                SeatActionButton(Icons.Default.Remove, enabled = selectedSeats > 1 && !isProcessing) {
                                    selectedSeats--
                                }
                                Text(
                                    text = selectedSeats.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = blueColor
                                )
                                SeatActionButton(Icons.Default.Add, enabled = selectedSeats < (trip?.seatsAvailable ?: 1) && !isProcessing) {
                                    selectedSeats++
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Prix par place", color = Color.Gray)
                                Text("${trip?.price} €", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nombre de places", color = Color.Gray)
                                Text("x$selectedSeats", fontWeight = FontWeight.Bold)
                            }
                            
                            HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)
                            
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TOTAL À PAYER", fontWeight = FontWeight.Black, fontSize = 16.sp)
                                Text(
                                    text = String.format("%.2f €", (trip?.price ?: 0.0) * selectedSeats),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xFF007A33),
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(Icons.Default.Shield, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Paiement sécurisé WayGo", fontSize = 12.sp, color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                try {
                                    val amountInCents = ((trip?.price ?: 0.0) * selectedSeats * 100).toInt()
                                    
                                    // 1. Simuler l'appel Stripe (facultatif en mode simulation)
                                    try {
                                        RetrofitClient.apiService.createPaymentIntent(mapOf("amount" to amountInCents))
                                    } catch (e: Exception) { /* Ignoré pour la démo */ }
                                    
                                    // 2. Tenter l'enregistrement de la réservation
                                    RetrofitClient.apiService.createBooking(CreateBookingRequest(tripId, selectedSeats))
                                    
                                    isProcessing = false
                                    Toast.makeText(context, "Réservation confirmée !", Toast.LENGTH_LONG).show()
                                    onConfirm()

                                } catch (e: HttpException) {
                                    isProcessing = false
                                    val errorBody = e.response()?.errorBody()?.string()
                                    val serverMessage = try {
                                        val json = JSONObject(errorBody ?: "")
                                        json.optString("details", json.optString("error", "Erreur serveur"))
                                    } catch (ex: Exception) {
                                        "Détails indisponibles (Code ${e.code()})"
                                    }
                                    Toast.makeText(context, "Serveur WayGo : $serverMessage", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    isProcessing = false
                                    Toast.makeText(context, "Erreur de connexion au serveur", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = blueColor),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Text("CONFIRMER ET RÉSERVER", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                    }
                    
                    TextButton(
                        onClick = onBack,
                        enabled = !isProcessing,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Annuler", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SeatActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(44.dp)
            .background(if (enabled) Color(0xFF003399).copy(alpha = 0.1f) else Color(0xFFF5F7FA), CircleShape)
    ) {
        Icon(icon, null, tint = if (enabled) Color(0xFF003399) else Color.LightGray)
    }
}
