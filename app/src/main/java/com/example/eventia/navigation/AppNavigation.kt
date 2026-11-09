package com.example.eventia.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventia.ui.screens.auth.LoginScreen
import com.example.eventia.ui.screens.auth.RegistroScreen
import com.example.eventia.ui.screens.catalog.CatalogScreen
import com.example.eventia.ui.screens.detail.DetalleEventoScreen
import com.example.eventia.ui.screens.favorites.FavoritesScreen
import com.example.eventia.ui.screens.map.MapScreen
import com.example.eventia.ui.screens.profile.ProfileScreen
import com.example.eventia.ui.screens.venues.DetalleLugarScreen
import com.example.eventia.ui.screens.venues.LugaresScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Text(screen.title.take(1))
                        },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Catalog.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Catalog.route) {
                CatalogScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    },
                    onVenuesClick = {
                        navController.navigate(Screen.Venues.route)
                    }
                )
            }
            composable(
                route = Screen.EventDetail.route,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.IntType }
                )
            ) {
                DetalleEventoScreen(
                    onBackClick = { navController.popBackStack() },
                    onVenueClick = { venueId ->
                        navController.navigate(Screen.VenueDetail.createRoute(venueId))
                    },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
            composable(Screen.Venues.route) {
                LugaresScreen(
                    onBackClick = { navController.popBackStack() },
                    onVenueClick = { venueId ->
                        navController.navigate(Screen.VenueDetail.createRoute(venueId))
                    }
                )
            }
            composable(
                route = Screen.VenueDetail.route,
                arguments = listOf(
                    navArgument("venueId") { type = NavType.IntType }
                )
            ) {
                DetalleLugarScreen(
                    onBackClick = { navController.popBackStack() },
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    }
                )
            }
            composable(Screen.Map.route) {
                MapScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    },
                    onRegistroClick = {
                        navController.navigate(Screen.Register.route)
                    },
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventDetail.createRoute(eventId))
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onBackClick = { navController.popBackStack() },
                    onLoginExitoso = { navController.popBackStack() },
                    onRegistroClick = {
                        // Reemplaza Login por Registro para que al terminar regrese directo a la pantalla previa
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegistroScreen(
                    onBackClick = { navController.popBackStack() },
                    onRegistroExitoso = { navController.popBackStack() }
                )
            }
        }
    }
}
