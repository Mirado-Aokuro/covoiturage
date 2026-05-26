package com.teammirado.waygo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teammirado.waygo.data.MockData
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WayGoApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    WayGoTheme {
        Scaffold(
            bottomBar = {
                // Liste des routes principales pour la barre de navigation
                val mainRoutes = listOf("home", "search", "map", "my_trips", "profile")
                
                if (currentDestination in mainRoutes) {
                    NavigationBar {
                        val items = listOf(
                            Triple("home", "Accueil", Icons.Default.Home),
                            Triple("search", "Rechercher", Icons.Default.Search),
                            Triple("map", "Carte", Icons.Default.Map),
                            Triple("my_trips", "Mes Trajets", Icons.Default.History),
                            Triple("profile", "Profil", Icons.Default.Person)
                        )

                        items.forEach { (route, label, icon) ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                selected = currentDestination == route,
                                onClick = {
                                    navController.navigate(route) {
                                        // Évite d'empiler les destinations si on clique plusieurs fois
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
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                // Écran d'Accueil
                composable("home") {
                    HomeScreen(onTripClick = { trip ->
                        navController.navigate("trip_detail/${trip.id}")
                    })
                }
                
                // Écran de Recherche
                composable("search") {
                    SearchTripScreen(onSearch = { departure, arrival ->
                        // Ici on simule une recherche en retournant à l'accueil
                        navController.navigate("home")
                    })
                }
                
                // Écran Carte (Google Maps SDK)
                composable("map") {
                    MapScreen()
                }
                
                // Écran Mes Trajets
                composable("my_trips") {
                    MyTripsScreen()
                }
                
                // Écran Profil
                composable("profile") {
                    ProfileScreen()
                }
                
                // Écran Détail du trajet (avec argument ID)
                composable(
                    "trip_detail/{tripId}",
                    arguments = listOf(navArgument("tripId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getInt("tripId")
                    val trip = MockData.trips.find { it.id == tripId }
                    TripDetailScreen(trip = trip, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
