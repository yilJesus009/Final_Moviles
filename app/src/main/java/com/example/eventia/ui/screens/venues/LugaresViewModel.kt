package com.example.eventia.ui.screens.venues

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.models.Lugar
import com.example.eventia.repositories.LugarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de Lista de Lugares (RF-13).
 */
data class LugaresUiState(
    val cargando: Boolean = false,
    val lugares: List<Lugar> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para la Lista de Lugares (Módulo B: RF-13).
 */
@HiltViewModel
class LugaresViewModel @Inject constructor(
    private val repository: LugarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LugaresUiState())
    val uiState: StateFlow<LugaresUiState> = _uiState.asStateFlow()

    init {
        cargarLugares()
    }

    fun cargarLugares() {
        // Regla agents.md: Asignar el estado Loading antes de lanzar la corrutina
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val lista = repository.getLugares()
                _uiState.update {
                    it.copy(
                        cargando = false,
                        lugares = lista,
                        error = null
                    )
                }
            } catch (e: Exception) {
                // Regla agents.md: Registrar con e.printStackTrace() y mensaje claro para el usuario
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudieron cargar los lugares. Verifica tu conexión e intenta nuevamente."
                    )
                }
            }
        }
    }
}
