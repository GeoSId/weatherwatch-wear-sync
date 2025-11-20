package com.weather.wearable.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import com.google.android.gms.wearable.DataItem
import com.weather.wearable.constants.WearableConstants
import com.weather.wearable.domain.WearableWeatherData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.gson.Gson

/**
 * Data Layer Listener Service for handling communication between phone and wearable
 * 
 * This service handles:
 * - Receiving weather data updates from the phone
 * - Managing bidirectional communication
 */
class DataLayerListenerService : WearableListenerService() {
    
    companion object {
        private const val TAG = "DataLayerListener"
        private const val NOTIFICATION_CHANNEL_ID = "wearable_sync_channel"
        private const val NOTIFICATION_ID = 1001
    }
    
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        // Start as foreground service to prevent Android from killing it
        try {
            startForegroundService()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Start the service in foreground mode with a notification
     * This prevents Android from killing the service when app goes to background
     */
    private fun startForegroundService() {
        try {
            // Create notification channel for Android O+
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Weather Sync",
                NotificationManager.IMPORTANCE_LOW // Low importance = no sound/vibration
            ).apply {
                description = "Syncing weather data with phone"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            // Create notification
            val notification =
                Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .apply {
                setContentTitle("Weather Sync Active")
                setContentText("Listening for updates from phone")
                setSmallIcon(android.R.drawable.ic_dialog_info)
                setOngoing(true) // Cannot be dismissed by user
                setPriority(Notification.PRIORITY_MIN) // Minimal priority
            }.build()
            
            // Start foreground
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service", e)
        }
    }
    
    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        // Return START_STICKY to keep service alive
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()

        // Stop foreground service
        try {
            stopForeground(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        // Extract data immediately before buffer is closed
        // DataEventBuffer closes after this method returns, so we need to copy data now
        val dataItems = mutableListOf<Pair<Int, DataItem>>()
        dataEvents.forEach { event ->
            try {
                // Freeze creates a copy that survives buffer closure
                dataItems.add(Pair(event.type, event.dataItem.freeze()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Now process the copied data in coroutine
        scope.launch {
            dataItems.forEach { (eventType, dataItem) ->
                when (eventType) {
                    DataEvent.TYPE_CHANGED -> handleDataChanged(dataItem)
                    DataEvent.TYPE_DELETED -> handleDataDeleted(dataItem)
                }
            }
        }
    }
    
    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        
        scope.launch {
            handleMessageReceived(messageEvent)
        }
    }
    
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        
        scope.launch {
            handleCapabilityChanged(capabilityInfo)
        }
    }
    
    private fun handleDataChanged(dataItem: DataItem) {
        try {
            val path = dataItem.uri.path

            // Check if path matches weather data (exact or with timestamp suffix)
            when {
                path == WearableConstants.DataLayer.WEATHER_DATA_PATH || 
                path?.startsWith("${WearableConstants.DataLayer.WEATHER_DATA_PATH}/") == true -> {
                    val weatherData = extractWeatherData(dataItem)
                    if (weatherData != null) {
                        // Send to WearableDataManager (ViewModel will observe it)
                        WearableDataManager.getInstance().updateWeatherData(weatherData)
                    }
                }
                
                path == "/sync_request_trigger" || path?.startsWith("/sync_request_trigger/") == true -> {
                    // This is our own sync request echoed back - ignore it
                }
                
                else -> {
                    Log.w(TAG, "Unknown data path: $path")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun handleDataDeleted(dataItem: DataItem) {
        try {
            when (dataItem.uri.path) {
                WearableConstants.DataLayer.WEATHER_DATA_PATH -> {
                    WearableDataManager.getInstance().clearWeatherData()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun handleMessageReceived(messageEvent: MessageEvent) {
        try {
            when (messageEvent.path) {
                "/weather_message" -> {
                    // Receive weather data via MessageClient for immediate updates
                    try {
                        val messageJson = String(messageEvent.data, Charsets.UTF_8)
                        val weatherData = gson.fromJson(messageJson, WearableWeatherData::class.java)
                        if (weatherData != null) {
                            // Send to WearableDataManager (ViewModel will observe it)
                            WearableDataManager.getInstance().updateWeatherData(weatherData)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                WearableConstants.DataLayer.SYNC_REQUEST_PATH -> {
                    // Sync request from phone - we ignore this now
                    // ViewModel handles all sync logic
                    val syncType = String(messageEvent.data)
                    Log.d(TAG, "Received sync request: $syncType (ViewModel handles this)")
                }
                
                else -> {
                    Log.d(TAG, "Unknown message path: ${messageEvent.path}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun handleCapabilityChanged(capabilityInfo: CapabilityInfo) {
        try {
            // Log detailed node information
            capabilityInfo.nodes.forEach { node ->
                Log.d(TAG, "Node: ${node.displayName} (${node.id}) - isNearby: ${node.isNearby}")
            }
            // Connection tracking is now handled by PhoneSyncRepository
            val isConnected = capabilityInfo.nodes.isNotEmpty()
            Log.d(TAG, "Connection status: $isConnected")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error handling capability changed", e)
        }
    }
    
    override fun onPeerConnected(peer: Node) {
        super.onPeerConnected(peer)
        Log.d(TAG, "Peer connected: ${peer.displayName} (${peer.id})")
        // Connection tracking is now handled by PhoneSyncRepository in ViewModel
    }
    
    override fun onPeerDisconnected(peer: Node) {
        super.onPeerDisconnected(peer)
        Log.d(TAG, "Peer disconnected: ${peer.displayName} (${peer.id})")
        // Connection tracking is now handled by PhoneSyncRepository in ViewModel
    }
    
    private fun extractWeatherData(dataItem: DataItem): WearableWeatherData? {
        return try {
            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
            val weatherJson = dataMap.getString(WearableConstants.DataLayer.WEATHER_DATA_KEY)
            
            if (weatherJson != null) {
                gson.fromJson(weatherJson, WearableWeatherData::class.java)
            } else {
                // Fallback: extract individual fields
                WearableWeatherData(
                    temperature = dataMap.getInt(WearableConstants.DataLayer.TEMPERATURE_KEY),
                    condition = dataMap.getString(WearableConstants.DataLayer.CONDITION_KEY) ?: WearableConstants.Labels.STATUS_UNKNOWN,
                    humidity = dataMap.getInt(WearableConstants.DataLayer.HUMIDITY_KEY),
                    windSpeed = dataMap.getFloat(WearableConstants.DataLayer.WIND_SPEED_KEY),
                    cityName = dataMap.getString(WearableConstants.DataLayer.LOCATION_KEY) ?: WearableConstants.Labels.STATUS_UNKNOWN,
                    timestamp = dataMap.getLong(WearableConstants.DataLayer.TIMESTAMP_KEY)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting weather data", e)
            null
        }
    }
} 