package com.example.eventia.api.models

import com.google.gson.annotations.SerializedName

/**
 * DTOs de autenticación (POST /auth/login, POST /auth/register, GET /me).
 *
 * NOTA: los nombres de los campos JSON ("name", "email", "password", "token", "user")
 * se deben confirmar con la respuesta real del backend cuando tengamos la URL.
 */

// Cuerpo que se envía en POST /auth/login
data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val contrasena: String
)

// Cuerpo que se envía en POST /auth/register
data class RegistroRequestDto(
    @SerializedName("name") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val contrasena: String
)

// Respuesta de login y registro: "Devuelve token y usuario" (sección 8.1)
data class AuthResponseDto(
    @SerializedName("token") val token: String?,
    @SerializedName("user") val usuario: UsuarioDto?
)

// Usuario autenticado (dentro de AuthResponseDto y en GET /me)
data class UsuarioDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val nombre: String?,
    @SerializedName("email") val email: String?
)
