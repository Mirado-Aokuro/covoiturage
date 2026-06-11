package com.teammirado.waygo.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip
import com.teammirado.waygo.data.model.User

@Composable
fun ChatListScreen(
    userRole: String?,
    onChatClick: (Int, Int) -> Unit
) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var tripsWithChats by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val blueColor = Color(0xFF003399)

    LaunchedEffect(Unit) {
        try {
            currentUser = RetrofitClient.apiService.getCurrentUser()
            val myBookings = try { RetrofitClient.apiService.getMyBookings() } catch (e: Exception) { emptyList() }
            val myOffers = if (userRole == "driver") {
                try { RetrofitClient.apiService.getMyPublishedTrips() } catch (e: Exception) { emptyList() }
            } else emptyList()
            
            tripsWithChats = (myBookings + myOffers).distinctBy { it.id }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mes Discussions", fontWeight = FontWeight.Bold) },
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
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = blueColor)
                }
            } else if (tripsWithChats.isEmpty()) {
                EmptyChatsView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(tripsWithChats) { index, trip ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }

                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInHorizontally(initialOffsetX = { -it / 2 }) + fadeIn(animationSpec = tween(300, delayMillis = index * 50))
                        ) {
                            ChatListItemModern(
                                trip = trip,
                                isDriver = userRole == "driver" && trip.driverName == currentUser?.name,
                                onClick = {
                                    if (userRole == "driver" && trip.driverName == currentUser?.name) {
                                        onChatClick(trip.id, -1)
                                    } else {
                                        onChatClick(trip.id, currentUser?.id ?: -1)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItemModern(
    trip: Trip,
    isDriver: Boolean,
    onClick: () -> Unit
) {
    val blueColor = Color(0xFF003399)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(blueColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = blueColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${trip.departureCity} → ${trip.arrivalCity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isDriver) "Gérer les messages passagers" else "Discuter avec ${trip.driverName}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = trip.date?.takeLast(5) ?: "", // Exemple: "12/05"
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
                Spacer(Modifier.height(4.dp))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyChatsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            color = Color(0xFFF0F4FF),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Forum,
                    null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF003399)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Aucun message", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Vos conversations s'afficheront ici.", color = Color.Gray, fontSize = 14.sp)
    }
}
