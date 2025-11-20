package com.weather.data.wearable

import android.util.Log
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Service to handle incoming messages and data from wearable devices
 * This is a separate service from WearableDataSyncService to handle Android Service lifecycle
 */
class WearableListenerServiceImpl : WearableListenerService() {
    
    companion object {
        private const val TAG = "WearableListener"
        // Data Layer paths - must match wearable constants
        private const val SYNC_REQUEST_PATH = "/sync_request"
        private const val WEATHER_DATA_PATH = "/weather_data"
    }
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        // Return START_STICKY to tell Android to recreate the service if it's killed
        val result = super.onStartCommand(intent, flags, startId)
        return result
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "WearableListenerService DESTROYED")
    }
    
    /**
     * Handle incoming messages from wearable devices
     */
    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        
        scope.launch {
            try {
                when (messageEvent.path) {
                    SYNC_REQUEST_PATH -> {
                        val syncType = String(messageEvent.data)
                        try {
                            handleSyncRequest(syncType)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    else -> {
                        Log.w(TAG, "❓ Unknown message path received: ${messageEvent.path}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Handle incoming data from wearable devices
     */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        
        // Extract data immediately before buffer is closed
        val dataItems = mutableListOf<Pair<Int, DataItem>>()
        dataEvents.forEach { event ->
            try {
                Log.d(TAG, "Processing event: type=${event.type}, path=${event.dataItem.uri.path}")
                // Create a copy of the DataItem to avoid buffer closure issues
                dataItems.add(Pair(event.type, event.dataItem.freeze()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Process extracted data in coroutine
        scope.launch {
            dataItems.forEach { (eventType, dataItem) ->
                when (eventType) {
                    DataEvent.TYPE_CHANGED -> {
                        handleDataChanged(dataItem)
                    }
                    DataEvent.TYPE_DELETED -> {}
                }
            }
        }
    }
    
    /**
     * Handle capability changes (wearable connect/disconnect)
     */
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        
        // If wearables are available, proactively send data
        if (capabilityInfo.nodes.isNotEmpty()) {
            scope.launch {
                try {
                    kotlinx.coroutines.delay(1000) // Give wearable time to initialize
                    
                    val cache = com.weather.data.cache.WeatherDataCache(applicationContext)
                    val cachedData = cache.getCachedWeatherDataAnyAge()
                    
                    if (cachedData != null) {
                        val syncService = WearableDataSyncService(applicationContext)
                        syncService.sendWeatherDataToWatch(cachedData)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    override fun onPeerConnected(peer: Node) {
        super.onPeerConnected(peer)
        // Proactively send cached data when wearable connects
        // This helps when wearable is freshly installed or reconnects
        scope.launch {
            try {
                // Give the wearable a moment to fully initialize
                kotlinx.coroutines.delay(1000)
                
                val cache = com.weather.data.cache.WeatherDataCache(applicationContext)
                val cachedData = cache.getCachedWeatherDataAnyAge()
                
                if (cachedData != null) {
                    val syncService = WearableDataSyncService(applicationContext)
                    syncService.sendWeatherDataToWatch(cachedData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPeerDisconnected(peer: Node) {
        super.onPeerDisconnected(peer)
        Log.d(TAG, "Wearable peer disconnected: ${peer.displayName} (${peer.id})")
    }

    // Private helper methods
    private suspend fun handleDataChanged(dataItem: DataItem) {
        try {
            val path = dataItem.uri.path

            when {
                path == WEATHER_DATA_PATH || path?.startsWith("$WEATHER_DATA_PATH/") == true -> {
                    // This service is for mobile app, not wearable
                }
                
                path == "/sync_request_trigger" || path?.startsWith("/sync_request_trigger/") == true -> {
                    // Wearable sent sync request via DataClient
                    // This is the WORKING method since MessageClient isn't reaching onMessageReceived
                    try {
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                        val syncType = dataMap.getString("sync_type", "full_sync")
                        val requestTime = dataMap.getLong("request_time", 0)
                        
                        // Ignore old requests (older than 10 seconds)
                        val requestAge = System.currentTimeMillis() - requestTime
                        if (requestAge > 10000) {
                            Log.w(TAG, "Ignoring old sync request (age=${requestAge}ms)")
                            return
                        }

                        handleSyncRequest(syncType)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                else -> {
                    Log.d(TAG, "📱 Unknown data path: $path")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error handling data changed", e)
            e.printStackTrace()
        }
    }
    
    private suspend fun handleSyncRequest(syncType: String) {
        try {
            
            val normalized = when (syncType) {
                "full_sync" -> "all"
                else -> syncType
            }

            when (normalized) {
                "weather", "all" -> {
                    val syncService = WearableDataSyncService(applicationContext)
                    
                    // STEP 1: Send cached data immediately if available
                    // This provides instant response to the wearable
                    try {
                        val cache = com.weather.data.cache.WeatherDataCache(applicationContext)
                        val cachedData = cache.getCachedWeatherDataAnyAge()
                        
                        if (cachedData != null) {
                            syncService.sendWeatherDataToWatch(cachedData)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    // STEP 2: Fetch fresh data in the background
                    // This ensures the wearable gets the most up-to-date information
                    try {
                        // Build a small Retrofit client to fetch current weather (avoids DI in Service)
                        val okHttp = okhttp3.OkHttpClient.Builder()
                            .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply {
                                level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
                            })
                            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                            .build()

                        val retrofit = retrofit2.Retrofit.Builder()
                            .baseUrl("https://api.openweathermap.org/data/2.5/")
                            .client(okHttp)
                            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                            .build()

                        val api = retrofit.create(com.weather.data.api.WeatherApiService::class.java)

                        // Use the app's configured API key
                        val apiKey = com.weather.BuildConfig.WEATHER_API_KEY

                        // Get the last cached city or use default
                        val cache = com.weather.data.cache.WeatherDataCache(applicationContext)
                        val city = cache.getLastCity() ?: "London"
                        
                        val weather = api.getCurrentWeather(cityName = city, apiKey = apiKey)

                        // Send fresh data to wearable
                        syncService.sendWeatherDataToWatch(weather)

                        // Update cache with fresh data
                        cache.cacheWeatherData(weather)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Cached data was already sent, so wearable has something to display
                    }
                }
                else -> {
                    Log.w(TAG, "⚠️ Unknown sync type: $syncType")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
