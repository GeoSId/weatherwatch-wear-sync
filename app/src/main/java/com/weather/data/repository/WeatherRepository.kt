package com.weather.data.repository

import com.weather.data.api.WeatherApiService
import com.weather.data.prediction.WeatherPredictionService
import com.weather.domain.model.WeatherResponse
import com.weather.domain.model.ForecastResponse
import com.weather.domain.model.WeatherPrediction
import com.weather.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: WeatherApiService,
    private val settingsRepository: SettingsRepository,
    private val predictionService: WeatherPredictionService
) {
    fun getCurrentWeather(city: String): Flow<Resource<WeatherResponse>> = flow {
        emit(Resource.Loading())
        try {
            val apiKey = settingsRepository.getApiKey().first()
            val response = api.getCurrentWeather(city, apiKey = apiKey)
            emit(Resource.Success(response))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Invalid API key. Please check your API key in settings."
                404 -> "City not found: $city"
                else -> "HTTP Error: ${e.code()} - ${e.message()}"
            }
            emit(Resource.Error(errorMsg))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
    
    fun getForecast(city: String): Flow<Resource<ForecastResponse>> = flow {
        emit(Resource.Loading())
        try {
            val apiKey = settingsRepository.getApiKey().first()
            val response = api.getForecast(city, apiKey = apiKey)
            emit(Resource.Success(response))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Invalid API key. Please check your API key in settings."
                404 -> "City not found: $city"
                else -> "HTTP Error: ${e.code()} - ${e.message()}"
            }
            emit(Resource.Error(errorMsg))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
    
    /**
     * Get AI-powered weather predictions for future days
     */
    fun getAiPredictions(city: String): Flow<Resource<List<WeatherPrediction>>> {
        // We'll use the forecast data to generate predictions
        return getForecast(city).map { resource ->
            when (resource) {
                is Resource.Success -> {
                    val forecastData = resource.data
                    if (forecastData != null) {
                        try {
                            val predictions = predictionService.generatePredictions(forecastData)
                            Resource.Success(predictions)
                        } catch (e: Exception) {
                            Resource.Error("Failed to generate AI predictions: ${e.message}")
                        }
                    } else {
                        Resource.Error("No forecast data available for predictions")
                    }
                }
                is Resource.Error -> Resource.Error(resource.message ?: "Unknown error")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }
} 