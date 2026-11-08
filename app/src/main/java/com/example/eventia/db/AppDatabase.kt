package com.example.eventia.db

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.example.eventia.db.dao.EventoCacheDao
import com.example.eventia.db.dao.FavoritoDao
import com.example.eventia.db.entities.CategoriaCacheEntity
import com.example.eventia.db.entities.EventoCacheEntity
import com.example.eventia.db.entities.FavoritoEntity

/**
 * Base de datos local de Eventia en Room 3 (Módulo C: RF-04, RF-05, RF-14).
 * Sigue el patrón de AppDatabase de PracticaRoom del docente.
 */
@Database(
    entities = [
        EventoCacheEntity::class,
        FavoritoEntity::class,
        CategoriaCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventoCacheDao(): EventoCacheDao
    abstract fun favoritoDao(): FavoritoDao

    companion object {
        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder<AppDatabase>(context, "eventia-db")
                .build()
        }
    }
}
