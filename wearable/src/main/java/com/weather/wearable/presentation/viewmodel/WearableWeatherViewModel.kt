package com.weather.wearable.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.wearable.data.WearableDataManager
import com.weather.wearable.data.repository.PhoneSyncRepository
import com.weather.wearable.data.repository.WearableSettingsRepository
import com.weather.wearable.data.repository.WearableWeatherRepository
import com.weather.wearable.domain.WearableWeatherData
import com.weather.wearable.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Unified ViewModel for wearable weather screen
 * Handles BOTH phone sync AND direct API calls
 * Single source of truth for all weather data
 * Follows MVVM architecture pattern
 */
@HiltViewModel
class WearableWeatherViewModel @Inject constructor(
    private val weatherRepository: WearableWeatherRepository,
    private val phoneSyncRepository: PhoneSyncRepository,
    private val settingsRepository: WearableSettingsRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "WearableWeatherVM"
        private const val SYNC_TIMEOUT_MS = 5000L
    }
    
    // Single source of truth for weather data
    private val _weatherData = MutableStateFlow<WearableWeatherData?>(null)
    val weatherData: StateFlow<WearableWeatherData?> = _weatherData.asStateFlow()
    
    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()
    
    // Phone connection status
    val isPhoneConnected: StateFlow<Boolean> = phoneSyncRepository.isPhoneConnected
    val isSyncing: StateFlow<Boolean> = phoneSyncRepository.isSyncing
    
    // Data source indicator
    private val _dataSource = MutableStateFlow("unknown")
    val dataSource: StateFlow<String> = _dataSource.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        // Load last searched city
        val lastCity = settingsRepository.getLastSearchedCity()
        _searchQuery.value = lastCity
        // Observe phone data from WearableDataManager
        observePhoneData()
        
        // Check phone connection and fetch weather
        viewModelScope.launch {
            checkConnectionAndFetchWeather()
        }
    }
    
    /**
     * Observe weather data from phone (via WearableDataManager)
     */
    private fun observePhoneData() {
        try {
            val dataManager = WearableDataManager.getInstance()
            
            dataManager.weatherData.onEach { phoneData ->
                if (phoneData != null) {
                    _weatherData.value = phoneData
                    _dataSource.value = "phone"
                    _error.value = ""
                    _isLoading.value = false
                    
                    // Notify phone sync repo that data was received
                    phoneSyncRepository.onDataReceivedFromPhone()
                }
            }.launchIn(viewModelScope)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Check phone connection and fetch weather using appropriate method
     */
    private suspend fun checkConnectionAndFetchWeather() {
        val isConnected = phoneSyncRepository.checkPhoneConnection()
        
        if (isConnected) {
            requestPhoneSync()
        } else {
            fetchWeatherFromApi()
        }
    }
    
    /**
     * Request weather data from phone via Data Layer
     */
    private suspend fun requestPhoneSync() {
        _isLoading.value = true
        _error.value = ""
        
        val success = phoneSyncRepository.requestWeatherSync()
        
        if (success) {

            // Wait for phone response with timeout
            viewModelScope.launch {
                delay(SYNC_TIMEOUT_MS)
                
                // If still syncing after timeout, fallback to API
                if (isSyncing.value) {
                    phoneSyncRepository.clearSyncingState()
                    fetchWeatherFromApi()
                }
            }
        } else {
            fetchWeatherFromApi()
        }
    }
    
    /**
     * Fetch weather directly from API (fallback)
     */
    private fun fetchWeatherFromApi() {
        val city = _searchQuery.value
        
        if (city.isBlank()) {
            _error.value = "City name is required"
            _isLoading.value = false
            return
        }
        

        weatherRepository.getCurrentWeather(city).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                    _error.value = ""
                }
                is Resource.Success -> {
                    _weatherData.value = result.data
                    _dataSource.value = "api"
                    _isLoading.value = false
                    _error.value = ""
                }
                is Resource.Error -> {
                    _error.value = result.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
            }
        }.launchIn(viewModelScope)
    }
    
    /**
     * Refresh weather data (smart: tries phone first, then API)
     */
    fun refreshWeather() {

        viewModelScope.launch {
            val isConnected = phoneSyncRepository.checkPhoneConnection()
            
            if (isConnected) {
                requestPhoneSync()
            } else {
                fetchWeatherFromApi()
            }
        }
    }
    
    /**
     * Update search query and fetch weather for new city
     */
    fun searchCity(city: String) {
        if (city.isBlank()) {
            Log.w(TAG, "Cannot search: city is blank")
            _error.value = "City name is required"
            return
        }
        
        _searchQuery.value = city
        settingsRepository.saveLastSearchedCity(city)

        // Always use API for new city searches (phone doesn't have this data yet)
        fetchWeatherFromApi()
    }
    
    /**
     * Check phone connection status
     */
    fun checkPhoneConnection() {
        viewModelScope.launch {
            phoneSyncRepository.checkPhoneConnection()
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = ""
    }
}

