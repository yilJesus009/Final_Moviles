package com.example.eventia.validation

/**
 * Validador puro en Kotlin para credenciales y formularios de autenticación (RF-11).
 * Implementado sin dependencias del framework de Android (como android.util.Patterns)
 * para permitir pruebas unitarias locales inmediatas con JUnit en la JVM.
 *
 * Sigue el mismo patrón arquitectónico de validadores desacoplados de PracticaAPILibros.
 */
object AuthValidator {

    // Expresión regular estándar RFC 5322 simplificada para validación de correo electrónico
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    const val MIN_PASSWORD_LENGTH = 8

    /**
     * Limpia y normaliza el correo electrónico eliminando espacios y convirtiéndolo a minúsculas.
     */
    fun normalizeEmail(email: String): String = email.trim().lowercase()

    /**
     * Valida el formato del correo electrónico.
     */
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return false
        return EMAIL_REGEX.matches(trimmed)
    }

    /**
     * Evalúa el correo electrónico y devuelve el mensaje de error correspondiente, o null si es válido.
     */
    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "Ingresa tu correo electrónico"
            !EMAIL_REGEX.matches(trimmed) -> "Ingresa un correo electrónico válido"
            else -> null
        }
    }

    /**
     * Valida la longitud y presencia de la contraseña (mínimo 8 caracteres según RF-11).
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    /**
     * Evalúa la contraseña y devuelve el mensaje de error correspondiente, o null si es válida.
     */
    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Ingresa tu contraseña"
            password.length < MIN_PASSWORD_LENGTH -> "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
            else -> null
        }
    }

    /**
     * Valida que el nombre de usuario no esté en blanco en el registro.
     */
    fun isValidName(name: String): Boolean {
        return name.trim().isNotEmpty()
    }

    /**
     * Evalúa el nombre y devuelve el mensaje de error correspondiente, o null si es válido.
     */
    fun validateName(name: String): String? {
        return when {
            name.trim().isEmpty() -> "Ingresa tu nombre completo"
            name.trim().length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        }
    }

    /**
     * Valida la coincidencia entre la contraseña y su confirmación.
     */
    fun validatePasswordConfirmation(password: String, confirmation: String): String? {
        return when {
            confirmation.isEmpty() -> "Confirma tu contraseña"
            password != confirmation -> "Las contraseñas no coinciden"
            else -> null
        }
    }
}
