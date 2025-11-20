package com.weather.data.wearable

import android.content.Context
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.weather.domain.model.WeatherResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for syncing weather data from mobile app to wearable devices
 * Handles bidirectional communication with paired wearables
 */
@Singleton
class WearableDataSyncService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val SYNC_REQUEST_PATH = "/sync_request"
        private const val WEATHER_MESSAGE_PATH = "/weather_message"
    }
    
    private val gson = Gson()

    /**
     * Send weather data to all connected wearable devices
     */
    suspend fun sendWeatherDataToWatch(weatherData: WeatherResponse) {
        try {
            val wearableData = convertToWearableWeatherData(weatherData)

            // Also send via MessageClient for immediate delivery
            try {
                sendWeatherDataViaMessage(wearableData)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * Send weather data via MessageClient for immediate delivery
     * Complements DataClient to ensure instant updates on the wearable
     */
    private suspend fun sendWeatherDataViaMessage(weatherData: WearableWeatherData) {
        try {
            val messageClient = Wearable.getMessageClient(context)
            val nodes = getConnectedWearables()
            
            val weatherJson = gson.toJson(weatherData)
            val messageBytes = weatherJson.toByteArray(Charsets.UTF_8)
            
            nodes.forEach { node ->
                try {
                    messageClient.sendMessage(node.id, WEATHER_MESSAGE_PATH, messageBytes).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Send a sync request message to wearable devices
     */
    suspend fun requestSyncFromWearable(syncType: String) {
        try {
            val client = Wearable.getMessageClient(context)
            val nodes = getConnectedWearables()
            
            nodes.forEach { node ->
                client.sendMessage(node.id, SYNC_REQUEST_PATH, syncType.toByteArray()).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Check if any wearable devices are connected
     */
    suspend fun isWearableConnected(): Boolean {
        return try {
            val client = Wearable.getNodeClient(context)
            val nodes = client.connectedNodes.await()
            nodes.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get list of connected wearable devices
     */
    suspend fun getConnectedWearables(): List<Node> {
        return try {
            val client = Wearable.getNodeClient(context)
            client.connectedNodes.await()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Private helper methods
    
    /**
     * Convert WeatherResponse to wearable-compatible format
     */
    private fun convertToWearableWeatherData(weather: WeatherResponse): WearableWeatherData {
        return WearableWeatherData(
            temperature = weather.main.temp.toInt(),
            condition = weather.weather.firstOrNull()?.main ?: "Unknown",  // Use .main for consistency
            humidity = weather.main.humidity,
            windSpeed = weather.wind.speed,
            cityName = weather.name,
            timestamp = System.currentTimeMillis()
        )
    }
}

/**
 * Simplified weather data model for wearable transmission
 */
data class WearableWeatherData(
    val temperature: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: Float,
    val cityName: String,
    val timestamp: Long
)
