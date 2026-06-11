package com.teammirado.waygo.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.teammirado.waygo.data.model.Booking
import com.teammirado.waygo.data.model.ReviewRequest
import com.teammirado.waygo.data.model.Trip
import kotlinx.coroutines.launch

@Composable
fun TripDetailScreen(
    tripId: Int, 
    userRole: String? = null,
    currentUserId: Int, 
    onBack: () -> Unit, 
    onBook: () -> Unit,
    onChatClick: (Int, Int) -> Unit,
    onTrackingClick: (Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var trip by remember { mutableStateOf<Trip?>(null) }
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var canSeeBookings by remember { mutableStateOf(false) }
    
    var showReviewDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var selectedUserIdForReview by remember { mutableStateOf<Int?>(null) }

    val blueColor = Color(0xFF003399)

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val fetchedTrip = RetrofitClient.apiService.getTripDetails(tripId)
                trip = fetchedTrip
                if (userRole == "driver") {
                    try {
                        bookings = RetrofitClient.apiService.getTripBookings(tripId)
                        canSeeBookings = true
                    } catch (e: Exception) {
                        canSeeBookings = false
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur de chargement", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(tripId) { loadData() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Détails du voyage", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = blueColor)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        trip?.let {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Rejoins mon trajet WayGo : ${it.departureCity} ➔ ${it.arrivalCity} !")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager"))
                        }
                    }) {
                        Icon(Icons.Default.Share, null, tint = blueColor)
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
        } else if (trip != null) {
            val currentTrip = trip!!
            
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFF7F9FC))
            ) {
                // --- SECTION VILLE DÉPART/ARRIVÉE ---
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = blueColor, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(currentTrip.departureCity ?: "", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(Modifier.padding(start = 9.dp).width(2.dp).height(30.dp).background(blueColor.copy(0.3f)))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(currentTrip.arrivalCity ?: "", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 20.dp),
                            color = DividerDefaults.color.copy(alpha = 0.5f)
                        )
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DetailInfoChip(Icons.Default.Event, currentTrip.date ?: "")
                            Text(
                                text = "${currentTrip.price} €",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF007A33)
                            )
                        }
                    }
                }

                // --- BOUTON SUIVI LIVE ---
                Button(
                    onClick = { onTrackingClick(currentTrip.id, userRole == "driver") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Icon(Icons.Default.MyLocation, null)
                    Spacer(Modifier.width(12.dp))
                    Text("SUIVRE LE TRAJET EN DIRECT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                // --- SECTION CONDUCTEUR ---
                Text("Conducteur", Modifier.padding(start = 24.dp, top = 32.dp, bottom = 12.dp), fontWeight = FontWeight.Bold, color = Color.Gray)
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://i.pravatar.cc/150?u=${currentTrip.driverId}",
                            contentDescription = null,
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.LightGray)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(currentTrip.driverName ?: "Conducteur", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                Text(" ${currentTrip.driverRating ?: 5.0} • ${currentTrip.driverReviewsCount ?: 0} avis", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                        if (userRole != "driver") {
                            IconButton(
                                onClick = { onChatClick(currentTrip.id, currentUserId) },
                                modifier = Modifier.background(blueColor.copy(0.1f), CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, null, tint = blueColor)
                            }
                        }
                    }
                }

                // --- PASSAGERS (Si Conducteur) ---
                if (canSeeBookings && bookings.isNotEmpty()) {
                    Text("Passagers confirmés", Modifier.padding(start = 24.dp, top = 32.dp, bottom = 12.dp), fontWeight = FontWeight.Bold, color = Color.Gray)
                    bookings.forEach { booking ->
                        BookingItemModern(
                            booking = booking,
                            onChat = { onChatClick(currentTrip.id, booking.userId) },
                            onReview = {
                                selectedUserIdForReview = booking.userId
                                showReviewDialog = true
                            },
                            onAccept = { scope.launch { try { RetrofitClient.apiService.confirmBooking(booking.id); loadData() } catch(e: Exception) { Toast.makeText(context, "Erreur validation", Toast.LENGTH_SHORT).show() } } },
                            onReject = { scope.launch { try { RetrofitClient.apiService.rejectBooking(booking.id); loadData() } catch(e: Exception) { Toast.makeText(context, "Erreur rejet", Toast.LENGTH_SHORT).show() } } }
                        )
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
            
            // --- BARRE D'ACTION BASSE (Pour passager) ---
            if (userRole != "driver") {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        color = Color.White,
                        shadowElevation = 16.dp
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Places dispos", color = Color.Gray, fontSize = 12.sp)
                                Text("${currentTrip.seatsAvailable ?: 1} restantes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Button(
                                onClick = onBook,
                                modifier = Modifier.width(200.dp).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = blueColor),
                                enabled = (currentTrip.seatsAvailable ?: 0) > 0
                            ) {
                                Text("RÉSERVER", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Laisser un avis", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        (1..5).forEach { index ->
                            IconButton(onClick = { rating = index }) {
                                Icon(
                                    Icons.Default.Star, null,
                                    tint = if (index <= rating) Color(0xFFFFB300) else Color.LightGray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment, onValueChange = { comment = it },
                        label = { Text("Votre expérience...") },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            // ✅ Correction : Appel avec arguments nommés pour correspondre au nouveau modèle
                            RetrofitClient.apiService.leaveReview(
                                ReviewRequest(
                                    tripId = tripId,
                                    rating = rating,
                                    comment = comment,
                                    ratedUserId = selectedUserIdForReview ?: trip?.driverId
                                )
                            )
                            showReviewDialog = false
                            Toast.makeText(context, "Merci pour votre avis !", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur lors de l'envoi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = blueColor)) { Text("Envoyer") }
            },
            dismissButton = { TextButton(onClick = { showReviewDialog = false }) { Text("Annuler") } }
        )
    }
}

@Composable
fun DetailInfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BookingItemModern(
    booking: Booking,
    onChat: () -> Unit,
    onReview: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = booking.userAvatar ?: "https://i.pravatar.cc/150?u=${booking.userId}",
                contentDescription = null,
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(booking.displayUserName, fontWeight = FontWeight.Bold)
                StatusBadge(booking.status)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (booking.status == "pending") {
                    IconButton(onClick = onAccept, modifier = Modifier.background(Color(0xFFE8F5E9), CircleShape)) {
                        Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50))
                    }
                    IconButton(onClick = onReject, modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape)) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFFD32F2F))
                    }
                } else {
                    IconButton(onClick = onChat, modifier = Modifier.background(Color(0xFFF0F4FF), CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color(0xFF003399))
                    }
                    if (booking.status == "confirmed") {
                        IconButton(onClick = onReview, modifier = Modifier.background(Color(0xFFFFF8E1), CircleShape)) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        "confirmed" -> Color(0xFF4CAF50) to "Confirmé"
        "pending" -> Color(0xFFFF9800) to "En attente"
        "rejected" -> Color(0xFFD32F2F) to "Refusé"
        else -> Color.Gray to status
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
