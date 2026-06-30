package com.morphos.app.core.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.morphos.app.core.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WeatherApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenRouterApi

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            var attempt = 0
            var lastException: java.io.IOException? = null
            while (attempt < maxRetries) {
                try {
                    val response = chain.proceed(chain.request())
                    if (response.isSuccessful || response.code !in listOf(429, 503)) {
                        return response
                    }
                    response.close()
                } catch (e: java.io.IOException) {
                    lastException = e
                }
                attempt++
                if (attempt < maxRetries) {
                    val backoff = minOf(1000L * (1 shl attempt), 8000L)
                    try {
                        Thread.sleep(backoff)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw java.io.IOException("Retry backoff interrupted", ie)
                    }
                }
            }
            throw lastException ?: java.io.IOException("Max retries exceeded")
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val userAgentInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", "MorphOS/1.0 Android")
                .build()
            chain.proceed(requestWithUserAgent)
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(RetryInterceptor())
            .build()
    }

    @Provides
    @Singleton
    @WeatherApi
    fun provideWeatherRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @OpenRouterApi
    fun provideOpenRouterRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl("https://openrouter.ai/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(@WeatherApi retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenRouterApiService(@OpenRouterApi retrofit: Retrofit): OpenRouterApiService {
        return retrofit.create(OpenRouterApiService::class.java)
    }
}
