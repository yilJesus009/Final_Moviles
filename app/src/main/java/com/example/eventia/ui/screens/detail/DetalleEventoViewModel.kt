package com.example.eventia.ui.screens.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.models.Evento
import com.example.eventia.repositories.AuthRepository
import com.example.eventia.repositories.ConflictoException
import com.example.eventia.repositories.EventoRepository
import com.example.eventia.repositories.FavoritoRepository
import com.example.eventia.repositories.InscripcionRepository
import com.example.eventia.repositories.SesionExpiradaException
import com.example.eventia.repositories.SinSesionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Estado de la pantalla de Detalle del Evento (RF-02, RF-04, RF-05, RF-11, RF-12).
 */
data class DetalleEventoUiState(
    val cargando: Boolean = false,
    val procesandoInscripcion: Boolean = false,
    val evento: Evento? = null,
    val esFavorito: Boolean = false,
    val haySesion: Boolean = false,
    val noEncontrado: Boolean = false,
    val navegarALogin: Boolean = false,
    val avisoSinConexion: String? = null,
    val error: String? = null,
    val mensajeAviso: String? = null
)

/**
 * ViewModel para el Detalle del Evento (RF-02, RF-04, RF-05, RF-12).
 */
@HiltViewModel
class DetalleEventoViewModel @Inject constructor(
    private val repository: EventoRepository,
    private val favoritoRepository: FavoritoRepository,
    private val authRepository: AuthRepository,
    private val inscripcionRepository: InscripcionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: Int = checkNotNull(savedStateHandle["eventId"])

    private val _uiState = MutableStateFlow(DetalleEventoUiState())
    val uiState: StateFlow<DetalleEventoUiState> = _uiState.asStateFlow()

    init {
        observarFavorito()
        observarEstadoConexion()
        observarSesion()
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

    // Cuando el usuario inicia sesión y regresa al detalle, se vuelve a pedir el evento con token
    private fun observarSesion() {
        viewModelScope.launch {
            authRepository.usuarioActual
                .distinctUntilChangedBy { it?.id }
                .collect { usuario ->
                    _uiState.update { it.copy(haySesion = usuario != null) }
                    cargarDetalle()
                }
        }
    }

    fun cargarDetalle() {
        viewModelScope.launch {
            val yaTeniaEvento = _uiState.value.evento != null
            _uiState.update {
                it.copy(
                    cargando = !yaTeniaEvento,
                    noEncontrado = false,
                    error = null
                )
            }
            try {
                val evento = repository.getEventoPorId(eventId)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        evento = evento,
                        error = null
                    )
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    // Sección 8.3: 404 → "Este evento ya no está disponible" y permitir volver
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            noEncontrado = true,
                            error = "Este evento ya no está disponible"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = "No se pudo cargar el detalle del evento. Intenta nuevamente."
                        )
                    }
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

    /**
     * RF-12: Inscribirse o cancelar inscripción en el evento.
     * - Sin sesión → lleva al inicio de sesión y al volver regresa a este evento.
     * - 409 → informa al usuario y refresca el detalle para mostrar los cupos reales.
     * - 401 → la sesión ya se borró en el repositorio; informa y lleva al login.
     */
    fun gestionarInscripcion() {
        val estado = _uiState.value
        val evento = estado.evento ?: return

        if (!estado.haySesion) {
            _uiState.update { it.copy(navegarALogin = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(procesandoInscripcion = true) }
            try {
                if (evento.estaInscrito) {
                    inscripcionRepository.cancelarInscripcion(eventId)
                    _uiState.update {
                        it.copy(
                            procesandoInscripcion = false,
                            mensajeAviso = "Cancelaste tu inscripción al evento"
                        )
                    }
                } else {
                    inscripcionRepository.inscribirse(eventId)
                    _uiState.update {
                        it.copy(
                            procesandoInscripcion = false,
                            mensajeAviso = "¡Te inscribiste al evento!"
                        )
                    }
                }
                cargarDetalle()
            } catch (e: ConflictoException) {
                _uiState.update {
                    it.copy(
                        procesandoInscripcion = false,
                        mensajeAviso = e.message ?: "No hay cupos disponibles para este evento"
                    )
                }
                cargarDetalle()
            } catch (e: SesionExpiradaException) {
                _uiState.update {
                    it.copy(
                        procesandoInscripcion = false,
                        mensajeAviso = e.message,
                        navegarALogin = true
                    )
                }
            } catch (e: SinSesionException) {
                _uiState.update {
                    it.copy(
                        procesandoInscripcion = false,
                        navegarALogin = true
                    )
                }
            } catch (e: Exception) {
                Log.e("DetalleEventoViewModel", "Error en inscripción: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        procesandoInscripcion = false,
                        mensajeAviso = "No se pudo procesar tu solicitud. Intenta nuevamente."
                    )
                }
            }
        }
    }

    fun consumirMensajeAviso() {
        _uiState.update { it.copy(mensajeAviso = null) }
    }

    fun consumirNavegarALogin() {
        _uiState.update { it.copy(navegarALogin = false) }
    }
}
