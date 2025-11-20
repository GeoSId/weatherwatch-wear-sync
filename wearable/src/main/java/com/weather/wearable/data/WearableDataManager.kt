package com.weather.wearable.data

import android.content.Context
import com.weather.wearable.domain.WearableWeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simplified data bridge for Wear OS Data Layer
 * 
 * Purpose: Receives data from DataLayerListenerService and emits it via StateFlow
 * ViewModel observes this StateFlow and consolidates it with API data
 * 
 * This is a lightweight singleton - all business logic is in ViewModel
 */
class WearableDataManager private constructor(
    @Suppress("UNUSED_PARAMETER") context: Context
) {
    
    companion object {
        
        @Volatile
        private var INSTANCE: WearableDataManager? = null
        
        fun getInstance(): WearableDataManager {
            return INSTANCE ?: synchronized(this) {
                throw IllegalStateException("WearableDataManager not initialized. Call initialize() first.")
            }
        }
        
        fun initialize(context: Context): WearableDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WearableDataManager(context.applicationContext).also { 
                    INSTANCE = it
                }
            }
        }
    }
    
    // Single StateFlow for weather data received from phone
    private val _weatherData = MutableStateFlow<WearableWeatherData?>(null)
    val weatherData: StateFlow<WearableWeatherData?> = _weatherData.asStateFlow()
    
    /**
     * Called by DataLayerListenerService when data arrives from phone
     * Simply emits the data - ViewModel handles all logic
     */
    fun updateWeatherData(data: WearableWeatherData) {
        _weatherData.value = data
    }
    
    /**
     * Clear weather data (optional - for cleanup)
     */
    fun clearWeatherData() {
        _weatherData.value = null
    }
}
