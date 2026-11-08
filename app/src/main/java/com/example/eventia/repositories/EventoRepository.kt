package com.example.eventia.repositories

import com.example.eventia.api.EventiaAPI
import com.example.eventia.api.models.aDominio
import com.example.eventia.db.dao.EventoCacheDao
import com.example.eventia.db.dao.FavoritoDao
import com.example.eventia.db.entities.aCacheEntity
import com.example.eventia.db.entities.aDominio
import com.example.eventia.models.Categoria
import com.example.eventia.models.Evento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val favoritoDao: FavoritoDao
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
            val remoto = api.getEventoPorId(id).aDominio()
            _avisoSinConexion.value = null
            remoto
        } catch (e: Exception) {
            // RF-05 y RF-14: Acceso sin conexión al detalle de favoritos o de eventos en caché
            val desdeFavorito = favoritoDao.getPorId(id)?.aDominio()
            val desdeCache = eventoCacheDao.getPorId(id)?.aDominio()
            val local = desdeFavorito ?: desdeCache
            if (local != null) {
                val ultimaActualizacion = eventoCacheDao.getUltimaActualizacionMillis()
                _avisoSinConexion.value = formatearAvisoCache(ultimaActualizacion)
                local
            } else {
                throw e
            }
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
