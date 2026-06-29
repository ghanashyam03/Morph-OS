package com.morphos.app.core.data.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWeatherApiService(): WeatherApiService {
        TODO("Not yet implemented")
    }

    @Provides
    @Singleton
    fun provideOpenRouterApiService(): OpenRouterApiService {
        TODO("Not yet implemented")
    }
}
