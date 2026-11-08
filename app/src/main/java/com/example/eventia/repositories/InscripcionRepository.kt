package com.example.eventia.repositories

import com.example.eventia.api.EventiaAPI
import com.example.eventia.api.models.aDominio
import com.example.eventia.datastore.SesionDataStore
import com.example.eventia.models.Evento
import retrofit2.HttpException

/**
 * Repositorio de inscripciones a eventos (Módulo D · RF-12).
 * Traduce los códigos HTTP a errores de negocio:
 * - 401 → borra la sesión local y lanza [SesionExpiradaException]
 * - 409 → lanza [ConflictoException] (sin cupos o ya inscrito)
 */
interface InscripcionRepository {
    suspend fun inscribirse(eventoId: Int)
    suspend fun cancelarInscripcion(eventoId: Int)
    suspend fun getMisInscripciones(): List<Evento>
}

class InscripcionRepositoryImpl(
    private val api: EventiaAPI,
    private val sesion: SesionDataStore
) : InscripcionRepository {

    override suspend fun inscribirse(eventoId: Int) {
        val autorizacion = obtenerAutorizacion()
        try {
            api.inscribirse(eventoId, autorizacion)
        } catch (e: HttpException) {
            throw convertirError(e)
        }
    }

    override suspend fun cancelarInscripcion(eventoId: Int) {
        val autorizacion = obtenerAutorizacion()
        try {
            api.cancelarInscripcion(eventoId, autorizacion)
        } catch (e: HttpException) {
            throw convertirError(e)
        }
    }

    override suspend fun getMisInscripciones(): List<Evento> {
        val autorizacion = obtenerAutorizacion()
        try {
            return api.getMisInscripciones(autorizacion)
                .map { it.aDominio() }
                .sortedBy { it.fechaInicio }
        } catch (e: HttpException) {
            throw convertirError(e)
        }
    }

    // Arma el encabezado "Bearer <token>"; si no hay sesión ni siquiera llama a la API
    private suspend fun obtenerAutorizacion(): String {
        val token = sesion.obtenerToken()
        if (token.isNullOrBlank()) throw SinSesionException()
        return "Bearer $token"
    }

    private suspend fun convertirError(e: HttpException): Exception {
        return when (e.code()) {
            401 -> {
                sesion.cerrarSesion()
                SesionExpiradaException()
            }
            409 -> {
                val mensaje = e.leerErrorApi()?.mensaje
                    ?: "No hay cupos disponibles o ya estás inscrito en este evento"
                ConflictoException(mensaje)
            }
            else -> e
        }
    }
}
