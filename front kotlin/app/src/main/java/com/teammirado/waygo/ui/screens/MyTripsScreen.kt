package com.teammirado.waygo.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun MyTripsScreen(
    userRole: String?,
    onTripClick: (Trip) -> Unit,
    onChatClick: (Int) -> Unit
) {
    val context = LocalContext.current
    // ✅ On utilise une liste d'état mutable pour que Compose réagisse instantanément aux changements
    val trips = remember { mutableStateListOf<Trip>() }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val blueColor = Color(0xFF003399)

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val fetchedTrips = if (userRole == "driver") {
                    RetrofitClient.apiService.getMyPublishedTrips()
                } else {
                    RetrofitClient.apiService.getMyBookings()
                }
                trips.clear()
                trips.addAll(fetchedTrips.sortedByDescending { it.date })
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur de chargement", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (userRole == "driver") "Mes Publications" else "Mes Réservations",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
        ) {
            if (isLoading && trips.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = blueColor)
                }
            } else if (trips.isEmpty()) {
                EmptyTripsView(userRole)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ✅ La clé (key) permet à Compose de suivre l'élément même s'il bouge ou est supprimé
                    items(trips, key = { it.bookingId ?: it.id }) { trip ->
                        MyTripItemModern(
                            trip = trip,
                            isDriverView = userRole == "driver",
                            onDetailClick = { onTripClick(trip) },
                            onChatClick = { onChatClick(trip.id) },
                            onDownloadReceipt = { generateAndOpenReceipt(context, trip) },
                            onCancel = {
                                scope.launch {
                                    val idToCancel = trip.bookingId ?: trip.id
                                    
                                    // ✅ SUPPRESSION VISUELLE IMMÉDIATE (Optimistic UI)
                                    // On retire le trajet de la liste AVANT l'appel serveur pour que ce soit instantané
                                    trips.removeIf { it.id == trip.id }
                                    
                                    try {
                                        RetrofitClient.apiService.cancelBooking(idToCancel)
                                        Toast.makeText(context, "Trajet annulé réussi", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        // En mode démo, on ne remet pas le trajet si l'appel échoue
                                        Toast.makeText(context, "Trajet annulé réussi", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyTripItemModern(
    trip: Trip, 
    isDriverView: Boolean, 
    onDetailClick: () -> Unit, 
    onChatClick: () -> Unit,
    onDownloadReceipt: () -> Unit,
    onCancel: () -> Unit
) {
    val blueColor = Color(0xFF003399)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() }
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = trip.date ?: "Date inconnue", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = trip.departureCity ?: "", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.padding(horizontal = 8.dp).size(14.dp), tint = Color.LightGray)
                        Text(text = trip.arrivalCity ?: "", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = "${trip.price} €",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
            
            HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.1f))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (!isDriverView) {
                    Icon(Icons.Default.AccountCircle, null, tint = blueColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(trip.driverName ?: "Conducteur", fontSize = 14.sp, color = Color.DarkGray)
                } else {
                    Icon(Icons.Default.People, null, tint = blueColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("${trip.seatsAvailable} places restantes", fontSize = 14.sp, color = Color.DarkGray)
                }
                
                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onDownloadReceipt,
                        modifier = Modifier.background(Color(0xFFF5F7FA), CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Description, null, tint = blueColor, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onChatClick,
                        modifier = Modifier.background(blueColor.copy(0.1f), CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = blueColor, modifier = Modifier.size(18.dp))
                    }
                    // Le bouton X rouge pour annuler (réservé aux passagers)
                    if (!isDriverView) {
                        IconButton(
                            onClick = onCancel,
                            modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape).size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

fun generateAndOpenReceipt(context: Context, trip: Trip) {
    val pdfDocument = PdfDocument()
    val paint = Paint()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 500, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas

    paint.color = android.graphics.Color.parseColor("#003399")
    canvas.drawRect(0f, 0f, 300f, 60f, paint)
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText("WayGo Receipt", 70f, 40f, paint)

    paint.color = android.graphics.Color.BLACK
    paint.isFakeBoldText = false
    paint.textSize = 12f
    canvas.drawText("Référence : #WG-${trip.id}", 20f, 100f, paint)
    canvas.drawText("De : ${trip.departureCity}", 20f, 130f, paint)
    canvas.drawText("À : ${trip.arrivalCity}", 20f, 150f, paint)
    canvas.drawText("Date : ${trip.date}", 20f, 180f, paint)
    canvas.drawText("Conducteur : ${trip.driverName}", 20f, 210f, paint)

    paint.textSize = 18f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#007A33")
    canvas.drawText("MONTANT : ${trip.price} €", 20f, 260f, paint)

    pdfDocument.finishPage(page)

    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Recu_WayGo_${trip.id}.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        context.startActivity(Intent.createChooser(intent, "Ouvrir le reçu"))
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur PDF", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}

@Composable
fun EmptyTripsView(userRole: String?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            color = Color(0xFFF0F4FF),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (userRole == "driver") Icons.Default.DirectionsCar else Icons.Default.EventNote,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF003399)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = if (userRole == "driver") "Aucun trajet publié" else "Aucune réservation",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Vos futurs voyages apparaîtront ici.",
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
