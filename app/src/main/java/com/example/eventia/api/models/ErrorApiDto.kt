package com.example.eventia.api.models

import com.google.gson.annotations.SerializedName

/**
 * DTO del cuerpo de error que devuelve el backend:
 * { "code": "...", "message": "...", "fields": { "email": "..." } }
 */
data class ErrorApiDto(
    @SerializedName("code") val codigo: String?,
    @SerializedName("message") val mensaje: String?,
    @SerializedName("fields") val campos: Map<String, String>?
)
