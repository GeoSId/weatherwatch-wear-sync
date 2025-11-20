package com.weather.wearable.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.weather.wearable.constants.WearableConstants
import com.weather.wearable.domain.WearableWeatherData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling phone sync via Wear OS Data Layer
 * This is separate from API calls and handles only phone communication
 */
@Singleton
class PhoneSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "PhoneSyncRepo"
    }
    
    private val _isPhoneConnected = MutableStateFlow(false)
    val isPhoneConnected: StateFlow<Boolean> = _isPhoneConnected.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    /**
     * Check if phone is connected
     */
    suspend fun checkPhoneConnection(): Boolean {
        return try {
            val nodeClient = Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.await()
            val isConnected = nodes.isNotEmpty()
            
            _isPhoneConnected.value = isConnected
            
            Log.d(TAG, "Phone connection check: $isConnected (${nodes.size} nodes)")
            nodes.forEach { node ->
                Log.d(TAG, "Connected node: ${node.displayName} (${node.id})")
            }
            
            isConnected
        } catch (e: Exception) {
            Log.e(TAG, "Error checking phone connection", e)
            _isPhoneConnected.value = false
            false
        }
    }
    
    /**
     * Request weather data sync from phone
     * Returns true if request was sent successfully
     */
    suspend fun requestWeatherSync(): Boolean {
        return try {
            _isSyncing.value = true
            
            val nodeClient = Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.await()
            
            if (nodes.isEmpty()) {
                Log.w(TAG, "No connected nodes found for sync request")
                _isSyncing.value = false
                return false
            }
            
            val messageClient = Wearable.getMessageClient(context)
            val syncMessage = WearableConstants.Sync.FULL_SYNC.toByteArray()
            val syncPath = WearableConstants.DataLayer.SYNC_REQUEST_PATH
            
            var sentSuccessfully = false
            nodes.forEach { node ->
                try {
                    messageClient.sendMessage(node.id, syncPath, syncMessage).await()
                    sentSuccessfully = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "Failed to send sync to ${node.displayName}", e)
                }
            }
            
            // Keep syncing state for a moment (will be cleared when data arrives or timeout)
            sentSuccessfully
            
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting weather sync", e)
            _isSyncing.value = false
            false
        }
    }
    
    /**
     * Called when data is received from phone
     * Should be called by DataLayerListenerService
     */
    fun onDataReceivedFromPhone() {
        _isSyncing.value = false
        Log.d(TAG, "Data received from phone - sync complete")
    }
    
    /**
     * Clear syncing state (for timeout scenarios)
     */
    fun clearSyncingState() {
        _isSyncing.value = false
    }
}

