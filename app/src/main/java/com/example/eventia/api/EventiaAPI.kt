package com.example.eventia.api

import com.example.eventia.api.models.CategoriaDto
import com.example.eventia.api.models.EventoDto
import com.example.eventia.api.models.LugarDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Definición de los endpoints del backend Eventia (sección 8.1 del enunciado).
 * Por ahora solo contiene los endpoints públicos del catálogo y de lugares;
 * los de autenticación e inscripciones se agregarán en el Módulo D.
 */
interface EventiaAPI {
    @GET("categories")
    suspend fun getCategorias(): List<CategoriaDto>

    // Si "busqueda" o "categoriaId" son null, Retrofit no los envía en la URL
    @GET("events")
    suspend fun getEventos(
        @Query("q") busqueda: String? = null,
        @Query("category") categoriaId: Int? = null
    ): List<EventoDto>

    @GET("events/{id}")
    suspend fun getEventoPorId(@Path("id") id: Int): EventoDto

    @GET("venues")
    suspend fun getLugares(): List<LugarDto>

    @GET("venues/{id}")
    suspend fun getLugarPorId(@Path("id") id: Int): LugarDto

    @GET("venues/{id}/events")
    suspend fun getEventosDeLugar(@Path("id") id: Int): List<EventoDto>
}
