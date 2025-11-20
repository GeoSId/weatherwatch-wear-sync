package com.weather.wearable.constants

/**
 * Constants for Wearable Communication and Configuration
 * Following project guidelines to avoid hardcoded values
 */
object WearableConstants {
    
    // Data Layer Communication
    object DataLayer {
        const val WEATHER_DATA_PATH = "/weather_data"
        const val SYNC_REQUEST_PATH = "/sync_request"// Data Keys
        const val WEATHER_DATA_KEY = "weather_data"
        const val TIMESTAMP_KEY = "timestamp"
        const val LOCATION_KEY = "location"
        const val TEMPERATURE_KEY = "temperature"
        const val HUMIDITY_KEY = "humidity"
        const val WIND_SPEED_KEY = "wind_speed"
        const val CONDITION_KEY = "condition"
    }

    // Synchronization
    object Sync {
        // Sync types
        const val FULL_SYNC = "full_sync"
    }
    
    // Labels and Titles (Following project requirement to use constants)
    object Labels {

        // Status labels
        const val STATUS_UNKNOWN = "Unknown"
        
        // Unit labels
        const val TEMPERATURE_UNIT = "°C"
    }
} 