package com.example.eventia.models

/**
 * Modelo de dominio de un lugar (venue).
 * Las coordenadas siguen siendo nulables porque hay lugares sin ubicación;
 * se debe consultar [tieneCoordenadasValidas] antes de dibujarlos en el mapa.
 */
data class Lugar(
    val id: Int,
    val nombre: String,
    val direccion: String,
    val latitud: Double?,
    val longitud: Double?,
    val descripcion: String = "",
    val horario: String = "",
    val imagenUrl: String? = null,
    val cantidadEventosProximos: Int = 0
) {
    // Evita marcadores incorrectos: ambas coordenadas deben existir, estar en rango y no ser (0,0)
    val tieneCoordenadasValidas: Boolean
        get() {
            val lat = latitud ?: return false
            val lng = longitud ?: return false
            val enRango = lat in -90.0..90.0 && lng in -180.0..180.0
            val esOrigen = lat == 0.0 && lng == 0.0
            return enRango && !esOrigen
        }
}
