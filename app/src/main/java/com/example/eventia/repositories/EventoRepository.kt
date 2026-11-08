package com.example.eventia.repositories

import com.example.eventia.api.EventiaAPI
import com.example.eventia.api.models.aDominio
import com.example.eventia.models.Categoria
import com.example.eventia.models.Evento
import java.time.ZonedDateTime

/**
 * Abstracción del repositorio de eventos. El ViewModel solo conoce esta interfaz,
 * nunca Retrofit ni Room directamente (requisito técnico de la sección 7).
 */
interface EventoRepository {
    suspend fun getEventos(busqueda: String? = null, categoriaId: Int? = null): List<Evento>
    suspend fun getEventoPorId(id: Int): Evento
    suspend fun getCategorias(): List<Categoria>
}

/**
 * Implementación que consume la API REST (mismo estilo que PostRepository de PracticaWebAPI).
 * La API ya no se crea aquí: Hilt la entrega por el constructor (ver di/AppModule.kt).
 * Más adelante se agregará la caché en Room para el modo sin conexión (RF-14).
 */
class EventoRepositoryImpl(
    private val api: EventiaAPI
) : EventoRepository {

    override suspend fun getEventos(busqueda: String?, categoriaId: Int?): List<Evento> {
        val ahora = ZonedDateTime.now()
        return api.getEventos(
            busqueda = busqueda?.trim()?.takeIf { it.isNotEmpty() },
            categoriaId = categoriaId
        )
            .map { it.aDominio() }
            // RF-01: los eventos pasados no se muestran en el catálogo
            .filter { evento -> evento.fechaInicio == null || evento.fechaInicio.isAfter(ahora) }
            .sortedBy { it.fechaInicio }
    }

    override suspend fun getEventoPorId(id: Int): Evento {
        return api.getEventoPorId(id).aDominio()
    }

    override suspend fun getCategorias(): List<Categoria> {
        return api.getCategorias().map { it.aDominio() }
    }
}
