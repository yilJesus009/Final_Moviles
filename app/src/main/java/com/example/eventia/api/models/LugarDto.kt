package com.example.eventia.api.models

import com.google.gson.annotations.SerializedName

/**
 * DTO de red: representa un lugar (venue) de la API.
 * Se usa tanto embebido dentro de un evento como en GET /venues y GET /venues/{id}.
 *
 * NOTA: los campos de detalle (descripción, horario, imagen, cantidad de eventos) se completarán
 * con los nombres exactos cuando tengamos acceso a la respuesta real del backend.
 */
data class LugarDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val nombre: String?,
    @SerializedName("address") val direccion: String?,
    @SerializedName("lat") val latitud: Double?,
    @SerializedName("lng") val longitud: Double?,
    @SerializedName("description") val descripcion: String?,
    @SerializedName("openingHours") val horario: String?,
    @SerializedName("imageUrl") val imagenUrl: String?,
    @SerializedName("upcomingEventsCount") val cantidadEventosProximos: Int?
)
