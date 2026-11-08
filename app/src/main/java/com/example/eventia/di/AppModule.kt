package com.example.eventia.di

import android.content.Context
import com.example.eventia.api.EventiaAPI
import com.example.eventia.db.AppDatabase
import com.example.eventia.db.dao.EventoCacheDao
import com.example.eventia.db.dao.FavoritoDao
import com.example.eventia.repositories.EventoRepository
import com.example.eventia.repositories.EventoRepositoryImpl
import com.example.eventia.repositories.FavoritoRepository
import com.example.eventia.repositories.FavoritoRepositoryImpl
import com.example.eventia.repositories.LugarRepository
import com.example.eventia.repositories.LugarRepositoryImpl
import com.example.eventia.repositories.RetrofitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.create
import javax.inject.Singleton

/**
 * Módulo de Hilt: provee las instancias singleton de Retrofit, Room Database, DAOs y Repositorios.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEventiaAPI(): EventiaAPI {
        return RetrofitRepository.getInstance().create<EventiaAPI>()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideEventoCacheDao(database: AppDatabase): EventoCacheDao {
        return database.eventoCacheDao()
    }

    @Provides
    @Singleton
    fun provideFavoritoDao(database: AppDatabase): FavoritoDao {
        return database.favoritoDao()
    }

    @Provides
    @Singleton
    fun provideEventoRepository(
        api: EventiaAPI,
        eventoCacheDao: EventoCacheDao,
        favoritoDao: FavoritoDao
    ): EventoRepository {
        return EventoRepositoryImpl(api, eventoCacheDao, favoritoDao)
    }

    @Provides
    @Singleton
    fun provideLugarRepository(api: EventiaAPI): LugarRepository {
        return LugarRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideFavoritoRepository(favoritoDao: FavoritoDao): FavoritoRepository {
        return FavoritoRepositoryImpl(favoritoDao)
    }
}
