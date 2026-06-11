package com.teammirado.waygo.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.model.Trip
import com.teammirado.waygo.data.model.User
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = "",
    val senderId: Int = 0,
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

@Composable
fun ChatScreen(
    tripId: Int, 
    passengerId: Int,
    onBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf<User?>(null) }
    var trip by remember { mutableStateOf<Trip?>(null) }
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    val blueColor = Color(0xFF003399)

    val conversationId = "${tripId}_$passengerId"

    LaunchedEffect(tripId) {
        try {
            currentUser = RetrofitClient.apiService.getCurrentUser()
            trip = RetrofitClient.apiService.getTripDetails(tripId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(conversationId) {
        val query = db.collection("conversations")
            .document(conversationId)
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
                if (messages.isNotEmpty()) {
                    scope.launch {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
        }
        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val titleText = if (currentUser?.role == "driver") "Passager" else (trip?.driverName ?: "Chargement...")
                            Text(titleText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (trip != null) {
                                // ✅ Correction : Utilisation des nouveaux noms uniformisés
                                Text("${trip!!.departureCity} → ${trip!!.arrivalCity}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = blueColor)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Écrivez votre message...") },
                        shape = RoundedCornerShape(28.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = blueColor,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                            focusedContainerColor = Color(0xFFF7F9FC),
                            unfocusedContainerColor = Color(0xFFF7F9FC)
                        )
                    )
                    
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank() && currentUser != null) {
                                val msgData = hashMapOf(
                                    "senderId" to currentUser!!.id,
                                    "senderName" to currentUser!!.name,
                                    "text" to messageText,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                db.collection("conversations")
                                    .document(conversationId)
                                    .collection("messages")
                                    .add(msgData)
                                messageText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = blueColor,
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isMine = message.senderId == currentUser?.id
                    
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(animationSpec = spring(Spring.DampingRatioLowBouncy)) + fadeIn()
                    ) {
                        MessageBubbleModern(message, isMine)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubbleModern(message: ChatMessage, isMine: Boolean) {
    val blueGradient = Brush.linearGradient(listOf(Color(0xFF003399), Color(0xFF0052CC)))
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(if (isMine) 2.dp else 1.dp, RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isMine) 20.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 20.dp
                )),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isMine) 20.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 20.dp
            ),
            color = if (isMine) Color.Transparent else Color.White
        ) {
            Box(
                modifier = Modifier
                    .background(if (isMine) blueGradient else Brush.linearGradient(listOf(Color.White, Color.White)))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isMine) Color.White else Color.Black,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
        
        Text(
            text = if (isMine) "Moi" else message.senderName,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp, start = 6.dp, end = 6.dp)
        )
    }
}
