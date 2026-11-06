package com.example.eventia.navigation

sealed class Screen(val route: String, val title: String) {
    // 4 destinos principales de la barra inferior
    data object Catalog : Screen("catalog", "Explorar")
    data object Map : Screen("map", "Mapa")
    data object Favorites : Screen("favorites", "Favoritos")
    data object Profile : Screen("profile", "Perfil")

    // Pantallas secundarias
    data object EventDetail : Screen("event_detail/{eventId}", "Detalle de Evento") {
        fun createRoute(eventId: Int) = "event_detail/$eventId"
    }
    data object VenueDetail : Screen("venue_detail/{venueId}", "Detalle de Lugar") {
        fun createRoute(venueId: Int) = "venue_detail/$venueId"
    }
    data object Login : Screen("login", "Iniciar Sesión")
    data object Register : Screen("register", "Registrarse")
}

val bottomNavItems = listOf(
    Screen.Catalog,
    Screen.Map,
    Screen.Favorites,
    Screen.Profile
)
