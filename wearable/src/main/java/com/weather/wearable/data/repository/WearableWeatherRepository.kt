package com.weather.wearable.data.repository

import android.util.Log
import com.weather.wearable.data.api.WearableWeatherApiService
import com.weather.wearable.domain.WearableWeatherData
import com.weather.wearable.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fetching weather data
 * Handles API calls and error handling
 * Follows MVVM architecture pattern
 */
@Singleton
class WearableWeatherRepository @Inject constructor(
    private val api: WearableWeatherApiService,
    private val settingsRepository: WearableSettingsRepository
) {
    
    companion object {
        private const val TAG = "WearableWeatherRepo"
    }
    
    /**
     * Fetch current weather data for a city
     * Returns Flow<Resource<WearableWeatherData>> for reactive UI updates
     */
    fun getCurrentWeather(city: String): Flow<Resource<WearableWeatherData>> = flow {
        emit(Resource.Loading())
        
        try {
            Log.d(TAG, "Fetching weather for: $city")
            
            val apiKey = settingsRepository.getApiKey()
            if (apiKey.isEmpty()) {
                emit(Resource.Error("API key not configured"))
                return@flow
            }
            
            val response = api.getCurrentWeather(city, apiKey = apiKey)
            val weatherData = response.toWearableWeatherData()
            
            // Save last searched city
            settingsRepository.saveLastSearchedCity(city)
            
            Log.d(TAG, "Weather fetched successfully: ${weatherData.cityName}, ${weatherData.temperature}°C")
            emit(Resource.Success(weatherData))
            
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Invalid API key. Please check your settings."
                404 -> "City not found: $city"
                else -> "HTTP Error: ${e.code()}"
            }
            Log.e(TAG, "HTTP error: ${e.code()}", e)
            emit(Resource.Error(errorMsg))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
    
    /**
     * Refresh weather data for the last searched city
     */
    fun refreshWeather(): Flow<Resource<WearableWeatherData>> {
        val lastCity = settingsRepository.getLastSearchedCity()
        return getCurrentWeather(lastCity)
    }
}

