package com.weather.wearable.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.weather.wearable.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing app settings
 * Handles API key and preferences storage
 */
@Singleton
class WearableSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val PREFS_NAME = "wearable_settings_prefs"
        private const val API_KEY = "api_key"
        private const val LAST_SEARCHED_CITY = "last_searched_city"
        private const val DEFAULT_CITY = "London"
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Get the API key
     * First checks user-saved key, then falls back to BuildConfig
     */
    fun getApiKey(): String {
        val userApiKey = prefs.getString(API_KEY, null)
        return if (userApiKey.isNullOrEmpty()) {
            BuildConfig.FALLBACK_API_KEY
        } else {
            userApiKey
        }
    }
    
    /**
     * Save a custom API key
     */
    fun saveApiKey(apiKey: String) {
        prefs.edit { putString(API_KEY, apiKey) }
    }
    
    /**
     * Get the last searched city
     */
    fun getLastSearchedCity(): String {
        return prefs.getString(LAST_SEARCHED_CITY, DEFAULT_CITY) ?: DEFAULT_CITY
    }
    
    /**
     * Save the last searched city
     */
    fun saveLastSearchedCity(city: String) {
        prefs.edit { putString(LAST_SEARCHED_CITY, city) }
    }
}

