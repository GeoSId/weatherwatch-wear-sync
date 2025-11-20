package com.weather.wearable.domain

import com.weather.wearable.constants.WearableConstants.Labels

/**
 * Wearable-optimized weather data model
 * Simplified version for watch displays
 */
data class WearableWeatherData(
    val temperature: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: Float,
    val cityName: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Create from JSON data received from phone
     */
    companion object {
        fun fromJson(data: Map<String, Any>): WearableWeatherData {
            return WearableWeatherData(
                temperature = (data["temperature"] as? Number)?.toInt() ?: 0,
                condition = data["condition"] as? String ?: Labels.STATUS_UNKNOWN,
                humidity = (data["humidity"] as? Number)?.toInt() ?: 0,
                windSpeed = (data["windSpeed"] as? Number)?.toFloat() ?: 0f,
                cityName = data["cityName"] as? String ?: Labels.STATUS_UNKNOWN,
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}