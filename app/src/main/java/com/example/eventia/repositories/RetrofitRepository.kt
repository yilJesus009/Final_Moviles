package com.example.eventia.repositories

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Proveedor de la instancia de Retrofit (mismo patrón que RetrofitRepository de PracticaWebAPI).
 * Diferencia: la instancia se crea una sola vez (by lazy) y se reutiliza en toda la app.
 */
object RetrofitRepository {
    /**
     * URL base de la API:
     * - En Emulador Android Studio: "http://10.0.2.2:8000/api/" (apunta a localhost:8000 de tu PC).
     * - En Dispositivo Físico: reemplazar 10.0.2.2 por la IP local de tu PC (ej. "http://192.168.1.15:8000/api/").
     * - En Servidor Remoto: la URL que proporcione el docente terminada en "/api/".
     */
    const val BASE_URL = "http://10.0.2.2:8000/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getInstance(): Retrofit {
        return retrofit
    }
}
