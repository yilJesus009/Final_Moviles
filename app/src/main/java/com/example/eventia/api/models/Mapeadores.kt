package com.example.eventia.api.models

import com.example.eventia.models.Categoria
import com.example.eventia.models.Evento
import com.example.eventia.models.Lugar
import com.example.eventia.models.Usuario
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Funciones de extensión que transforman los DTO de red en modelos de dominio
 * (mismo estilo que toContactUi() en PracticaRoom del docente).
 * Aquí se limpian los nulos para que la UI reciba siempre datos consistentes.
 */

fun UsuarioDto.aDominio() = Usuario(
    id = id ?: 0,
    nombre = nombre.orEmpty(),
    email = email.orEmpty()
)

fun CategoriaDto.aDominio() = Categoria(
    id = id ?: 0,
    nombre = nombre.orEmpty()
)

fun LugarDto.aDominio() = Lugar(
    id = id ?: 0,
    nombre = nombre.orEmpty(),
    direccion = direccion.orEmpty(),
    latitud = latitud,
    longitud = longitud,
    descripcion = descripcion.orEmpty(),
    horario = horario.orEmpty(),
    imagenUrl = imagenUrl?.takeIf { it.isNotBlank() },
    cantidadEventosProximos = cantidadEventosProximos ?: 0
)

fun EventoDto.aDominio() = Evento(
    id = id ?: 0,
    titulo = titulo.orEmpty(),
    resumen = resumen.orEmpty(),
    descripcion = descripcion ?: resumen.orEmpty(),
    fechaInicio = convertirFechaLocal(fechaInicio),
    imagenUrl = imagenUrl?.takeIf { it.isNotBlank() },
    categoria = categoria?.aDominio(),
    lugar = lugar?.aDominio(),
    capacidad = capacidad ?: 0,
    cuposDisponibles = cuposDisponibles ?: 0,
    estaInscrito = estaInscrito ?: false
)

/**
 * Convierte una fecha ISO-8601 con zona horaria (ej. "2026-11-14T20:00:00-04:00")
 * a la zona horaria local del dispositivo. Si el formato es inválido devuelve null en vez de fallar.
 */
fun convertirFechaLocal(fechaIso: String?): ZonedDateTime? {
    if (fechaIso.isNullOrBlank()) return null
    return try {
        OffsetDateTime.parse(fechaIso).atZoneSameInstant(ZoneId.systemDefault())
    } catch (e: Exception) {
        null
    }
}
