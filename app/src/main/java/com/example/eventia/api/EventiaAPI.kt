package com.example.eventia.api

import com.example.eventia.api.models.AuthResponseDto
import com.example.eventia.api.models.CategoriaDto
import com.example.eventia.api.models.EventoDto
import com.example.eventia.api.models.LoginRequestDto
import com.example.eventia.api.models.LugarDto
import com.example.eventia.api.models.RegistroRequestDto
import com.example.eventia.api.models.UsuarioDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Definición de los endpoints del backend Eventia (sección 8.1 del enunciado).
 *
 * Los endpoints que requieren sesión reciben el encabezado "Authorization: Bearer <token>"
 * como parámetro @Header. Si el valor es null, Retrofit simplemente no envía el encabezado.
 */
interface EventiaAPI {

    // ---------- Catálogo y lugares (públicos) ----------

    @GET("categories")
    suspend fun getCategorias(): List<CategoriaDto>

    // Si "busqueda" o "categoriaId" son null, Retrofit no los envía en la URL
    @GET("events")
    suspend fun getEventos(
        @Query("q") busqueda: String? = null,
        @Query("category") categoriaId: Int? = null
    ): List<EventoDto>

    // Sesión opcional: con token la respuesta incluye "isAttending"
    @GET("events/{id}")
    suspend fun getEventoPorId(
        @Path("id") id: Int,
        @Header("Authorization") autorizacion: String? = null
    ): EventoDto

    @GET("venues")
    suspend fun getLugares(): List<LugarDto>

    @GET("venues/{id}")
    suspend fun getLugarPorId(@Path("id") id: Int): LugarDto

    @GET("venues/{id}/events")
    suspend fun getEventosDeLugar(@Path("id") id: Int): List<EventoDto>

    // ---------- Cuenta (Módulo D · RF-11) ----------

    @POST("auth/login")
    suspend fun login(@Body datos: LoginRequestDto): AuthResponseDto

    @POST("auth/register")
    suspend fun registrar(@Body datos: RegistroRequestDto): AuthResponseDto

    @GET("me")
    suspend fun getPerfil(@Header("Authorization") autorizacion: String): UsuarioDto

    // ---------- Inscripciones (Módulo D · RF-12) ----------

    @POST("events/{id}/attendance")
    suspend fun inscribirse(
        @Path("id") eventoId: Int,
        @Header("Authorization") autorizacion: String
    )

    @DELETE("events/{id}/attendance")
    suspend fun cancelarInscripcion(
        @Path("id") eventoId: Int,
        @Header("Authorization") autorizacion: String
    )

    @GET("me/attendances")
    suspend fun getMisInscripciones(@Header("Authorization") autorizacion: String): List<EventoDto>
}
