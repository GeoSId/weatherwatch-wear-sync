package com.weather.wearable.presentation

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.wear.compose.material.*
import com.weather.wearable.R
import com.weather.wearable.constants.WearableConstants
import com.weather.wearable.data.WearableDataManager
import com.weather.wearable.presentation.viewmodel.WearableWeatherViewModel
import kotlin.math.cos
import kotlin.math.sin

// Color scheme matching mobile app
private val WearDarkBlue = Color(0xFF02111D)
private val WearMediumBlue = Color(0xFF05445E)
private val WearTurquoise = Color(0xFF75E6DA)
private val WearLightBlue = Color(0xFF64B5F6)
private val WearWhite = Color(0xFFFFFFFF)
private val WearGray = Color(0xFF757575)

@Composable
fun AnimatedWeatherScreen(
    viewModel: WearableWeatherViewModel = hiltViewModel()
) {
    // Get context in composable scope
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Initialize WearableDataManager (still needed for background Data Layer service)
    LaunchedEffect(Unit) {
        try {
            WearableDataManager.initialize(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Observe ViewModel state (SINGLE SOURCE OF TRUTH)
    val weatherData by viewModel.weatherData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isPhoneConnected by viewModel.isPhoneConnected.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val dataSource by viewModel.dataSource.collectAsState()
    
    var lastRefreshTime by remember { mutableLongStateOf(0L) }
    
    // Log when data updates
    LaunchedEffect(weatherData) {
        weatherData?.let {
            Log.d("AnimatedWeatherScreen", "✓ Weather data updated: ${it.cityName}, ${it.temperature}°C (source: $dataSource)")
        }
    }
    
    // Simple refresh function - ViewModel handles all logic
    fun refreshData() {
        if (System.currentTimeMillis() - lastRefreshTime > 2000) {
            viewModel.refreshWeather()
            lastRefreshTime = System.currentTimeMillis()
        }
    }
    
    // Infinite animations
    val infiniteTransition = rememberInfiniteTransition()
    
    val bgAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val tempPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val cityPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        WearDarkBlue,
                        WearMediumBlue
                    )
                )
            )
            .drawBehind {
                // Animated background elements (rotating circles)
                for (i in 0 until 12) {
                    val angle = Math.toRadians((bgAnimation + i * 30).toDouble())
                    val radius = size.minDimension * (0.25f + (i % 3) * 0.08f)
                    val x = center.x + cos(angle) * radius
                    val y = center.y + sin(angle) * radius

                    drawCircle(
                        color = WearTurquoise.copy(alpha = 0.08f),
                        radius = size.minDimension * 0.04f,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
            .clickable { refreshData() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Connection status indicator
            ConnectionStatusIndicator(isPhoneConnected)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Weather icon with animation
            weatherData?.let { data ->
                AnimatedWeatherIcon(
                    weatherCondition = data.condition,
                    modifier = Modifier.size(60.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Temperature with pulsing animation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(tempPulse)
            ) {
                Text(
                    text = when {
                        isLoading && weatherData == null -> "..."
                        isSyncing && weatherData == null -> "..."
                        else -> weatherData?.let { "${it.temperature}${WearableConstants.Labels.TEMPERATURE_UNIT}" }
                            ?: "--°"
                    },
                    color = WearWhite,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // City name with pulsing animation
            weatherData?.let { data ->
                Text(
                    text = data.cityName.uppercase(),
                    color = WearTurquoise,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(cityPulse)
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Weather condition
            weatherData?.let { data ->
                Text(
                    text = data.condition,
                    color = WearGray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status message
            Text(
                text = when {
                    isSyncing -> stringResource(R.string.syncing_with_phone)
                    isLoading && weatherData == null -> stringResource(R.string.loading)
                    error.isNotEmpty() && weatherData == null -> error.take(25)
                    !isPhoneConnected && weatherData == null -> stringResource(R.string.tap_to_retry)
                    dataSource == "phone" -> stringResource(R.string.from_phone)
                    dataSource == "api" -> stringResource(R.string.direct_api)
                    else -> "Tap to refresh"
                },
                color = WearGray,
                fontSize = 8.sp,
                textAlign = TextAlign.Center
            )
        }
        
        // Futuristic corner elements
        FuturisticCornerElements()
    }
}

@Composable
fun ConnectionStatusIndicator(isConnected: Boolean?) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(
                if (isConnected == true) Color.Green.copy(alpha = pulse)
                else Color.Red.copy(alpha = pulse)
            )
    )
}

@Composable
fun FuturisticCornerElements() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Top-left corner
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopStart)
        ) {
            drawLine(
                color = WearTurquoise.copy(alpha = pulse * 0.5f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = WearTurquoise.copy(alpha = pulse * 0.5f),
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
        
        // Bottom-right corner
        Canvas(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.BottomEnd)
        ) {
            drawLine(
                color = WearTurquoise.copy(alpha = pulse * 0.5f),
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = WearTurquoise.copy(alpha = pulse * 0.5f),
                start = Offset(size.width, 0f),
                end = Offset(size.width, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

// Wearable-optimized animated weather icons
@Composable
fun AnimatedWeatherIcon(
    weatherCondition: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        when (weatherCondition.lowercase()) {
            "clear", "sunny" -> WearSunAnimation()
            "clouds", "cloudy" -> WearCloudAnimation()
            "rain", "drizzle" -> WearRainAnimation()
            "snow" -> WearSnowAnimation()
            "thunderstorm" -> WearThunderstormAnimation()
            else -> WearSunAnimation()
        }
    }
}

@Composable
fun WearSunAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = Modifier
            .size(60.dp)
            .drawBehind {
                // Sun core
                drawCircle(
                    color = Color(0xFFFFC107),
                    radius = size.minDimension * 0.3f,
                    center = center
                )

                // Sun rays
                for (i in 0 until 8) {
                    val angle = Math.toRadians(((rotation + i * 45) % 360).toDouble())
                    val startX = center.x + cos(angle) * size.width * 0.35f
                    val startY = center.y + sin(angle) * size.height * 0.35f
                    val endX = center.x + cos(angle) * size.width * 0.45f
                    val endY = center.y + sin(angle) * size.height * 0.45f

                    drawLine(
                        color = Color(0xFFFFC107),
                        start = Offset(startX.toFloat(), startY.toFloat()),
                        end = Offset(endX.toFloat(), endY.toFloat()),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Glow effect
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFC107).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension * 0.5f,
                    center = center
                )
            }
    )
}

@Composable
fun WearCloudAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(60.dp)
            .offset(x = offsetX.dp)
            .drawBehind {
                // Main cloud body
                drawOval(
                    color = Color.White.copy(alpha = 0.9f),
                    size = size.copy(width = size.width * 0.7f, height = size.height * 0.35f),
                    topLeft = Offset(size.width * 0.15f, size.height * 0.4f)
                )

                // Cloud puffs
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = size.minDimension * 0.18f,
                    center = Offset(size.width * 0.3f, size.height * 0.4f)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = size.minDimension * 0.22f,
                    center = Offset(size.width * 0.5f, size.height * 0.35f)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = size.minDimension * 0.18f,
                    center = Offset(size.width * 0.7f, size.height * 0.4f)
                )
            }
    )
}

@Composable
fun WearRainAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    
    Box(
        modifier = Modifier.size(60.dp)
    ) {
        // Cloud
        Box(
            modifier = Modifier
                .size(60.dp)
                .drawBehind {
                    drawOval(
                        color = Color.White.copy(alpha = 0.8f),
                        size = size.copy(width = size.width * 0.7f, height = size.height * 0.25f),
                        topLeft = Offset(size.width * 0.15f, size.height * 0.15f)
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width * 0.3f, size.height * 0.15f)
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.18f,
                        center = Offset(size.width * 0.5f, size.height * 0.12f)
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width * 0.7f, size.height * 0.15f)
                    )
                }
        )
        
        // Rain drops (3 drops for wearable)
        for (i in 0 until 3) {
            val dropDelay = i * 400
            val dropOffsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 30f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1200,
                        delayMillis = dropDelay,
                        easing = FastOutLinearInEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
            
            val dropAlpha by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1200,
                        delayMillis = dropDelay,
                        easing = FastOutLinearInEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
            
            Box(
                modifier = Modifier
                    .size(2.dp, 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(WearLightBlue.copy(alpha = dropAlpha))
                    .align(Alignment.TopCenter)
                    .offset(
                        x = (i * 12 - 12).dp,
                        y = (dropOffsetY + 20).dp
                    )
            )
        }
    }
}

@Composable
fun WearSnowAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    
    Box(
        modifier = Modifier.size(60.dp)
    ) {
        // Cloud
        Box(
            modifier = Modifier
                .size(60.dp)
                .drawBehind {
                    drawOval(
                        color = Color.White.copy(alpha = 0.8f),
                        size = size.copy(width = size.width * 0.7f, height = size.height * 0.25f),
                        topLeft = Offset(size.width * 0.15f, size.height * 0.15f)
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width * 0.3f, size.height * 0.15f)
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.18f,
                        center = Offset(size.width * 0.5f, size.height * 0.12f)
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width * 0.7f, size.height * 0.15f)
                    )
                }
        )
        
        // Snowflakes
        for (i in 0 until 3) {
            val flakeDelay = i * 400
            val flakeOffsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 30f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000,
                        delayMillis = flakeDelay,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
            
            val flakeOffsetX by infiniteTransition.animateFloat(
                initialValue = -2f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000,
                        delayMillis = flakeDelay,
                        easing = EaseInOutSine
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .align(Alignment.TopCenter)
                    .offset(
                        x = ((i * 12 - 12) + flakeOffsetX).dp,
                        y = (flakeOffsetY + 20).dp
                    )
            )
        }
    }
}

@Composable
fun WearThunderstormAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Lightning flash
    val flash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 0
                1f at 100
                0f at 300
                0.7f at 320
                0f at 500
                0f at 3000
            },
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = Modifier.size(60.dp)
    ) {
        // Dark cloud
        Box(
            modifier = Modifier
                .size(60.dp)
                .drawBehind {
                    drawOval(
                        color = Color(0xFF424242).copy(alpha = 0.8f),
                        size = size.copy(width = size.width * 0.7f, height = size.height * 0.25f),
                        topLeft = Offset(size.width * 0.15f, size.height * 0.15f)
                    )

                    drawCircle(
                        color = Color(0xFF424242).copy(alpha = 0.8f),
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width * 0.3f, size.height * 0.15f)
                    )

                    drawCircle(
                        color = Color(0xFF424242).copy(alpha = 0.8f),
                        radius = size.minDimension * 0.18f,
                        center = Offset(size.width * 0.5f, size.height * 0.12f)
                    )

                    drawCircle(
                        color = Color(0xFF424242).copy(alpha = 0.8f),
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width * 0.7f, size.height * 0.15f)
                    )
                }
        )
        
        // Lightning bolt
        Box(
            modifier = Modifier
                .size(20.dp, 30.dp)
                .align(Alignment.Center)
                .offset(y = 8.dp)
                .drawBehind {
                    val path = Path().apply {
                        moveTo(size.width * 0.5f, 0f)
                        lineTo(size.width * 0.2f, size.height * 0.5f)
                        lineTo(size.width * 0.5f, size.height * 0.5f)
                        lineTo(size.width * 0.3f, size.height)
                        lineTo(size.width * 0.8f, size.height * 0.4f)
                        lineTo(size.width * 0.5f, size.height * 0.4f)
                        close()
                    }
                    
                    drawPath(
                        path = path,
                        color = Color(0xFFFFEB3B).copy(alpha = flash * 0.9f)
                    )
                }
        )
        
        // Flash effect on screen
        if (flash > 0.7f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFFFEB3B).copy(alpha = flash * 0.1f))
            )
        }
    }
}

