package com.weather.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.weather.BuildConfig
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "weather_app_preferences"
        private const val API_KEY = "api_key"
        private const val LAST_SEARCHED_CITY = "last_searched_city"
        private const val DEFAULT_CITY = "London"
    }
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getApiKey(): Flow<String> = flow {
        val userApiKey = sharedPreferences.getString(API_KEY, null)
        val apiKey = if (userApiKey.isNullOrEmpty()) {
            BuildConfig.WEATHER_API_KEY
        } else {
            userApiKey
        }
        emit(apiKey)
    }

    fun saveApiKey(apiKey: String): Flow<Boolean> = flow {
        sharedPreferences.edit { putString(API_KEY, apiKey) }
        emit(true)
    }

    fun getLastSearchedCity(): String {
        return sharedPreferences.getString(LAST_SEARCHED_CITY, DEFAULT_CITY) ?: DEFAULT_CITY
    }

    fun saveLastSearchedCity(city: String) {
        sharedPreferences.edit { putString(LAST_SEARCHED_CITY, city) }
    }
} 