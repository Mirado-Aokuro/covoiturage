package com.teammirado.waygo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.local.TokenManager
import com.teammirado.waygo.data.model.User
import com.teammirado.waygo.data.model.FcmTokenRequest
import com.teammirado.waygo.ui.screens.*
import com.teammirado.waygo.ui.theme.WayGoTheme
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    
    private val notificationIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationIntent.value = intent
        
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        setContent {
            WayGoApp(notificationIntent.value)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationIntent.value = intent
    }
}

@Composable
fun WayGoApp(intent: Intent?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    
    var isLoggedIn by remember { 
        mutableStateOf(tokenManager.getToken() != null) 
    }
    var currentUser by remember { mutableStateOf<User?>(null) }

    val blueColor = Color(0xFF003399)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) Log.d("FCM", "Permission accordée")
    }

    LaunchedEffect(isLoggedIn, currentUser, intent) {
        if (isLoggedIn && currentUser != null && intent != null) {
            val targetScreen = intent.getStringExtra("target_screen")
            val tripId = intent.getStringExtra("tripId")
            val passengerId = intent.getStringExtra("passengerId")
            val isDriver = intent.getBooleanExtra("isDriver", false)

            if (tripId != null) {
                when (targetScreen) {
                    "chat" -> {
                        val pId = passengerId ?: (if (currentUser!!.role == "driver") "-1" else currentUser!!.id.toString())
                        navController.navigate("chat/$tripId/$pId")
                    }
                    "tracking" -> navController.navigate("tracking/$tripId/$isDriver")
                    "trip_detail" -> navController.navigate("trip_detail/$tripId")
                }
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            RetrofitClient.authToken = tokenManager.getToken()
            try {
                currentUser = RetrofitClient.apiService.getCurrentUser()
                
                // ✅ Connexion Firebase Auto au démarrage si déjà loggé
                FirebaseAuth.getInstance().signInAnonymously()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fcmToken = task.result
                        scope.launch {
                            try {
                                RetrofitClient.apiService.updateFcmToken(FcmTokenRequest(fcmToken))
                            } catch (e: Exception) {
                                Log.e("FCM", "Erreur token: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e.message?.contains("401") == true) {
                    tokenManager.deleteToken()
                    isLoggedIn = false
                }
            }
        }
    }

    WayGoTheme {
        Scaffold(
            bottomBar = {
                val mainRoutes = listOf("home", "search", "chats", "my_trips", "profile")
                val isMainRoute = currentDestination?.split("?")?.get(0)?.split("/")?.get(0) in mainRoutes

                if (isLoggedIn && isMainRoute) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        val items = mutableListOf<Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>>()
                        items.add(Triple("home", "Accueil", Icons.Default.Home))
                        if (currentUser?.role != "driver") items.add(Triple("search", "Recherche", Icons.Default.Search))
                        items.add(Triple("chats", "Messages", Icons.AutoMirrored.Filled.Chat))
                        items.add(Triple("my_trips", if (currentUser?.role == "driver") "Mes Offres" else "Mes Trajets", Icons.Default.History))
                        items.add(Triple("profile", "Profil", Icons.Default.Person))

                        items.forEach { (route, label, icon) ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        icon,
                                        contentDescription = label,
                                        modifier = Modifier.animateContentSize()
                                    )
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                selected = currentDestination?.startsWith(route) == true,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = blueColor,
                                    unselectedIconColor = Color.Gray,
                                    indicatorColor = blueColor.copy(alpha = 0.1f)
                                ),
                                onClick = {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) "home" else "login",
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(400)) },
                exitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(400)) },
                popEnterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(400)) },
                popExitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(400)) }
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { token, userResponse ->
                            tokenManager.saveToken(token)
                            RetrofitClient.authToken = token
                            currentUser = User(id = userResponse.id, name = userResponse.name, email = userResponse.email, role = userResponse.role)
                            isLoggedIn = true
                            navController.navigate("home") { popUpTo("login") { inclusive = true } }
                        },
                        onSignUpClick = { navController.navigate("signup") }
                    )
                }

                composable("signup") {
                    SignUpScreen(
                        onBackClick = { navController.popBackStack() },
                        onSignUpSuccess = { token, userResponse ->
                            tokenManager.saveToken(token)
                            RetrofitClient.authToken = token
                            currentUser = User(id = userResponse.id, name = userResponse.name, email = userResponse.email, role = userResponse.role)
                            isLoggedIn = true
                            navController.navigate("home") { popUpTo("login") { inclusive = true } }
                        }
                    )
                }

                composable("home") {
                    HomeScreen(
                        userRole = currentUser?.role,
                        onTripClick = { trip -> navController.navigate("trip_detail/${trip.id}") },
                        onPublishClick = { navController.navigate("publish_map") },
                        onSearchClick = { navController.navigate("search") },
                        onChatClick = { tripId -> 
                            if (currentUser?.role == "driver") {
                                navController.navigate("trip_detail/$tripId")
                            } else {
                                currentUser?.id?.let { myId ->
                                    navController.navigate("chat/$tripId/$myId")
                                }
                            }
                        }
                    )
                }

                composable(
                    route = "home?dep={dep}&arr={arr}&date={date}&driver={driver}",
                    arguments = listOf(
                        navArgument("dep") { nullable = true; defaultValue = null },
                        navArgument("arr") { nullable = true; defaultValue = null },
                        navArgument("date") { nullable = true; defaultValue = null },
                        navArgument("driver") { nullable = true; defaultValue = null }
                    )
                ) { backStackEntry ->
                    HomeScreen(
                        userRole = currentUser?.role,
                        departureFilter = backStackEntry.arguments?.getString("dep"),
                        arrivalFilter = backStackEntry.arguments?.getString("arr"),
                        dateFilter = backStackEntry.arguments?.getString("date"),
                        driverFilter = backStackEntry.arguments?.getString("driver"),
                        onTripClick = { trip -> navController.navigate("trip_detail/${trip.id}") },
                        onPublishClick = { navController.navigate("publish_map") },
                        onSearchClick = { navController.navigate("search") },
                        onChatClick = { tripId ->
                            if (currentUser?.role == "driver") {
                                navController.navigate("trip_detail/$tripId")
                            } else {
                                currentUser?.id?.let { myId ->
                                    navController.navigate("chat/$tripId/$myId")
                                }
                            }
                        }
                    )
                }

                composable("publish_map") {
                    PublishMapScreen(
                        onPointsSelected = { dLat, dLng, aLat, aLng -> 
                            val depStr = "$dLat,$dLng"
                            val arrStr = "$aLat,$aLng"
                            navController.navigate("publish_details/$depStr/$arrStr")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "publish_details/{dep}/{arr}",
                    arguments = listOf(
                        navArgument("dep") { type = NavType.StringType },
                        navArgument("arr") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    PublishTripDetailsScreen(
                        departure = backStackEntry.arguments?.getString("dep") ?: "",
                        arrival = backStackEntry.arguments?.getString("arr") ?: "",
                        onConfirm = {
                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                
                composable("search") {
                    SearchTripScreen(onSearch = { dep, arr, date, driver ->
                        navController.navigate("home?dep=$dep&arr=$arr&date=$date&driver=$driver")
                    })
                }
                
                composable("chats") {
                    ChatListScreen(
                        userRole = currentUser?.role,
                        onChatClick = { tripId, passengerId -> 
                            if (passengerId == -1) {
                                navController.navigate("trip_detail/$tripId")
                            } else {
                                navController.navigate("chat/$tripId/$passengerId") 
                            }
                        }
                    )
                }

                composable("my_trips") {
                    MyTripsScreen(
                        userRole = currentUser?.role,
                        onTripClick = { trip -> navController.navigate("trip_detail/${trip.id}") },
                        onChatClick = { tripId ->
                            if (currentUser?.role == "driver") {
                                navController.navigate("trip_detail/$tripId")
                            } else {
                                currentUser?.id?.let { myId ->
                                    navController.navigate("chat/$tripId/$myId")
                                }
                            }
                        }
                    )
                }
                
                composable("profile") {
                    ProfileScreen(
                        userRole = currentUser?.role,
                        onSettingsClick = { navController.navigate("settings") },
                        onLogout = {
                            tokenManager.deleteToken()
                            RetrofitClient.authToken = null
                            currentUser = null
                            isLoggedIn = false
                            navController.navigate("login") { 
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                }

                composable("settings") { SettingsScreen(onBack = { navController.popBackStack() }) }
                
                composable(
                    "trip_detail/{tripId}",
                    arguments = listOf(navArgument("tripId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getInt("tripId") ?: -1
                    TripDetailScreen(
                        tripId = tripId, 
                        userRole = currentUser?.role,
                        currentUserId = currentUser?.id ?: -1,
                        onBack = { navController.popBackStack() },
                        onBook = { navController.navigate("booking_confirmation/$tripId") },
                        onChatClick = { tId, pId -> 
                            navController.navigate("chat/$tId/$pId") 
                        },
                        onTrackingClick = { tId, isDr ->
                            navController.navigate("tracking/$tId/$isDr")
                        }
                    )
                }

                composable(
                    "booking_confirmation/{tripId}",
                    arguments = listOf(navArgument("tripId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getInt("tripId") ?: -1
                    BookingConfirmationScreen(
                        tripId = tripId,
                        onConfirm = {
                            navController.navigate("my_trips") { popUpTo("home") }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "chat/{tripId}/{passengerId}",
                    arguments = listOf(
                        navArgument("tripId") { type = NavType.IntType },
                        navArgument("passengerId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    ChatScreen(
                        tripId = backStackEntry.arguments?.getInt("tripId") ?: -1,
                        passengerId = backStackEntry.arguments?.getInt("passengerId") ?: -1,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "tracking/{tripId}/{isDriver}",
                    arguments = listOf(
                        navArgument("tripId") { type = NavType.IntType },
                        navArgument("isDriver") { type = NavType.BoolType }
                    )
                ) { backStackEntry ->
                    TrackingScreen(
                        tripId = backStackEntry.arguments?.getInt("tripId") ?: -1,
                        isDriver = backStackEntry.arguments?.getBoolean("isDriver") ?: false,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
