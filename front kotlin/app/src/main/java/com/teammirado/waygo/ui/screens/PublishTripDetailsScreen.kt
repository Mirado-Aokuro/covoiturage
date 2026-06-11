package com.teammirado.waygo.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.PublishTripRequest
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun PublishTripDetailsScreen(
    departure: String, // Format "lat,lng"
    arrival: String,   // Format "lat,lng"
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    var depCity by remember { mutableStateOf("") }
    var arrCity by remember { mutableStateOf("") }
    var dateDisplay by remember { mutableStateOf("") }
    var dateApi by remember { mutableStateOf("") }
    var timeDisplay by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("3") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val blueColor = Color(0xFF003399)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val m = if (month + 1 < 10) "0${month + 1}" else "${month + 1}"
            val d = if (day < 10) "0$day" else "$day"
            dateDisplay = "$d/$m/$year"
            dateApi = "$year-$m-$d"
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, min ->
            val h = if (hour < 10) "0$hour" else "$hour"
            val m = if (min < 10) "0$min" else "$min"
            timeDisplay = "$h:$m"
        },
        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Publier une offre", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = blueColor)
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
            }

            // --- SECTION VILLES ---
            PublishSectionCard(title = "Itinéraire", icon = Icons.Default.Route) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CustomPublishField(
                        value = depCity,
                        onValueChange = { depCity = it },
                        label = "Ville de départ",
                        icon = Icons.Default.LocationOn
                    )
                    
                    CustomPublishField(
                        value = arrCity,
                        onValueChange = { arrCity = it },
                        label = "Ville d'arrivée",
                        icon = Icons.Default.Place,
                        iconColor = Color(0xFFD32F2F)
                    )
                }
            }

            // --- SECTION DATE ET HEURE ---
            PublishSectionCard(title = "Planification", icon = Icons.Default.Event) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ClickablePublishField(
                        value = if (dateDisplay.isEmpty()) "Date" else dateDisplay,
                        onClick = { datePickerDialog.show() },
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )
                    ClickablePublishField(
                        value = if (timeDisplay.isEmpty()) "Heure" else timeDisplay,
                        onClick = { timePickerDialog.show() },
                        icon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- SECTION PRIX ET PLACES ---
            PublishSectionCard(title = "Conditions", icon = Icons.Default.Settings) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomPublishField(
                        value = price,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) price = it },
                        label = "Prix (€)",
                        icon = Icons.Default.Euro,
                        modifier = Modifier.weight(1f)
                    )
                    CustomPublishField(
                        value = seats,
                        onValueChange = { if (it.all { char -> char.isDigit() }) seats = it },
                        label = "Places",
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val depCoords = departure.split(",")
                            val arrCoords = arrival.split(",")
                            
                            val request = PublishTripRequest(
                                departureCity = depCity,
                                arrivalCity = arrCity,
                                departureLatitude = depCoords.getOrNull(0)?.toDoubleOrNull() ?: 0.0,
                                departureLongitude = depCoords.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
                                arrivalLatitude = arrCoords.getOrNull(0)?.toDoubleOrNull() ?: 0.0,
                                arrivalLongitude = arrCoords.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
                                date = dateApi,
                                departureTime = timeDisplay,
                                price = price.toDoubleOrNull() ?: 0.0,
                                seatsAvailable = seats.toIntOrNull() ?: 1
                            )
                            
                            RetrofitClient.apiService.publishTrip(request)
                            Toast.makeText(context, "Trajet publié avec succès !", Toast.LENGTH_SHORT).show()
                            onConfirm()
                        } catch (e: Exception) {
                            errorMessage = "Impossible de publier. Vérifiez vos données."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blueColor),
                enabled = !isLoading && depCity.isNotBlank() && arrCity.isNotBlank() && dateApi.isNotBlank() && timeDisplay.isNotBlank() && price.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    Text("PUBLIER LE TRAJET", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PublishSectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
fun CustomPublishField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconColor: Color = Color(0xFF003399)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        leadingIcon = { Icon(icon, null, tint = iconColor) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF003399),
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedContainerColor = Color(0xFFF7F9FC),
            unfocusedContainerColor = Color(0xFFF7F9FC)
        ),
        singleLine = true
    )
}

@Composable
fun ClickablePublishField(
    value: String,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF7F9FC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color(0xFF003399))
            Spacer(Modifier.width(12.dp))
            Text(
                text = value,
                color = if (value == "Date" || value == "Heure") Color.Gray else Color.Black,
                fontSize = 16.sp
            )
        }
    }
}
