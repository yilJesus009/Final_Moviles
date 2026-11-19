package com.example.eventia.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.repositories.AuthRepository
import com.example.eventia.repositories.DatosInvalidosException
import com.example.eventia.validation.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado del formulario de inicio de sesión (RF-11).
 * Los textos viven aquí (y no en la pantalla) para que se conserven al rotar el dispositivo.
 */
data class LoginUiState(
    val email: String = "",
    val contrasena: String = "",
    val errorEmail: String? = null,
    val errorContrasena: String? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val sesionIniciada: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun cambiarEmail(nuevoEmail: String) {
        _uiState.update { it.copy(email = nuevoEmail, errorEmail = null, error = null) }
    }

    fun cambiarContrasena(nuevaContrasena: String) {
        _uiState.update { it.copy(contrasena = nuevaContrasena, errorContrasena = null, error = null) }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        // Validación local desacoplada ANTES de llamar al servidor
        val errorEmail = AuthValidator.validateEmail(estado.email)
        val errorContrasena = AuthValidator.validatePassword(estado.contrasena)
        if (errorEmail != null || errorContrasena != null) {
            _uiState.update { it.copy(errorEmail = errorEmail, errorContrasena = errorContrasena) }
            return
        }

        // Regla agents.md: Asignar el estado Loading antes de lanzar la corrutina
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                repository.login(AuthValidator.normalizeEmail(estado.email), estado.contrasena)
                _uiState.update { it.copy(cargando = false, sesionIniciada = true) }
            } catch (e: DatosInvalidosException) {
                // 400 / 422 / 401: se muestra el mensaje del servidor junto al campo correspondiente
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = e.message,
                        errorEmail = e.errores["email"],
                        errorContrasena = e.errores["password"]
                    )
                }
            } catch (e: Exception) {
                // Regla agents.md: Registrar con e.printStackTrace() y mensaje claro para el usuario
                e.printStackTrace()
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

