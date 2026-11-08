package com.example.eventia.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.models.Evento
import com.example.eventia.repositories.FavoritoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de Favoritos (Módulo C: RF-04 y RF-05).
 */
data class FavoritosUiState(
    val cargando: Boolean = true,
    val favoritos: List<Evento> = emptyList(),
    val eventoEliminadoReciente: Evento? = null
)

/**
 * ViewModel de Favoritos (RF-04 y RF-05).
 * Observa los favoritos persistidos en Room y permite deshacer la eliminación de un favorito.
 */
@HiltViewModel
class FavoritosViewModel @Inject constructor(
    private val favoritoRepository: FavoritoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritosUiState())
    val uiState: StateFlow<FavoritosUiState> = _uiState.asStateFlow()

    init {
        observarFavoritos()
    }

    private fun observarFavoritos() {
        viewModelScope.launch {
            favoritoRepository.observarFavoritos().collect { lista ->
                _uiState.update {
                    it.copy(
                        cargando = false,
                        favoritos = lista
                    )
                }
            }
        }
    }

    /**
     * RF-04: Quita el favorito de Room y lo guarda temporalmente para ofrecer la opción "Deshacer".
     */
    fun quitarFavorito(evento: Evento) {
        viewModelScope.launch {
            favoritoRepository.quitarFavorito(evento.id)
            _uiState.update { it.copy(eventoEliminadoReciente = evento) }
        }
    }

    /**
     * RF-04: Restaura en Room el último favorito eliminado cuando el usuario pulsa "Deshacer".
     */
    fun deshacerEliminacion() {
        val eliminado = _uiState.value.eventoEliminadoReciente ?: return
        viewModelScope.launch {
            favoritoRepository.agregarFavorito(eliminado)
            _uiState.update { it.copy(eventoEliminadoReciente = null) }
        }
    }

    fun limpiarEliminadoReciente() {
        _uiState.update { it.copy(eventoEliminadoReciente = null) }
    }
}
