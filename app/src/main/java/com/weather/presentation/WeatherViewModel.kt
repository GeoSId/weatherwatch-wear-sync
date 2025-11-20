package com.weather.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.data.cache.WeatherDataCache
import com.weather.data.repository.WeatherRepository
import com.weather.data.wearable.WearableDataSyncService
import com.weather.domain.model.WeatherResponse
import com.weather.domain.model.ForecastResponse
import com.weather.domain.model.WeatherData
import com.weather.domain.model.WeatherPrediction
import com.weather.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val wearableDataSyncService: WearableDataSyncService,
    private val weatherDataCache: WeatherDataCache,
    private val settingsRepository: com.weather.data.repository.SettingsRepository
) : ViewModel() {

    private val _currentWeatherState = mutableStateOf(CurrentWeatherState())
    val currentWeatherState: State<CurrentWeatherState> = _currentWeatherState

    private val _forecastState = mutableStateOf(ForecastState())
    val forecastState: State<ForecastState> = _forecastState
    
    private val _predictionsState = mutableStateOf(PredictionsState())
    val predictionsState: State<PredictionsState> = _predictionsState

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery
    
    // Selected forecast item for detailed view
    private val _selectedForecast = mutableStateOf<WeatherData?>(null)
    val selectedForecast: State<WeatherData?> = _selectedForecast
    
    // Show detailed forecast screen flag
    private val _showDetailScreen = mutableStateOf(false)
    val showDetailScreen: State<Boolean> = _showDetailScreen

    init {
        // Load last searched city from SharedPreferences
        val lastSearchedCity = settingsRepository.getLastSearchedCity()
        _searchQuery.value = lastSearchedCity
        
        // Send cached data to wearable immediately on startup if available
        viewModelScope.launch {
            sendCachedDataToWearable()
        }
        
        // Get weather for the last searched city
        getWeather(lastSearchedCity)
    }
    
    /**
     * Send cached weather data to wearable if available
     * This ensures wearable has data even if mobile app just opened
     */
    private suspend fun sendCachedDataToWearable() {
        try {
            val cachedData = weatherDataCache.getCachedWeatherDataAnyAge()
            if (cachedData != null) {
                syncWeatherDataToWearable(cachedData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Sync weather data to wearable device immediately
     * Called whenever new weather data is fetched from search
     */
    private suspend fun syncWeatherDataToWearable(weatherData: WeatherResponse) {
        try {
            val isConnected = wearableDataSyncService.isWearableConnected()
            if (isConnected) {
                wearableDataSyncService.sendWeatherDataToWatch(weatherData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // Save the searched city to SharedPreferences
        settingsRepository.saveLastSearchedCity(query)
    }
    
    fun selectForecastItem(weatherData: WeatherData) {
        _selectedForecast.value = weatherData
        _showDetailScreen.value = true
    }
    
    fun closeDetailScreen() {
        _showDetailScreen.value = false
    }

    fun getWeather(city: String) {
        repository.getCurrentWeather(city).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _currentWeatherState.value = CurrentWeatherState(
                        weatherData = result.data,
                        isLoading = false
                    )
                    
                    // Cache weather data and sync to wearable devices immediately
                    result.data?.let { weatherData ->
                        // Cache the data locally
                        weatherDataCache.cacheWeatherData(weatherData)
                        
                        // Immediately sync to wearable devices
                        viewModelScope.launch {
                            syncWeatherDataToWearable(weatherData)
                        }
                    }
                }
                is Resource.Error -> {
                    _currentWeatherState.value = CurrentWeatherState(
                        error = result.message ?: "An unexpected error occurred",
                        isLoading = false
                    )
                }
                is Resource.Loading -> {
                    _currentWeatherState.value = CurrentWeatherState(
                        isLoading = true
                    )
                }
            }
        }.launchIn(viewModelScope)

        repository.getForecast(city).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _forecastState.value = ForecastState(
                        forecastData = result.data,
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _forecastState.value = ForecastState(
                        error = result.message ?: "An unexpected error occurred",
                        isLoading = false
                    )
                }
                is Resource.Loading -> {
                    _forecastState.value = ForecastState(
                        isLoading = true
                    )
                }
            }
        }.launchIn(viewModelScope)
        
        // Get AI predictions
        repository.getAiPredictions(city).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _predictionsState.value = PredictionsState(
                        predictions = result.data ?: emptyList(),
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _predictionsState.value = PredictionsState(
                        error = result.message ?: "An unexpected error occurred",
                        isLoading = false
                    )
                }
                is Resource.Loading -> {
                    _predictionsState.value = PredictionsState(
                        isLoading = true
                    )
                }
            }
        }.launchIn(viewModelScope)
    }
}

data class CurrentWeatherState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val error: String = ""
)

data class ForecastState(
    val isLoading: Boolean = false,
    val forecastData: ForecastResponse? = null,
    val error: String = ""
)

data class PredictionsState(
    val isLoading: Boolean = false,
    val predictions: List<WeatherPrediction> = emptyList(),
    val error: String = ""
) 