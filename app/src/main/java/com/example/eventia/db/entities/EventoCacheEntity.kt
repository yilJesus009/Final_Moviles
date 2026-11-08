package com.example.eventia.db.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.example.eventia.models.Categoria
import com.example.eventia.models.Evento
import com.example.eventia.models.Lugar
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Entidad Room para la caché del catálogo de eventos (RF-14).
 * Guarda el último catálogo descargado junto con la marca de tiempo (guardadoEnMillis)
 * para mostrar el aviso "Sin conexión · datos de hace X min/h".
 */
@Entity(tableName = "eventos_cache")
data class EventoCacheEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val resumen: String,
    val descripcion: String,
    val fechaInicioEpochSecond: Long?,
    val imagenUrl: String?,
    val categoriaId: Int?,
    val categoriaNombre: String?,
    val lugarId: Int?,
    val lugarNombre: String?,
    val lugarDireccion: String?,
    val lugarLatitud: Double?,
    val lugarLongitud: Double?,
    val capacidad: Int,
    val cuposDisponibles: Int,
    val estaInscrito: Boolean,
    val guardadoEnMillis: Long
)

fun Evento.aCacheEntity(guardadoEnMillis: Long = System.currentTimeMillis()) = EventoCacheEntity(
    id = id,
    titulo = titulo,
    resumen = resumen,
    descripcion = descripcion,
    fechaInicioEpochSecond = fechaInicio?.toEpochSecond(),
    imagenUrl = imagenUrl,
    categoriaId = categoria?.id,
    categoriaNombre = categoria?.nombre,
    lugarId = lugar?.id,
    lugarNombre = lugar?.nombre,
    lugarDireccion = lugar?.direccion,
    lugarLatitud = lugar?.latitud,
    lugarLongitud = lugar?.longitud,
    capacidad = capacidad,
    cuposDisponibles = cuposDisponibles,
    estaInscrito = estaInscrito,
    guardadoEnMillis = guardadoEnMillis
)

fun EventoCacheEntity.aDominio() = Evento(
    id = id,
    titulo = titulo,
    resumen = resumen,
    descripcion = descripcion,
    fechaInicio = fechaInicioEpochSecond?.let {
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneId.systemDefault())
    },
    imagenUrl = imagenUrl,
    categoria = if (categoriaId != null && categoriaNombre != null) {
        Categoria(id = categoriaId, nombre = categoriaNombre)
    } else null,
    lugar = if (lugarId != null && lugarNombre != null) {
        Lugar(
            id = lugarId,
            nombre = lugarNombre,
            direccion = lugarDireccion.orEmpty(),
            latitud = lugarLatitud,
            longitud = lugarLongitud
        )
    } else null,
    capacidad = capacidad,
    cuposDisponibles = cuposDisponibles,
    estaInscrito = estaInscrito
)
