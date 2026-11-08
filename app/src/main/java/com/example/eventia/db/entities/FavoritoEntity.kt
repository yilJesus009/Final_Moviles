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
 * Entidad Room para los eventos favoritos del usuario (RF-04 y RF-05).
 * Almacena toda la información básica del evento para que sea accesible sin conexión
 * incluso si el catálogo se actualiza o limpia.
 */
@Entity(tableName = "favoritos")
data class FavoritoEntity(
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
    val fechaAgregadoMillis: Long = System.currentTimeMillis()
)

fun Evento.aFavoritoEntity(fechaAgregadoMillis: Long = System.currentTimeMillis()) = FavoritoEntity(
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
    fechaAgregadoMillis = fechaAgregadoMillis
)

fun FavoritoEntity.aDominio() = Evento(
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
    cuposDisponibles = cuposDisponibles
)
