package com.example.eventia.repositories

import com.example.eventia.api.models.ErrorApiDto
import com.google.gson.JsonParser
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
 * Soporta tanto valores de tipo texto simple ("email": "...") como listas de Laravel ("email": ["..."]).
 * Si el cuerpo no tiene ese formato devuelve null en lugar de fallar.
 */
fun HttpException.leerErrorApi(): ErrorApiDto? {
    val cuerpo = try {
        response()?.errorBody()?.string()
    } catch (e: Exception) {
        null
    }
    return parsearErrorApi(cuerpo)
}

fun parsearErrorApi(cuerpo: String?): ErrorApiDto? {
    if (cuerpo.isNullOrBlank()) return null
    return try {
        val obj = JsonParser.parseString(cuerpo).asJsonObject
        val codigo = obj.get("code")?.takeIf { !it.isJsonNull }?.asString
        val mensaje = obj.get("message")?.takeIf { !it.isJsonNull }?.asString
        val camposMap = mutableMapOf<String, String>()
        val fieldsElement = obj.get("fields")
        if (fieldsElement != null && fieldsElement.isJsonObject) {
            for ((clave, valor) in fieldsElement.asJsonObject.entrySet()) {
                when {
                    valor.isJsonPrimitive -> camposMap[clave] = valor.asString
                    valor.isJsonArray && valor.asJsonArray.size() > 0 -> {
                        val primero = valor.asJsonArray[0]
                        if (primero != null && !primero.isJsonNull) {
                            camposMap[clave] = primero.asString
                        }
                    }
                }
            }
        }
        ErrorApiDto(
            codigo = codigo,
            mensaje = mensaje,
            campos = camposMap
        )
    } catch (e: Exception) {
        null
    }
}
