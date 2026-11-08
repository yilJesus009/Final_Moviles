package com.example.eventia.di

import com.example.eventia.api.EventiaAPI
import com.example.eventia.repositories.EventoRepository
import com.example.eventia.repositories.EventoRepositoryImpl
import com.example.eventia.repositories.RetrofitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.create
import javax.inject.Singleton

/**
 * Módulo de Hilt: le dice a Hilt CÓMO crear las dependencias que no puede construir solo.
 * SingletonComponent + @Singleton = una sola instancia para toda la app.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Reutiliza el mismo RetrofitRepository del estilo del docente
    @Provides
    @Singleton
    fun provideEventiaAPI(): EventiaAPI {
        return RetrofitRepository.getInstance().create<EventiaAPI>()
    }

    // Cuando un ViewModel pida un EventoRepository (interfaz), Hilt le entrega EventoRepositoryImpl
    @Provides
    @Singleton
    fun provideEventoRepository(api: EventiaAPI): EventoRepository {
        return EventoRepositoryImpl(api)
    }
}
