package com.weather.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.weather.R
import com.weather.domain.model.WeatherData
import com.weather.presentation.WeatherViewModel
import com.weather.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
) {
    val currentWeatherState = viewModel.currentWeatherState.value
    val forecastState = viewModel.forecastState.value
    val predictionsState = viewModel.predictionsState.value
    val searchQuery = viewModel.searchQuery.value
    val showDetailScreen = viewModel.showDetailScreen.value
    val selectedForecast = viewModel.selectedForecast.value
    
    var searchText by remember { mutableStateOf(searchQuery) }
    
    // Update searchText when searchQuery changes (e.g., on app restart)
    LaunchedEffect(searchQuery) {
        searchText = searchQuery
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    
    val bgAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    // Show detail screen if a forecast item is selected
    if (showDetailScreen && selectedForecast != null && currentWeatherState.weatherData != null) {
        // Get sunrise/sunset from current weather data
        val sunriseTime = currentWeatherState.weatherData.sys.sunrise
        val sunsetTime = currentWeatherState.weatherData.sys.sunset
        
        DayForecastScreen(
            weatherData = selectedForecast,
            sunriseTime = sunriseTime,
            sunsetTime = sunsetTime,
            onBackClick = { viewModel.closeDetailScreen() }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF02111D),
                            Color(0xFF05445E)
                        )
                    )
                )
                .drawBehind {
                    // Animated background elements
                    for (i in 0 until 20) {
                        val angle = Math.toRadians((bgAnimation + i * 18).toDouble())
                        val radius = size.minDimension * (0.3f + (i % 5) * 0.1f)
                        val x = center.x + cos(angle) * radius
                        val y = center.y + sin(angle) * radius

                        drawCircle(
                            color = Color(0xFF75E6DA).copy(alpha = 0.05f),
                            radius = size.minDimension * 0.05f,
                            center = Offset(x.toFloat(), y.toFloat())
                        )
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App title with futuristic styling and settings button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = stringResource(R.string.weather),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 48.dp)
                    )
                    
                    // Animated settings button
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.9f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    
                    Surface(
                        onClick = onNavigateToSettings,
                        interactionSource = interactionSource,
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(scale),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings),
                                tint = Color(0xFF75E6DA),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                // Search bar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text(stringResource(R.string.search_city), color = Color.White.copy(alpha = 0.6f)) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.setSearchQuery(searchText)
                                viewModel.getWeather(searchText)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search),
                                tint = Color(0xFF75E6DA)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.setSearchQuery(searchText)
                            viewModel.getWeather(searchText)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                
                if (currentWeatherState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(50.dp)
                            .padding(16.dp),
                        color = Color(0xFF75E6DA)
                    )
                } else if (currentWeatherState.error.isNotEmpty()) {
                    Text(
                        text = currentWeatherState.error,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } else {
                    currentWeatherState.weatherData?.let { weather ->
                        // City name with animation
                        val cityPulse by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = EaseInOutCubic),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        
                        Text(
                            text = weather.name.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF75E6DA),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .alpha(cityPulse)
                                .padding(bottom = 8.dp)
                        )
                        
                        // Main weather display with animations
                        FuturisticWeatherCard(
                            temperature = "${weather.main.temp.toInt()}°C",
                            weatherCondition = weather.weather.firstOrNull()?.main ?: stringResource(
                                R.string.unknown
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AnimatedWeatherIcon(
                                        weatherCondition = weather.weather.firstOrNull()?.main?: stringResource(
                                            R.string.unknown
                                        )
                                    )
                                }
                                
                                Column {
                                    WeatherDataRow(
                                        label = "Feels Like",
                                        value = "${weather.main.feels_like.toInt()}°C"
                                    )
                                    
                                    WeatherDataRow(
                                        label = "Humidity",
                                        value = "${weather.main.humidity}%"
                                    )
                                    
                                    WeatherDataRow(
                                        label = stringResource(R.string.wind),
                                        value = "${weather.wind.speed} m/s"
                                    )
                                }
                            }
                        }
                        
                        // Date and time display
                        val currentDate = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
                            .format(Date(weather.dt * 1000))
                        
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                        
                        // Weather forecast section
                        Text(
                            text = stringResource(R.string.forecast),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF75E6DA),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        
                        if (forecastState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                color = Color(0xFF75E6DA)
                            )
                        } else if (forecastState.forecastData != null) {
                            ForecastSection(forecastState.forecastData.list, viewModel)
                        }
                        
                        // AI Predictions Section
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Divider(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth(0.8f),
                                color = Color(0xFF75E6DA).copy(alpha = 0.3f)
                            )
                        }
                        
                        if (predictionsState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                color = Color(0xFF75E6DA)
                            )
                        } else if (predictionsState.error.isNotEmpty()) {
                            Text(
                                text = predictionsState.error,
                                color = Color.Red.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else if (predictionsState.predictions.isNotEmpty()) {
                            AiPredictionsSection(
                                predictions = predictionsState.predictions
                            )
                        }
                    }
                }
                
                // Futuristic design elements for empty space
                FuturisticBackgroundElements()
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun ForecastSection(
    forecast: List<WeatherData>,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(forecast.take(10)) { weatherData ->
            ForecastItem(
                weatherData = weatherData,
                onClick = { viewModel.selectForecastItem(weatherData) }
            )
        }
    }
}

@Composable
fun ForecastItem(
    weatherData: WeatherData,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, HH:mm", Locale.getDefault())
    val date = Date(weatherData.dt * 1000)
    
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(180.dp)
            .pointerInput(Unit) {
                detectTapGestures {
                    onClick()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0E1621).copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateFormat.format(date),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            
            AnimatedWeatherIcon(
                weatherCondition = weatherData.weather.firstOrNull()?.main ?: "Unknown",
                modifier = Modifier.size(60.dp)
            )
            
            Text(
                text = "${weatherData.main.temp.toInt()}°C",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            
            Text(
                text = weatherData.weather.firstOrNull()?.main ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF75E6DA)
            )
        }
    }
}

@Composable
fun FuturisticBackgroundElements() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 0 until 4) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF75E6DA).copy(alpha = pulse * 0.5f))
                    .blur(radius = 4.dp * pulse)
            )
        }
    }
} 