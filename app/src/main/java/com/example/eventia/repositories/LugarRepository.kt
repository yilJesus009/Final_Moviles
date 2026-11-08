package com.example.eventia.repositories

import com.example.eventia.api.EventiaAPI
import com.example.eventia.api.models.aDominio
import com.example.eventia.models.Evento
import com.example.eventia.models.Lugar
import java.time.ZonedDateTime

/**
 * Abstracción del repositorio de lugares (venues) para el Módulo B (RF-13).
 * Los ViewModel dependen únicamente de esta interfaz.
 */
interface LugarRepository {
    suspend fun getLugares(): List<Lugar>
    suspend fun getLugarPorId(id: Int): Lugar
    suspend fun getEventosDeLugar(lugarId: Int): List<Evento>
}

/**
 * Implementación que consume los endpoints /venues, /venues/{id} y /venues/{id}/events.
 */
class LugarRepositoryImpl(
    private val api: EventiaAPI
) : LugarRepository {

    override suspend fun getLugares(): List<Lugar> {
        return api.getLugares().map { it.aDominio() }
    }

    override suspend fun getLugarPorId(id: Int): Lugar {
        return api.getLugarPorId(id).aDominio()
    }

    override suspend fun getEventosDeLugar(lugarId: Int): List<Evento> {
        val ahora = ZonedDateTime.now()
        return api.getEventosDeLugar(lugarId)
            .map { it.aDominio() }
            .filter { evento -> evento.fechaInicio == null || evento.fechaInicio.isAfter(ahora) }
            .sortedBy { it.fechaInicio }
    }
}
