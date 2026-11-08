package com.example.eventia.repositories

import com.example.eventia.api.EventiaAPI
import com.example.eventia.api.models.LoginRequestDto
import com.example.eventia.api.models.RegistroRequestDto
import com.example.eventia.api.models.aDominio
import com.example.eventia.datastore.SesionDataStore
import com.example.eventia.models.Usuario
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

/**
 * Repositorio de la cuenta de usuario (Módulo D · RF-11).
 * El ViewModel nunca toca Retrofit ni DataStore directamente.
 */
interface AuthRepository {
    // Usuario con sesión iniciada (null = sin sesión). La UI lo observa para reaccionar al login/logout.
    val usuarioActual: Flow<Usuario?>
    suspend fun login(email: String, contrasena: String): Usuario
    suspend fun registrar(nombre: String, email: String, contrasena: String): Usuario
    suspend fun getPerfil(): Usuario
    suspend fun cerrarSesion()
}

class AuthRepositoryImpl(
    private val api: EventiaAPI,
    private val sesion: SesionDataStore
) : AuthRepository {

    override val usuarioActual: Flow<Usuario?> = sesion.usuario

    override suspend fun login(email: String, contrasena: String): Usuario {
        try {
            val respuesta = api.login(LoginRequestDto(email.trim(), contrasena))
            return guardarRespuesta(respuesta.token, respuesta.usuario?.aDominio())
        } catch (e: HttpException) {
            throw convertirErrorFormulario(e)
        }
    }

    override suspend fun registrar(nombre: String, email: String, contrasena: String): Usuario {
        try {
            val respuesta = api.registrar(RegistroRequestDto(nombre.trim(), email.trim(), contrasena))
            return guardarRespuesta(respuesta.token, respuesta.usuario?.aDominio())
        } catch (e: HttpException) {
            throw convertirErrorFormulario(e)
        }
    }

    // GET /me: valida que el token siga vigente y actualiza los datos guardados
    override suspend fun getPerfil(): Usuario {
        val token = sesion.obtenerToken() ?: throw SinSesionException()
        try {
            val usuario = api.getPerfil("Bearer $token").aDominio()
            sesion.actualizarUsuario(usuario)
            return usuario
        } catch (e: HttpException) {
            if (e.code() == 401) {
                sesion.cerrarSesion()
                throw SesionExpiradaException()
            }
            throw e
        }
    }

    override suspend fun cerrarSesion() {
        sesion.cerrarSesion()
    }

    private suspend fun guardarRespuesta(token: String?, usuario: Usuario?): Usuario {
        if (token.isNullOrBlank() || usuario == null) {
            throw Exception("Respuesta inválida del servidor")
        }
        sesion.guardarSesion(token, usuario)
        return usuario
    }

    // 400 / 422 / 401 en formularios → mensaje del servidor + errores por campo
    private fun convertirErrorFormulario(e: HttpException): Exception {
        val errorApi = e.leerErrorApi()
        val mensaje = errorApi?.mensaje ?: when (e.code()) {
            401 -> "Correo o contraseña incorrectos"
            409 -> "Ya existe una cuenta con ese correo"
            else -> "No se pudo completar la solicitud (código ${e.code()})"
        }
        return DatosInvalidosException(mensaje, errorApi?.campos.orEmpty())
    }
}
