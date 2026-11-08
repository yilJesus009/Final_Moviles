package com.example.eventia.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.eventia.db.entities.CategoriaCacheEntity
import com.example.eventia.db.entities.EventoCacheEntity

/**
 * DAO para la caché del catálogo y categorías en Room (RF-14).
 */
@Dao
interface EventoCacheDao {

    @Query("SELECT * FROM eventos_cache")
    suspend fun getTodos(): List<EventoCacheEntity>

    @Query("SELECT * FROM eventos_cache WHERE id = :id LIMIT 1")
    suspend fun getPorId(id: Int): EventoCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(eventos: List<EventoCacheEntity>)

    @Query("DELETE FROM eventos_cache")
    suspend fun limpiarEventos()

    @Query("SELECT MAX(guardadoEnMillis) FROM eventos_cache")
    suspend fun getUltimaActualizacionMillis(): Long?

    @Query("SELECT * FROM categorias_cache ORDER BY id ASC")
    suspend fun getCategorias(): List<CategoriaCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCategorias(categorias: List<CategoriaCacheEntity>)

    @Query("DELETE FROM categorias_cache")
    suspend fun limpiarCategorias()
}
