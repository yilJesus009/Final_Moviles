package com.example.eventia.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.eventia.db.entities.FavoritoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para los eventos favoritos persistentes en Room (RF-04 y RF-05).
 */
@Dao
interface FavoritoDao {

    @Query("SELECT * FROM favoritos ORDER BY fechaAgregadoMillis DESC")
    fun observarFavoritos(): Flow<List<FavoritoEntity>>

    @Query("SELECT id FROM favoritos")
    fun observarIdsFavoritos(): Flow<List<Int>>

    @Query("SELECT * FROM favoritos WHERE id = :id LIMIT 1")
    suspend fun getPorId(id: Int): FavoritoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(favorito: FavoritoEntity)

    @Query("DELETE FROM favoritos WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}
