package com.example.eventia.repositories

import com.example.eventia.api.models.ErrorApiDto
import com.google.gson.Gson
import retrofit2.HttpException

/**
 * Errores "de negocio" que los repositorios lanzan para que el ViewModel
 * sepa qué mostrar sin conocer detalles de Retrofit (sección 8.3 del enunciado).
 */

// 401: token ausente, inválido o expirado → la sesión local ya fue borrada
class SesionExpiradaException :
    Exception("Tu sesión expiró. Inicia sesión nuevamente.")

// Se intentó una acción que requiere cuenta sin haber iniciado sesión
class SinSesionException :
    Exception("Debes iniciar sesión para continuar.")

// 409: sin cupos o ya inscrito
class ConflictoException(mensaje: String) : Exception(mensaje)

// 400 / 422: datos inválidos, con los errores por campo que envía el servidor
class DatosInvalidosException(
    mensaje: String,
    val errores: Map<String, String>
) : Exception(mensaje)

/**
 * Lee el cuerpo de error del backend { "code", "message", "fields" }.
 * Si el cuerpo no tiene ese formato devuelve null en lugar de fallar.
 */
fun HttpException.leerErrorApi(): ErrorApiDto? {
    return try {
        val cuerpo = response()?.errorBody()?.string() ?: return null
        Gson().fromJson(cuerpo, ErrorApiDto::class.java)
    } catch (e: Exception) {
        null
    }
}
