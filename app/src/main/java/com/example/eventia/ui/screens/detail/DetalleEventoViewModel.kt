package com.example.eventia.ui.screens.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.models.Evento
import com.example.eventia.repositories.EventoRepository
import com.example.eventia.repositories.FavoritoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de Detalle del Evento (RF-02, RF-04, RF-05).
 */
data class DetalleEventoUiState(
    val cargando: Boolean = false,
    val evento: Evento? = null,
    val esFavorito: Boolean = false,
    val avisoSinConexion: String? = null,
    val error: String? = null,
    val mensajeAviso: String? = null
)

/**
 * ViewModel para el Detalle del Evento (RF-02, RF-04, RF-05).
 */
@HiltViewModel
class DetalleEventoViewModel @Inject constructor(
    private val repository: EventoRepository,
    private val favoritoRepository: FavoritoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: Int = checkNotNull(savedStateHandle["eventId"])

    private val _uiState = MutableStateFlow(DetalleEventoUiState())
    val uiState: StateFlow<DetalleEventoUiState> = _uiState.asStateFlow()

    init {
        observarFavorito()
        observarEstadoConexion()
        cargarDetalle()
    }

    private fun observarFavorito() {
        viewModelScope.launch {
            favoritoRepository.observarIdsFavoritos().collect { ids ->
                _uiState.update { it.copy(esFavorito = eventId in ids) }
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

    fun cargarDetalle() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val evento = repository.getEventoPorId(eventId)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        evento = evento,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("DetalleEventoViewModel", "Error al cargar detalle del evento $eventId: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo cargar el detalle del evento. Intenta nuevamente."
                    )
                }
            }
        }
    }

    /**
     * RF-04 y RF-05: Agrega o quita el evento actual de favoritos en Room sin requerir conexión.
     */
    fun alternarFavorito() {
        val eventoActual = _uiState.value.evento ?: return
        viewModelScope.launch {
            val agregado = favoritoRepository.alternarFavorito(eventoActual)
            val aviso = if (agregado) {
                "Evento guardado en favoritos"
            } else {
                "Evento quitado de favoritos"
            }
            _uiState.update { it.copy(mensajeAviso = aviso) }
        }
    }

    fun consumirMensajeAviso() {
        _uiState.update { it.copy(mensajeAviso = null) }
    }
}
