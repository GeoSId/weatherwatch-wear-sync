package com.weather.domain.model

data class WeatherPrediction(
    val dt: Long,                 // Unix timestamp for the date of prediction
    val temp: Float,              // Predicted temperature
    val condition: String,        // Predicted weather condition (e.g., "Clear", "Rain")
    val humidity: Int,            // Predicted humidity percentage
    val windSpeed: Float,         // Predicted wind speed
    val confidence: Float = 0.7f  // AI model's confidence in the prediction (0.0-1.0)
) {
    val date: String
        get() = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
            .format(java.util.Date(dt * 1000))
} 