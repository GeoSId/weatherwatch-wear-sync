package com.weather.wearable

import android.app.Application
import com.weather.wearable.data.WearableDataManager
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Wearable App
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class WearableWeatherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize WearableDataManager
        WearableDataManager.initialize(this)
    }
}