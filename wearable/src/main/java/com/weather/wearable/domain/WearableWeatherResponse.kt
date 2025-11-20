package com.weather.wearable.domain

import com.google.gson.annotations.SerializedName

/**
 * API response model from OpenWeatherMap
 * Simplified for wearable use
 */
data class WearableWeatherResponse(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("main")
    val main: Main,
    
    @SerializedName("weather")
    val weather: List<Weather>,
    
    @SerializedName("wind")
    val wind: Wind
) {
    data class Main(
        @SerializedName("temp")
        val temp: Double,
        
        @SerializedName("humidity")
        val humidity: Int
    )
    
    data class Weather(
        @SerializedName("main")
        val main: String,
        
        @SerializedName("description")
        val description: String
    )
    
    data class Wind(
        @SerializedName("speed")
        val speed: Double
    )
    
    /**
     * Convert API response to WearableWeatherData
     */
    fun toWearableWeatherData(): WearableWeatherData {
        return WearableWeatherData(
            temperature = main.temp.toInt(),
            condition = weather.firstOrNull()?.main ?: "Unknown",  // Use .main for consistency with mobile app
            humidity = main.humidity,
            windSpeed = wind.speed.toFloat(),
            cityName = name,
            timestamp = System.currentTimeMillis()
        )
    }
}

