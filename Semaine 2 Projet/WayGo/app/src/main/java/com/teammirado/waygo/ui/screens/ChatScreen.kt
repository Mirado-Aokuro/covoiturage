package com.teammirado.waygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip
import com.teammirado.waygo.data.model.User

data class ChatMessage(
    val id: String = "",
    val senderId: Int = 0,
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

@Composable
fun ChatScreen(tripId: Int, onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var currentUser by remember { mutableStateOf<User?>(null) }
    var trip by remember { mutableStateOf<Trip?>(null) }
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }

    LaunchedEffect(tripId) {
        try {
            currentUser = RetrofitClient.apiService.getCurrentUser()
            trip = RetrofitClient.apiService.getTripDetails(tripId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(tripId) {
        val query = db.collection("trips")
            .document(tripId.toString())
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                val newMessages = snapshot.documents.mapNotNull { doc ->
                    val msg = doc.toObject(ChatMessage::class.java)
                    msg?.copy(id = doc.id)
                }
                messages.clear()
                messages.addAll(newMessages)
            }
        }
        
        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(trip?.driverName ?: "Chat", style = MaterialTheme.typography.titleMedium)
                        Text(if (trip != null) "${trip!!.departure} → ${trip!!.arrival}" else "Chargement...", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF003399),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Votre message...") },
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank() && currentUser != null) {
                                val msgData = hashMapOf(
                                    "senderId" to currentUser!!.id,
                                    "senderName" to currentUser!!.name,
                                    "text" to messageText,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                db.collection("trips")
                                    .document(tripId.toString())
                                    .collection("messages")
                                    .add(msgData)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank() && currentUser != null,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF003399))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            reverseLayout = false,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message, isMine = message.senderId == currentUser?.id)
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isMine: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMine) Color(0xFF003399) else Color(0xFFF0F0F0),
            contentColor = if (isMine) Color.White else Color.Black,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 0.dp,
                bottomEnd = if (isMine) 0.dp else 16.dp
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = if (isMine) "Moi" else message.senderName,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
