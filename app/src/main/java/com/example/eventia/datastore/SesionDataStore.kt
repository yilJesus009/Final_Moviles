package com.example.eventia.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.eventia.models.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Crea un único archivo de preferencias "sesion" para toda la app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

/**
 * Guarda la sesión del usuario (token + datos básicos) con DataStore (RF-11).
 * Al ser persistente, si el usuario cierra y vuelve a abrir la app sigue con su sesión iniciada.
 */
class SesionDataStore(private val context: Context) {

    companion object {
        private val CLAVE_TOKEN = stringPreferencesKey("token")
        private val CLAVE_ID = intPreferencesKey("usuario_id")
        private val CLAVE_NOMBRE = stringPreferencesKey("usuario_nombre")
        private val CLAVE_EMAIL = stringPreferencesKey("usuario_email")
    }

    // Flujo observable: emite el token cada vez que cambia (null = sin sesión)
    val token: Flow<String?> = context.dataStore.data.map { preferencias ->
        preferencias[CLAVE_TOKEN]
    }

    // Flujo observable con el usuario guardado (null = sin sesión)
    val usuario: Flow<Usuario?> = context.dataStore.data.map { preferencias ->
        val token = preferencias[CLAVE_TOKEN]
        if (token.isNullOrBlank()) {
            null
        } else {
            Usuario(
                id = preferencias[CLAVE_ID] ?: 0,
                nombre = preferencias[CLAVE_NOMBRE].orEmpty(),
                email = preferencias[CLAVE_EMAIL].orEmpty()
            )
        }
    }

    // Lectura puntual del token (para armar el encabezado Authorization)
    suspend fun obtenerToken(): String? {
        return token.first()
    }

    suspend fun guardarSesion(token: String, usuario: Usuario) {
        context.dataStore.edit { preferencias ->
            preferencias[CLAVE_TOKEN] = token
            preferencias[CLAVE_ID] = usuario.id
            preferencias[CLAVE_NOMBRE] = usuario.nombre
            preferencias[CLAVE_EMAIL] = usuario.email
        }
    }

    suspend fun actualizarUsuario(usuario: Usuario) {
        context.dataStore.edit { preferencias ->
            preferencias[CLAVE_ID] = usuario.id
            preferencias[CLAVE_NOMBRE] = usuario.nombre
            preferencias[CLAVE_EMAIL] = usuario.email
        }
    }

    // Cierre de sesión local: se usa en "Cerrar sesión" y cuando el servidor responde 401
    suspend fun cerrarSesion() {
        context.dataStore.edit { preferencias ->
            preferencias.clear()
        }
    }
}
