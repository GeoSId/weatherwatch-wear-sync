package com.weather.wearable.data.api

import com.weather.wearable.domain.WearableWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service for wearable weather data
 * Direct calls to OpenWeatherMap API
 */
interface WearableWeatherApiService {
    
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WearableWeatherResponse
}

