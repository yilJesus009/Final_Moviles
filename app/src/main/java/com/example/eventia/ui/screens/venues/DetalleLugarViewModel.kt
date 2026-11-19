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
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Estado de la pantalla de Detalle del Lugar (RF-13).
 */
data class DetalleLugarUiState(
    val cargando: Boolean = false,
    val lugar: Lugar? = null,
    val eventosProximos: List<Evento> = emptyList(),
    val noEncontrado: Boolean = false,
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
        // Regla agents.md: Asignar el estado Loading antes de lanzar la corrutina
        _uiState.update { it.copy(cargando = true, noEncontrado = false, error = null) }
        viewModelScope.launch {
            try {
                val lugarDeferred = async { repository.getLugarPorId(venueId) }
                val eventosDeferred = async {
                    try {
                        repository.getEventosDeLugar(venueId)
                    } catch (e: Exception) {
                        e.printStackTrace()
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
            } catch (e: HttpException) {
                e.printStackTrace()
                if (e.code() == 404) {
                    // Sección 8.3: 404 → recurso inexistente, mostrar mensaje claro y permitir volver
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            noEncontrado = true,
                            error = "Este lugar ya no está disponible"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "No se pudo cargar la información del lugar. Intenta nuevamente."
                        )
                    }
                }
            } catch (e: Exception) {
                // Regla agents.md: Registrar con e.printStackTrace() y mensaje claro para el usuario
                e.printStackTrace()
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
