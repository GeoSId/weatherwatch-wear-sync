package com.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.weather.presentation.screens.*
import com.weather.ui.theme.WeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.Weather) }
    BackHandler(enabled = currentScreen != Screen.Weather) {
        when (currentScreen) {
            Screen.Settings -> currentScreen = Screen.Weather
            else -> currentScreen = Screen.Weather
        }
    }
    
    AnimatedVisibility(
        visible = currentScreen == Screen.Weather,
        enter = fadeIn(animationSpec = tween(300)) + 
                slideInHorizontally(
                    initialOffsetX = { -it }, // Slide in from left
                    animationSpec = tween(300)
                ),
        exit = fadeOut(animationSpec = tween(300)) + 
               slideOutHorizontally(
                   targetOffsetX = { -it }, // Slide out to left
                   animationSpec = tween(300)
               )
    ) {
        WeatherScreen(
            onNavigateToSettings = { currentScreen = Screen.Settings },
        )
    }
    
    AnimatedVisibility(
        visible = currentScreen == Screen.Settings,
        enter = fadeIn(animationSpec = tween(300)) + 
                slideInHorizontally(
                    initialOffsetX = { it }, // Slide in from right
                    animationSpec = tween(300)
                ),
        exit = fadeOut(animationSpec = tween(300)) + 
               slideOutHorizontally(
                   targetOffsetX = { it }, // Slide out to right
                   animationSpec = tween(300)
               )
    ) {
        SettingsScreen(
            onBackClick = { currentScreen = Screen.Weather },
        )
    }
}

enum class Screen {
    Weather,
    Settings
} 