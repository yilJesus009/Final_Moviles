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
     * - Celular Físico conectado por USB: "http://localhost:8000/api/" (usando adb reverse tcp:8000 tcp:8000)
     * - Emulador Android Studio: "http://10.0.2.2:8000/api/"
     * - Servidor en la nube: la URL pública terminada en "/api/"
     */
    const val BASE_URL = "http://localhost:8000/api/"

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
