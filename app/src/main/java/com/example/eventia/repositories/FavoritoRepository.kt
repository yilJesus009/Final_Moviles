package com.example.eventia.repositories

import com.example.eventia.db.dao.FavoritoDao
import com.example.eventia.db.entities.aDominio
import com.example.eventia.db.entities.aFavoritoEntity
import com.example.eventia.models.Evento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Abstracción del repositorio de favoritos en Room (Módulo C: RF-04 y RF-05).
 * Todas las operaciones funcionan localmente sin requerir conexión a Internet.
 */
interface FavoritoRepository {
    fun observarFavoritos(): Flow<List<Evento>>
    fun observarIdsFavoritos(): Flow<Set<Int>>
    suspend fun esFavorito(eventoId: Int): Boolean
    suspend fun agregarFavorito(evento: Evento)
    suspend fun quitarFavorito(eventoId: Int)
    suspend fun alternarFavorito(evento: Evento): Boolean
}

class FavoritoRepositoryImpl(
    private val favoritoDao: FavoritoDao
) : FavoritoRepository {

    override fun observarFavoritos(): Flow<List<Evento>> {
        return favoritoDao.observarFavoritos().map { lista ->
            lista.map { it.aDominio() }
        }
    }

    override fun observarIdsFavoritos(): Flow<Set<Int>> {
        return favoritoDao.observarIdsFavoritos().map { it.toSet() }
    }

    override suspend fun esFavorito(eventoId: Int): Boolean {
        return favoritoDao.getPorId(eventoId) != null
    }

    override suspend fun agregarFavorito(evento: Evento) {
        favoritoDao.insertar(evento.aFavoritoEntity())
    }

    override suspend fun quitarFavorito(eventoId: Int) {
        favoritoDao.eliminarPorId(eventoId)
    }

    override suspend fun alternarFavorito(evento: Evento): Boolean {
        return if (esFavorito(evento.id)) {
            quitarFavorito(evento.id)
            false
        } else {
            agregarFavorito(evento)
            true
        }
    }
}
