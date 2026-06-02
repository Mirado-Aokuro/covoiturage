package com.teammirado.waygo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teammirado.waygo.data.api.RetrofitClient
import com.teammirado.waygo.data.local.TokenManager
import com.teammirado.waygo.ui.screens.*
import com.teammirado.waygo.ui.theme.WayGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WayGoApp()
        }
    }
}

@Composable
fun WayGoApp() {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    
    var isLoggedIn by remember { 
        mutableStateOf(tokenManager.getToken() != null) 
    }

    LaunchedEffect(Unit) {
        RetrofitClient.authToken = tokenManager.getToken()
    }

    WayGoTheme {
        Scaffold(
            bottomBar = {
                val mainRoutes = listOf("home", "search", "my_trips", "profile")
                if (isLoggedIn && currentDestination?.split("?")?.get(0)?.split("/")?.get(0) in mainRoutes) {
                    NavigationBar {
                        val items = listOf(
                            Triple("home", "Accueil", Icons.Default.Home),
                            Triple("search", "Recherche", Icons.Default.Search),
                            Triple("my_trips", "Trajets", Icons.Default.History),
                            Triple("profile", "Profil", Icons.Default.Person)
                        )

                        items.forEach { (route, label, icon) ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                selected = currentDestination?.startsWith(route) == true,
                                onClick = {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
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
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { token ->
                            tokenManager.saveToken(token)
                            RetrofitClient.authToken = token
                            isLoggedIn = true
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onSignUpClick = { navController.navigate("signup") }
                    )
                }

                composable("signup") {
                    SignUpScreen(
                        onBackClick = { navController.popBackStack() },
                        onSignUpSuccess = { token ->
                            tokenManager.saveToken(token)
                            RetrofitClient.authToken = token
                            isLoggedIn = true
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "home?dep={dep}&arr={arr}&date={date}",
                    arguments = listOf(
                        navArgument("dep") { nullable = true; defaultValue = null },
                        navArgument("arr") { nullable = true; defaultValue = null },
                        navArgument("date") { nullable = true; defaultValue = null }
                    )
                ) { backStackEntry ->
                    HomeScreen(
                        departureFilter = backStackEntry.arguments?.getString("dep"),
                        arrivalFilter = backStackEntry.arguments?.getString("arr"),
                        dateFilter = backStackEntry.arguments?.getString("date"),
                        onTripClick = { trip -> navController.navigate("trip_detail/${trip.id}") },
                        onPublishClick = { navController.navigate("publish_map") },
                        onSearchClick = { navController.navigate("search") }
                    )
                }
                
                composable("home") {
                    HomeScreen(
                        onTripClick = { trip -> navController.navigate("trip_detail/${trip.id}") },
                        onPublishClick = { navController.navigate("publish_map") },
                        onSearchClick = { navController.navigate("search") }
                    )
                }

                composable("publish_map") {
                    PublishMapScreen(
                        onPointsSelected = { dep, arr -> 
                            val depStr = "${dep.latitude},${dep.longitude}"
                            val arrStr = "${arr.latitude},${arr.longitude}"
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
                    SearchTripScreen(onSearch = { dep, arr, date ->
                        navController.navigate("home?dep=$dep&arr=$arr&date=$date")
                    })
                }
                
                composable("my_trips") {
                    MyTripsScreen(
                        onTripClick = { trip -> navController.navigate("trip_detail/${trip.id}") },
                        onChatClick = { tripId -> navController.navigate("chat/$tripId") }
                    )
                }
                
                composable("profile") {
                    ProfileScreen(
                        onSettingsClick = { navController.navigate("settings") },
                        onLogout = {
                            tokenManager.deleteToken()
                            RetrofitClient.authToken = null
                            isLoggedIn = false
                            navController.navigate("login") { 
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                }

                composable("settings") {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
                
                composable(
                    "trip_detail/{tripId}",
                    arguments = listOf(navArgument("tripId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getInt("tripId") ?: -1
                    TripDetailScreen(
                        tripId = tripId, 
                        onBack = { navController.popBackStack() },
                        onBook = { navController.navigate("booking_confirmation/$tripId") }
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
                    "chat/{tripId}",
                    arguments = listOf(navArgument("tripId") { type = NavType.IntType })
                ) { backStackEntry ->
                    ChatScreen(
                        tripId = backStackEntry.arguments?.getInt("tripId") ?: -1,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
