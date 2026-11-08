package com.example.eventia.ui.screens.catalog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.models.Categoria
import com.example.eventia.models.Evento
import com.example.eventia.repositories.EventoRepository
import com.example.eventia.repositories.FavoritoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado único e inmutable de la pantalla del Catálogo (sección 7).
 * Representa los estados de carga, actualización, contenido, vacío, error, favoritos y caché offline.
 */
data class CatalogoUiState(
    val cargando: Boolean = false,
    val actualizando: Boolean = false,
    val eventos: List<Evento> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val idsFavoritos: Set<Int> = emptySet(),
    val textoBusqueda: String = "",
    val categoriaSeleccionadaId: Int? = null,
    val avisoSinConexion: String? = null,
    val error: String? = null,
    val mensajeAviso: String? = null
) {
    val tieneFiltrosActivos: Boolean
        get() = textoBusqueda.isNotBlank() || categoriaSeleccionadaId != null
}

/**
 * ViewModel del Catálogo de Eventos (Módulo A y Módulo C: RF-01, RF-03, RF-04, RF-06, RF-14).
 */
@HiltViewModel
class CatalogoViewModel @Inject constructor(
    private val repository: EventoRepository,
    private val favoritoRepository: FavoritoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    private var trabajoBusqueda: Job? = null

    init {
        observarFavoritos()
        observarEstadoConexion()
        cargarCategorias()
        cargarEventos()
    }

    private fun observarFavoritos() {
        viewModelScope.launch {
            favoritoRepository.observarIdsFavoritos().collect { ids ->
                _uiState.update { it.copy(idsFavoritos = ids) }
            }
        }
    }

    private fun observarEstadoConexion() {
        viewModelScope.launch {
            repository.avisoSinConexion.collect { aviso ->
                _uiState.update { it.copy(avisoSinConexion = aviso) }
            }
        }
    }

    private fun cargarCategorias() {
        viewModelScope.launch {
            try {
                val listaCategorias = repository.getCategorias()
                _uiState.update { it.copy(categorias = listaCategorias) }
            } catch (e: Exception) {
                Log.e("CatalogoViewModel", "Error al cargar categorías: ${e.message}", e)
            }
        }
    }

    /**
     * Carga la lista de eventos aplicando el texto de búsqueda y la categoría actuales.
     * El repositorio decide si los datos provienen de la red o de la caché en Room (RF-14).
     */
    fun cargarEventos() {
        viewModelScope.launch {
            val estadoActual = _uiState.value
            val habiaDatosPrevios = estadoActual.eventos.isNotEmpty()

            _uiState.update {
                it.copy(
                    cargando = !habiaDatosPrevios,
                    error = null
                )
            }

            try {
                val eventos = repository.getEventos(
                    busqueda = estadoActual.textoBusqueda,
                    categoriaId = estadoActual.categoriaSeleccionadaId
                )
                _uiState.update {
                    it.copy(
                        cargando = false,
                        actualizando = false,
                        eventos = eventos,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("CatalogoViewModel", "Error al obtener eventos: ${e.message}", e)
                // RF-06: si ya había datos en pantalla, se conservan y se muestra un aviso no bloqueante
                if (_uiState.value.eventos.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            actualizando = false,
                            mensajeAviso = "No se pudo actualizar el catálogo. Mostrando los datos actuales."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            actualizando = false,
                            error = "No se pudieron cargar los eventos. Verifica tu conexión e intenta nuevamente."
                        )
                    }
                }
            }
        }
    }

    /**
     * RF-06: Actualización manual solicitada por el usuario (deslizar hacia abajo o botón).
     */
    fun actualizarCatalogo() {
        viewModelScope.launch {
            _uiState.update { it.copy(actualizando = true, error = null) }
            if (_uiState.value.categorias.isEmpty()) {
                cargarCategorias()
            }
            cargarEventos()
        }
    }

    /**
     * RF-03: Búsqueda por nombre con espera (debounce) de 300 ms antes de consultar.
     */
    fun cambiarTextoBusqueda(nuevoTexto: String) {
        _uiState.update { it.copy(textoBusqueda = nuevoTexto) }
        trabajoBusqueda?.cancel()
        trabajoBusqueda = viewModelScope.launch {
            delay(300L)
            cargarEventos()
        }
    }

    /**
     * RF-03: Filtro por categoría combinable con el texto de búsqueda.
     */
    fun seleccionarCategoria(categoriaId: Int?) {
        val nuevoId = if (_uiState.value.categoriaSeleccionadaId == categoriaId) null else categoriaId
        _uiState.update { it.copy(categoriaSeleccionadaId = nuevoId) }
        trabajoBusqueda?.cancel()
        cargarEventos()
    }

    /**
     * RF-03: "Limpiar filtros" restablece tanto la búsqueda como la categoría seleccionada.
     */
    fun limpiarFiltros() {
        trabajoBusqueda?.cancel()
        _uiState.update {
            it.copy(
                textoBusqueda = "",
                categoriaSeleccionadaId = null
            )
        }
        cargarEventos()
    }

    /**
     * RF-04 y RF-05: Agrega o quita un evento de favoritos en Room sin requerir conexión.
     */
    fun alternarFavorito(evento: Evento) {
        viewModelScope.launch {
            val agregado = favoritoRepository.alternarFavorito(evento)
            val aviso = if (agregado) {
                "Agregado a favoritos: ${evento.titulo}"
            } else {
                "Quitado de favoritos: ${evento.titulo}"
            }
            _uiState.update { it.copy(mensajeAviso = aviso) }
        }
    }

    fun consumirMensajeAviso() {
        _uiState.update { it.copy(mensajeAviso = null) }
    }
}
