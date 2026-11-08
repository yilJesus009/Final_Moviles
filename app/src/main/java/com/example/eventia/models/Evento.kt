package com.example.eventia.models

import java.time.ZonedDateTime

/**
 * Modelo de dominio de un evento. Es lo único que llega a la UI (los DTO de red nunca salen del repositorio).
 * La fecha ya viene convertida a la zona horaria local del dispositivo.
 */
data class Evento(
    val id: Int,
    val titulo: String,
    val resumen: String,
    val descripcion: String,
    val fechaInicio: ZonedDateTime?,
    val imagenUrl: String?,
    val categoria: Categoria?,
    val lugar: Lugar?,
    val capacidad: Int,
    val cuposDisponibles: Int,
    val estaInscrito: Boolean = false
) {
    val tieneCupos: Boolean
        get() = cuposDisponibles > 0
}
