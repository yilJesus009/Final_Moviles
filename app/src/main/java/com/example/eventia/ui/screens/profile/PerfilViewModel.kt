package com.example.eventia.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.models.Evento
import com.example.eventia.models.Usuario
import com.example.eventia.repositories.AuthRepository
import com.example.eventia.repositories.InscripcionRepository
import com.example.eventia.repositories.SesionExpiradaException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Estado de la pantalla de Perfil (RF-11 y RF-12).
 * "Mis inscripciones" se separa en próximas y pasadas.
 */
data class PerfilUiState(
    val usuario: Usuario? = null,
    val cargando: Boolean = false,
    val proximas: List<Evento> = emptyList(),
    val pasadas: List<Evento> = emptyList(),
    val error: String? = null,
    val sesionExpirada: Boolean = false
) {
    val haySesion: Boolean
        get() = usuario != null
}

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val inscripcionRepository: InscripcionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        observarSesion()
    }

    // Cada vez que el usuario inicia o cierra sesión, la pantalla se actualiza sola
    private fun observarSesion() {
        viewModelScope.launch {
            authRepository.usuarioActual
                .distinctUntilChangedBy { it?.id }
                .collect { usuario ->
                    _uiState.update { it.copy(usuario = usuario) }
                    if (usuario != null) {
                        cargarDatos()
                    } else {
                        _uiState.update { it.copy(proximas = emptyList(), pasadas = emptyList(), error = null) }
                    }
                }
        }
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                // GET /me valida que el token siga vigente (si responde 401 se cierra la sesión)
                val usuario = authRepository.getPerfil()
                val inscripciones = inscripcionRepository.getMisInscripciones()
                val ahora = ZonedDateTime.now()
                val (proximas, pasadas) = inscripciones.partition { evento ->
                    evento.fechaInicio == null || evento.fechaInicio.isAfter(ahora)
                }
                _uiState.update {
                    it.copy(
                        cargando = false,
                        usuario = usuario,
                        proximas = proximas,
                        pasadas = pasadas.reversed() // las pasadas más recientes primero
                    )
                }
            } catch (e: SesionExpiradaException) {
                // RF-11: el repositorio ya borró la sesión local; la pantalla navega al login
                _uiState.update { it.copy(cargando = false, sesionExpirada = true) }
            } catch (e: Exception) {
                Log.e("PerfilViewModel", "Error al cargar el perfil: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudieron cargar tus inscripciones. Verifica tu conexión e intenta nuevamente."
                    )
                }
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.cerrarSesion()
        }
    }

    fun consumirSesionExpirada() {
        _uiState.update { it.copy(sesionExpirada = false) }
    }
}
