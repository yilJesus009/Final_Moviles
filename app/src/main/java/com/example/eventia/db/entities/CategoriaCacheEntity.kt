package com.example.eventia.db.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.example.eventia.models.Categoria

/**
 * Entidad Room para guardar las categorías en caché local y permitir filtrar sin conexión.
 */
@Entity(tableName = "categorias_cache")
data class CategoriaCacheEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)

fun Categoria.aCacheEntity() = CategoriaCacheEntity(
    id = id,
    nombre = nombre
)

fun CategoriaCacheEntity.aDominio() = Categoria(
    id = id,
    nombre = nombre
)
