package com.example.eventia.repositories

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Proveedor de la instancia de Retrofit (mismo patrón que RetrofitRepository de PracticaWebAPI).
 * Diferencia: la instancia se crea una sola vez (by lazy) y se reutiliza en toda la app.
 */
object RetrofitRepository {
    // TODO: reemplazar por la URL base publicada en la plataforma del curso (debe terminar en "/")
    const val BASE_URL = "https://eventia-api.example.com/"

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
