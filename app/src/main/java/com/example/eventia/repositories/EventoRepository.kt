package com.example.eventia.repositories

import com.example.eventia.api.EventiaAPI
import com.example.eventia.api.models.aDominio
import com.example.eventia.datastore.SesionDataStore
import com.example.eventia.db.dao.EventoCacheDao
import com.example.eventia.db.dao.FavoritoDao
import com.example.eventia.db.entities.aCacheEntity
import com.example.eventia.db.entities.aDominio
import com.example.eventia.models.Categoria
import com.example.eventia.models.Evento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import java.time.ZonedDateTime
import kotlin.math.max

/**
 * Abstracción del repositorio de eventos. El ViewModel solo conoce esta interfaz,
 * nunca Retrofit ni Room directamente (requisito técnico de la sección 7 y RF-14).
 */
interface EventoRepository {
    val avisoSinConexion: StateFlow<String?>
    suspend fun getEventos(busqueda: String? = null, categoriaId: Int? = null): List<Evento>
    suspend fun getEventoPorId(id: Int): Evento
    suspend fun getCategorias(): List<Categoria>
}

/**
 * Implementación que decide de forma transparente entre la API REST y la caché local en Room (RF-14).
 * - Cuando hay conexión, actualiza la caché en Room y limpia el aviso sin conexión.
 * - Cuando no hay conexión, devuelve la caché filtrada y expone un aviso del tipo
 *   "Sin conexión · datos de hace 2 h" (o en minutos si es reciente).
 */
class EventoRepositoryImpl(
    private val api: EventiaAPI,
    private val eventoCacheDao: EventoCacheDao,
    private val favoritoDao: FavoritoDao,
    private val sesion: SesionDataStore
) : EventoRepository {

    private val _avisoSinConexion = MutableStateFlow<String?>(null)
    override val avisoSinConexion: StateFlow<String?> = _avisoSinConexion.asStateFlow()

    override suspend fun getEventos(busqueda: String?, categoriaId: Int?): List<Evento> {
        val ahora = ZonedDateTime.now()
        val textoLimpio = busqueda?.trim()?.takeIf { it.isNotEmpty() }

        return try {
            val remotos = api.getEventos(
                busqueda = textoLimpio,
                categoriaId = categoriaId
            )
                .map { it.aDominio() }
                // RF-01: los eventos pasados no se muestran en el catálogo
                .filter { evento -> evento.fechaInicio == null || evento.fechaInicio.isAfter(ahora) }
                .sortedBy { it.fechaInicio }

            // RF-14: Al descargar el catálogo completo, reemplazamos la caché en Room
            val marcaTiempo = System.currentTimeMillis()
            if (textoLimpio == null && categoriaId == null) {
                eventoCacheDao.limpiarEventos()
                eventoCacheDao.insertarTodos(remotos.map { it.aCacheEntity(marcaTiempo) })
            } else if (remotos.isNotEmpty()) {
                eventoCacheDao.insertarTodos(remotos.map { it.aCacheEntity(marcaTiempo) })
            }

            _avisoSinConexion.value = null
            remotos
        } catch (e: Exception) {
            // RF-14: Si falla la red, consultamos la caché de Room
            val enCache = eventoCacheDao.getTodos()
            if (enCache.isNotEmpty()) {
                val ultimaActualizacion = eventoCacheDao.getUltimaActualizacionMillis()
                _avisoSinConexion.value = formatearAvisoCache(ultimaActualizacion)

                enCache
                    .map { it.aDominio() }
                    .filter { evento -> evento.fechaInicio == null || evento.fechaInicio.isAfter(ahora) }
                    .filter { evento ->
                        textoLimpio == null ||
                            evento.titulo.contains(textoLimpio, ignoreCase = true) ||
                            evento.resumen.contains(textoLimpio, ignoreCase = true)
                    }
                    .filter { evento ->
                        categoriaId == null || evento.categoria?.id == categoriaId
                    }
                    .sortedBy { it.fechaInicio }
            } else {
                throw e
            }
        }
    }

    override suspend fun getEventoPorId(id: Int): Evento {
        return try {
            val remoto = getEventoRemoto(id)
            _avisoSinConexion.value = null
            remoto
        } catch (e: HttpException) {
            // 404: el evento ya no existe, no tiene sentido buscarlo en caché (la UI muestra el aviso)
            if (e.code() == 404) throw e
            buscarEventoLocal(id) ?: throw e
        } catch (e: Exception) {
            // RF-05 y RF-14: sin conexión, se busca el detalle en favoritos o en la caché
            buscarEventoLocal(id) ?: throw e
        }
    }

    private suspend fun buscarEventoLocal(id: Int): Evento? {
        val desdeFavorito = favoritoDao.getPorId(id)?.aDominio()
        val desdeCache = eventoCacheDao.getPorId(id)?.aDominio()
        val local = desdeFavorito ?: desdeCache ?: return null
        val ultimaActualizacion = eventoCacheDao.getUltimaActualizacionMillis()
        _avisoSinConexion.value = formatearAvisoCache(ultimaActualizacion)
        return local
    }

    /**
     * GET /events/{id} con sesión opcional: si hay token se envía para recibir "isAttending" (RF-12).
     * Si el token ya no es válido (401) se borra la sesión local y se reintenta sin token,
     * porque el detalle también debe poder verse sin cuenta.
     */
    private suspend fun getEventoRemoto(id: Int): Evento {
        val token = sesion.obtenerToken()
        if (token.isNullOrBlank()) {
            return api.getEventoPorId(id).aDominio()
        }
        return try {
            api.getEventoPorId(id, "Bearer $token").aDominio()
        } catch (e: HttpException) {
            if (e.code() != 401) throw e
            sesion.cerrarSesion()
            api.getEventoPorId(id).aDominio()
        }
    }

    override suspend fun getCategorias(): List<Categoria> {
        return try {
            val remotas = api.getCategorias().map { it.aDominio() }
            eventoCacheDao.limpiarCategorias()
            eventoCacheDao.insertarCategorias(remotas.map { it.aCacheEntity() })
            remotas
        } catch (e: Exception) {
            eventoCacheDao.getCategorias().map { it.aDominio() }
        }
    }

    private fun formatearAvisoCache(ultimaActualizacionMillis: Long?): String {
        if (ultimaActualizacionMillis == null || ultimaActualizacionMillis <= 0L) {
            return "Sin conexión · mostrando datos guardados"
        }
        val diferenciaMinutos = max(1L, (System.currentTimeMillis() - ultimaActualizacionMillis) / 60_000L)
        return when {
            diferenciaMinutos < 60L -> "Sin conexión · datos de hace $diferenciaMinutos min"
            diferenciaMinutos < 1440L -> {
                val horas = diferenciaMinutos / 60L
                "Sin conexión · datos de hace $horas h"
            }
            else -> {
                val dias = diferenciaMinutos / 1440L
                "Sin conexión · datos de hace $dias d"
            }
        }
    }
}
