package com.morphos.app.core.data.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("forecast_days") days: Int = 3
    ): WeatherResponse
}

@Serializable
data class WeatherResponse(
    val current_weather: CurrentWeather,
    val daily: Daily
)

@Serializable
data class CurrentWeather(
    val temperature: Double,
    val weathercode: Int,
    val windspeed: Double
)

@Serializable
data class Daily(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)
