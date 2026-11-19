package com.example.eventia.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para [AuthValidator].
 * Sigue la misma estructura y convención de pruebas unitarias de PracticaAPILibros (BookValidatorTest).
 */
class AuthValidatorTest {

    // ---------- Validación de Correo Electrónico ----------

    @Test
    fun acceptsValidEmailAddresses() {
        assertTrue(AuthValidator.isValidEmail("ana@eventia.test"))
        assertTrue(AuthValidator.isValidEmail("usuario.prueba@gmail.com"))
        assertTrue(AuthValidator.isValidEmail("estudiante_2026@nur.edu.bo"))
        assertTrue(AuthValidator.isValidEmail("  ana@eventia.test  "))
    }

    @Test
    fun rejectsInvalidEmailAddresses() {
        assertFalse(AuthValidator.isValidEmail(""))
        assertFalse(AuthValidator.isValidEmail("   "))
        assertFalse(AuthValidator.isValidEmail("anaeventia.test"))
        assertFalse(AuthValidator.isValidEmail("ana@"))
        assertFalse(AuthValidator.isValidEmail("@eventia.test"))
        assertFalse(AuthValidator.isValidEmail("ana@eventia"))
        assertFalse(AuthValidator.isValidEmail("ana @eventia.test"))
    }

    @Test
    fun validateEmailReturnsNullForValidEmail() {
        assertNull(AuthValidator.validateEmail("ana@eventia.test"))
    }

    @Test
    fun validateEmailReturnsSpecificErrorMessage() {
        assertEquals("Ingresa tu correo electrónico", AuthValidator.validateEmail(""))
        assertEquals("Ingresa tu correo electrónico", AuthValidator.validateEmail("   "))
        assertEquals("Ingresa un correo electrónico válido", AuthValidator.validateEmail("correo-invalido"))
    }

    @Test
    fun normalizeEmailTrimsAndLowercases() {
        assertEquals("ana@eventia.test", AuthValidator.normalizeEmail("  ANA@EVENTIA.TEST  "))
        assertEquals("test@test.com", AuthValidator.normalizeEmail("Test@Test.COM "))
    }

    // ---------- Validación de Contraseña (RF-11: >= 8 caracteres) ----------

    @Test
    fun acceptsPasswordsWithEightOrMoreCharacters() {
        assertTrue(AuthValidator.isValidPassword("eventia123"))
        assertTrue(AuthValidator.isValidPassword("12345678"))
        assertTrue(AuthValidator.isValidPassword("passwordMuySeguro!2026"))
    }

    @Test
    fun rejectsPasswordsWithLessThanEightCharacters() {
        assertFalse(AuthValidator.isValidPassword(""))
        assertFalse(AuthValidator.isValidPassword("123"))
        assertFalse(AuthValidator.isValidPassword("1234567"))
    }

    @Test
    fun validatePasswordReturnsNullForValidPassword() {
        assertNull(AuthValidator.validatePassword("eventia123"))
    }

    @Test
    fun validatePasswordReturnsSpecificErrorMessage() {
        assertEquals("Ingresa tu contraseña", AuthValidator.validatePassword(""))
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres",
            AuthValidator.validatePassword("corta")
        )
    }

    // ---------- Validación de Nombre (Registro) ----------

    @Test
    fun acceptsValidNames() {
        assertTrue(AuthValidator.isValidName("Ana Gómez"))
        assertTrue(AuthValidator.isValidName("Juan"))
    }

    @Test
    fun rejectsEmptyOrBlankNames() {
        assertFalse(AuthValidator.isValidName(""))
        assertFalse(AuthValidator.isValidName("   "))
    }

    @Test
    fun validateNameReturnsSpecificErrorMessage() {
        assertEquals("Ingresa tu nombre completo", AuthValidator.validateName(""))
        assertEquals("El nombre debe tener al menos 2 caracteres", AuthValidator.validateName("A"))
        assertNull(AuthValidator.validateName("Ana"))
    }

    // ---------- Validación de Confirmación de Contraseña ----------

    @Test
    fun validatePasswordConfirmationMatches() {
        assertNull(AuthValidator.validatePasswordConfirmation("eventia123", "eventia123"))
    }

    @Test
    fun validatePasswordConfirmationFailsWhenDifferentOrEmpty() {
        assertEquals(
            "Confirma tu contraseña",
            AuthValidator.validatePasswordConfirmation("eventia123", "")
        )
        assertEquals(
            "Las contraseñas no coinciden",
            AuthValidator.validatePasswordConfirmation("eventia123", "otraCosa123")
        )
    }
}
