package com.teammirado.waygo.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun SearchTripScreen(onSearch: (String, String, String, String) -> Unit) {
    var departure by remember { mutableStateOf("") }
    var arrival by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") } 
    var dateDisplay by remember { mutableStateOf("") }
    var dateApi by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val blueColor = Color(0xFF003399)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val monthStr = if (month + 1 < 10) "0${month + 1}" else "${month + 1}"
            val dayStr = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            dateDisplay = "$dayStr/$monthStr/$year"
            dateApi = "$year-$monthStr-$dayStr"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Trouver un trajet", fontWeight = FontWeight.Bold) },
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
            // --- SECTION ITINÉRAIRE ---
            SearchSectionCard(title = "Itinéraire", icon = Icons.Default.Route) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CustomSearchField(
                        value = departure,
                        onValueChange = { departure = it },
                        label = "D'où partez-vous ?",
                        icon = Icons.Default.LocationOn
                    )
                    
                    CustomSearchField(
                        value = arrival,
                        onValueChange = { arrival = it },
                        label = "Où allez-vous ?",
                        icon = Icons.Default.Place,
                        iconColor = Color(0xFFD32F2F)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF7F9FC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = blueColor)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = if (dateDisplay.isEmpty()) "Date du voyage" else dateDisplay,
                                color = if (dateDisplay.isEmpty()) Color.Gray else Color.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // --- SECTION CONDUCTEUR ---
            SearchSectionCard(title = "Conducteur spécifique", icon = Icons.Default.Person) {
                CustomSearchField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = "Nom du chauffeur",
                    icon = Icons.Default.Badge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOUTON RECHERCHE ---
            Button(
                onClick = { 
                    onSearch(departure.trim(), arrival.trim(), dateApi, driverName.trim())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blueColor),
                enabled = (departure.isNotBlank() && arrival.isNotBlank()) || driverName.isNotBlank()
            ) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(12.dp))
                Text("RECHERCHER MAINTENANT", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SearchSectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
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
fun CustomSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    iconColor: Color = Color(0xFF003399)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
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
