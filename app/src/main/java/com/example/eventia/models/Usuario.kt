package com.example.eventia.models

/**
 * Modelo de dominio del usuario autenticado.
 */
data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String
)
