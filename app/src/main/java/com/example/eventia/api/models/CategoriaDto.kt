package com.example.eventia.api.models

import com.google.gson.annotations.SerializedName

/**
 * DTO de red: representa una categoría tal como la devuelve la API (GET /categories).
 * Los campos son nulables porque el backend puede omitirlos; la limpieza se hace en el mapeo a dominio.
 */
data class CategoriaDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val nombre: String?
)
