package com.example.eventia.api.models

import com.google.gson.annotations.SerializedName

/**
 * DTO de red: representa un evento tal como lo devuelve la API
 * (GET /events, GET /events/{id}, GET /venues/{id}/events).
 *
 * Todos los campos son nulables a propósito: los datos de prueba incluyen eventos
 * sin imagen, sin coordenadas o con información incompleta, y la app no debe fallar.
 */
data class EventoDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val titulo: String?,
    @SerializedName("summary") val resumen: String?,
    @SerializedName("description") val descripcion: String?,
    @SerializedName("startsAt") val fechaInicio: String?,
    @SerializedName("category") val categoria: CategoriaDto?,
    @SerializedName("imageUrl") val imagenUrl: String?,
    @SerializedName("venue") val lugar: LugarDto?,
    @SerializedName("capacity") val capacidad: Int?,
    @SerializedName("availableSpots") val cuposDisponibles: Int?,
    @SerializedName("isAttending") val estaInscrito: Boolean?
)
