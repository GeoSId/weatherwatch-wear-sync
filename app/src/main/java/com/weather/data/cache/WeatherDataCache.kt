package com.weather.data.cache

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.weather.domain.model.WeatherResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache manager for weather data
 * Persists weather data locally to enable sharing with wearable on app startup
 */
@Singleton
class WeatherDataCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "WeatherDataCache"
        private const val PREFS_NAME = "weather_cache"
        private const val KEY_WEATHER_DATA = "cached_weather_data"
        private const val KEY_LAST_UPDATE = "last_update_time"
        private const val KEY_LAST_CITY = "last_city"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Cache weather data locally
     */
    fun cacheWeatherData(weatherData: WeatherResponse) {
        try {
            val json = gson.toJson(weatherData)
            prefs.edit {
                putString(KEY_WEATHER_DATA, json)
                putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                putString(KEY_LAST_CITY, weatherData.name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get cached weather data regardless of age
     * Useful for sending to wearable even if slightly stale
     */
    fun getCachedWeatherDataAnyAge(): WeatherResponse? {
        return try {
            val json = prefs.getString(KEY_WEATHER_DATA, null) ?: return null
            gson.fromJson(json, WeatherResponse::class.java).also {
                Log.d(TAG, "Retrieved cached weather data (any age) for ${it.name}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get the last cached city name
     */
    fun getLastCity(): String? {
        return prefs.getString(KEY_LAST_CITY, null)
    }
}

