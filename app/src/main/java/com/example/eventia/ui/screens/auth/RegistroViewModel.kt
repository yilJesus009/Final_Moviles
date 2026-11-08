package com.example.eventia.ui.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.repositories.AuthRepository
import com.example.eventia.repositories.DatosInvalidosException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado del formulario de registro (RF-11).
 */
data class RegistroUiState(
    val nombre: String = "",
    val email: String = "",
    val contrasena: String = "",
    val errorNombre: String? = null,
    val errorEmail: String? = null,
    val errorContrasena: String? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val registroExitoso: Boolean = false
)

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun cambiarNombre(nuevoNombre: String) {
        _uiState.update { it.copy(nombre = nuevoNombre, errorNombre = null, error = null) }
    }

    fun cambiarEmail(nuevoEmail: String) {
        _uiState.update { it.copy(email = nuevoEmail, errorEmail = null, error = null) }
    }

    fun cambiarContrasena(nuevaContrasena: String) {
        _uiState.update { it.copy(contrasena = nuevaContrasena, errorContrasena = null, error = null) }
    }

    fun registrarse() {
        val estado = _uiState.value
        val errorNombre = if (estado.nombre.isBlank()) "Ingresa tu nombre" else null
        val errorEmail = validarEmail(estado.email)
        val errorContrasena = validarContrasena(estado.contrasena)
        if (errorNombre != null || errorEmail != null || errorContrasena != null) {
            _uiState.update {
                it.copy(
                    errorNombre = errorNombre,
                    errorEmail = errorEmail,
                    errorContrasena = errorContrasena
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                repository.registrar(estado.nombre, estado.email, estado.contrasena)
                _uiState.update { it.copy(cargando = false, registroExitoso = true) }
            } catch (e: DatosInvalidosException) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = e.message,
                        errorNombre = e.errores["name"],
                        errorEmail = e.errores["email"],
                        errorContrasena = e.errores["password"]
                    )
                }
            } catch (e: Exception) {
                Log.e("RegistroViewModel", "Error al registrarse: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo conectar con el servidor. Verifica tu conexión e intenta nuevamente."
                    )
                }
            }
        }
    }
}
