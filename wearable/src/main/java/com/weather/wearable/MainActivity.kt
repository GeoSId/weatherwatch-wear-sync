package com.weather.wearable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material.*
import com.weather.wearable.presentation.AnimatedWeatherScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Wearable App
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AnimatedWeatherScreen()
            }
        }
    }
}