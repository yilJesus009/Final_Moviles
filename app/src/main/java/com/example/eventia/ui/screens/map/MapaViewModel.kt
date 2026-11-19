package com.example.eventia.ui.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.models.Categoria
import com.example.eventia.models.Evento
import com.example.eventia.repositories.EventoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla del Mapa (Módulo E · RF-07 y RF-08).
 * Solo se guardan eventos con coordenadas válidas: los eventos sin ubicación
 * se descartan aquí para no generar errores ni marcadores en (0,0).
 */
data class MapaUiState(
    val cargando: Boolean = false,
    val eventosConUbicacion: List<Evento> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val categoriaSeleccionadaId: Int? = null,
    val eventoSeleccionado: Evento? = null,
    val avisoSinConexion: String? = null,
    val error: String? = null
)

@HiltViewModel
class MapaViewModel @Inject constructor(
    private val repository: EventoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    init {
        observarEstadoConexion()
        cargarCategorias()
        cargarEventos()
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
                e.printStackTrace()
            }
        }
    }

    fun cargarEventos() {
        val categoriaId = _uiState.value.categoriaSeleccionadaId
        // Regla agents.md: Asignar el estado Loading antes de lanzar la corrutina
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                // RF-07: el mapa respeta el filtro de categoría y omite eventos sin coordenadas válidas
                val conUbicacion = repository.getEventos(categoriaId = categoriaId)
                    .filter { it.lugar?.tieneCoordenadasValidas == true }
                _uiState.update {
                    it.copy(
                        cargando = false,
                        eventosConUbicacion = conUbicacion,
                        error = null
                    )
                }
            } catch (e: Exception) {
                // Regla agents.md: Registrar con e.printStackTrace() y mensaje claro para el usuario
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudieron cargar los eventos en el mapa. Verifica tu conexión e intenta nuevamente."
                    )
                }
            }
        }
    }

    // RF-07: tocar la misma categoría vuelve a mostrar "Todas"
    fun seleccionarCategoria(categoriaId: Int?) {
        val nuevoId = if (_uiState.value.categoriaSeleccionadaId == categoriaId) null else categoriaId
        _uiState.update {
            it.copy(
                categoriaSeleccionadaId = nuevoId,
                eventoSeleccionado = null
            )
        }
        cargarEventos()
    }

    // RF-08: al tocar un marcador se abre la hoja inferior con el resumen del evento
    fun seleccionarEvento(evento: Evento?) {
        _uiState.update { it.copy(eventoSeleccionado = evento) }
    }
}
