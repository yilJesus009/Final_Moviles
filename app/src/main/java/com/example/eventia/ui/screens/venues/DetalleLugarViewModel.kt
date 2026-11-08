package com.example.eventia.ui.screens.venues

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.models.Evento
import com.example.eventia.models.Lugar
import com.example.eventia.repositories.LugarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de Detalle del Lugar (RF-13).
 */
data class DetalleLugarUiState(
    val cargando: Boolean = false,
    val lugar: Lugar? = null,
    val eventosProximos: List<Evento> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para el Detalle del Lugar (Módulo B: RF-13).
 * Consulta los datos del lugar (/venues/{id}) y sus próximos eventos (/venues/{id}/events).
 */
@HiltViewModel
class DetalleLugarViewModel @Inject constructor(
    private val repository: LugarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val venueId: Int = checkNotNull(savedStateHandle["venueId"])

    private val _uiState = MutableStateFlow(DetalleLugarUiState())
    val uiState: StateFlow<DetalleLugarUiState> = _uiState.asStateFlow()

    init {
        cargarDetalleLugar()
    }

    fun cargarDetalleLugar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val lugarDeferred = async { repository.getLugarPorId(venueId) }
                val eventosDeferred = async {
                    try {
                        repository.getEventosDeLugar(venueId)
                    } catch (e: Exception) {
                        Log.w("DetalleLugarViewModel", "No se pudieron cargar eventos del lugar $venueId: ${e.message}")
                        emptyList()
                    }
                }

                val lugar = lugarDeferred.await()
                val eventos = eventosDeferred.await()

                _uiState.update {
                    it.copy(
                        cargando = false,
                        lugar = lugar,
                        eventosProximos = eventos,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("DetalleLugarViewModel", "Error al cargar detalle del lugar $venueId: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo cargar la información del lugar. Intenta nuevamente."
                    )
                }
            }
        }
    }
}
