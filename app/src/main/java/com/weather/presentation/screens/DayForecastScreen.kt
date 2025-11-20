package com.weather.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weather.R
import com.weather.domain.model.WeatherData
import com.weather.presentation.components.AnimatedWeatherIcon
import com.weather.presentation.components.WeatherDataRow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun DayForecastScreen(
    weatherData: WeatherData,
    sunriseTime: Long,
    sunsetTime: Long,
    onBackClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = Date(weatherData.dt * 1000)
    
    val scrollState = rememberScrollState()
    
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top app bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated back button
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
                    onClick = onBackClick,
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Text(
                    text = stringResource(R.string.day_forecast),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                // Empty box for alignment
                Box(modifier = Modifier.width(48.dp))
            }
            
            // Date display
            Text(
                text = dateFormat.format(date).uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF75E6DA),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            
            // Main weather card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0E1621).copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                                weatherCondition = weatherData.weather.firstOrNull()?.main ?: stringResource(R.string.unknown),
                                modifier = Modifier.size(80.dp)
                            )
                            
                            Text(
                                text = weatherData.weather.firstOrNull()?.main ?:stringResource(R.string.unknown),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF75E6DA)
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${weatherData.main.temp.toInt()}°C",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                            
                            Text(
                                text = stringResource(
                                    R.string.feels_like_c,
                                    weatherData.main.feels_like.toInt()
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Detailed weather data
                    WeatherDataRow(
                        label = stringResource(R.string.time),
                        value = timeFormat.format(date)
                    )
                    
                    WeatherDataRow(
                        label = stringResource(R.string.humidity),
                        value = "${weatherData.main.humidity}%"
                    )
                    
                    WeatherDataRow(
                        label = stringResource(R.string.wind_speed),
                        value = "${weatherData.wind.speed} m/s"
                    )
                    
                    WeatherDataRow(
                        label = stringResource(R.string.wind_direction),
                        value = "${weatherData.wind.deg}°"
                    )
                    
                    WeatherDataRow(
                        label = stringResource(R.string.pressure),
                        value = "${weatherData.main.pressure} hPa"
                    )
                    
                    if (weatherData.pop != null) {
                        WeatherDataRow(
                            label = stringResource(R.string.chance_of_rain),
                            value = "${(weatherData.pop * 100).toInt()}%"
                        )
                    }
                    
                    WeatherDataRow(
                        label = stringResource(R.string.visibility),
                        value = "${weatherData.visibility / 1000} km"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sun animation card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0E1621).copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Sunrise/Sunset text
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                R.string.sunrise,
                                timeFormat.format(Date(sunriseTime * 1000))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        
                        Text(
                            text = stringResource(
                                R.string.sunset,
                                timeFormat.format(Date(sunsetTime * 1000))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Sun journey animation
                    SunJourneyAnimation(
                        weatherTime = weatherData.dt,
                        sunriseTime = sunriseTime,
                        sunsetTime = sunsetTime,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun SunJourneyAnimation(
    weatherTime: Long,
    sunriseTime: Long,
    sunsetTime: Long,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Calculate the progress of the sun's journey (0.0 to 1.0)
    // 0.0 = sunrise, 0.5 = noon, 1.0 = sunset
    val dayDuration = sunsetTime - sunriseTime
    var progress = if (dayDuration > 0) {
        ((weatherTime - sunriseTime).toFloat() / dayDuration).coerceIn(0f, 1f)
    } else {
        0.5f // Default to noon if we don't have valid sunrise/sunset times
    }
    
    // Handle cases where weatherTime is before sunrise or after sunset
    progress = when {
        weatherTime < sunriseTime -> 0f
        weatherTime > sunsetTime -> 1f
        else -> progress
    }
    
    // Animate the sun's position - only animate if we're before sunset
    val animatedProgress by if (progress < 1f) {
        infiniteTransition.animateFloat(
            initialValue = progress,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = maxOf(1, (30000 * (1 - progress)).toInt()),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }
    
    val sunGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Use regular progress if at sunset, otherwise use animated progress
    val activeProgress = if (progress >= 1f) progress else animatedProgress
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val horizonY = height * 0.7f
        
        // Draw horizon line
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(0f, horizonY),
            end = Offset(width, horizonY),
            strokeWidth = 2.dp.toPx()
        )
        
        // Draw sun path arc
        val pathRadius = height * 0.5f

        // Draw arc path
        val arcPath = Path().apply {
            moveTo(0f, horizonY)
            val rect = Rect(
                left = width / 2 - pathRadius,
                top = horizonY,
                right = width / 2 + pathRadius,
                bottom = horizonY + pathRadius * 2
            )
            arcTo(
                rect = rect,
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
        }
        
        drawPath(
            path = arcPath,
            color = Color.White.copy(alpha = 0.15f),
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Calculate sun position on the arc
        val angle = PI * activeProgress
        val sunX = width / 2 - pathRadius * cos(angle).toFloat()
        val sunY = horizonY - pathRadius * sin(angle).toFloat()
        
        // Draw sun
        val sunRadius = 15.dp.toPx() * sunGlow
        
        // Draw sun glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFC107).copy(alpha = 0.5f * sunGlow),
                    Color.Transparent
                ),
                center = Offset(sunX, sunY),
                radius = sunRadius * 2
            ),
            radius = sunRadius * 2,
            center = Offset(sunX, sunY)
        )
        
        // Draw sun circle
        drawCircle(
            color = Color(0xFFFFC107),
            radius = sunRadius,
            center = Offset(sunX, sunY)
        )
        
        // Draw sun rays
        for (i in 0 until 8) {
            val rayAngle = i * PI / 4
            val startX = sunX + (sunRadius * 1.2f * cos(rayAngle)).toFloat()
            val startY = sunY + (sunRadius * 1.2f * sin(rayAngle)).toFloat()
            val endX = sunX + (sunRadius * 1.8f * cos(rayAngle)).toFloat()
            val endY = sunY + (sunRadius * 1.8f * sin(rayAngle)).toFloat()
            
            drawLine(
                color = Color(0xFFFFC107).copy(alpha = 0.7f * sunGlow),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )
        }
        
        // Draw time markers
        val markerPoints = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)

        markerPoints.forEachIndexed { index, point ->
            val markerAngle = PI * point
            val markerX = width / 2 - pathRadius * cos(markerAngle).toFloat()
            val markerY = horizonY - pathRadius * sin(markerAngle).toFloat()
            
            // Draw marker dot
            drawCircle(
                color = if (activeProgress >= point) Color(0xFF75E6DA) else Color.White.copy(alpha = 0.3f),
                radius = 4.dp.toPx(),
                center = Offset(markerX, markerY)
            )
            
            // Draw time text (simplified - in real implementation you'd use drawIntoCanvas)
            if (point == 0f || point == 0.5f || point == 1f) {
                drawCircle(
                    color = if (activeProgress >= point) Color(0xFF75E6DA) else Color.White.copy(alpha = 0.3f),
                    radius = 2.dp.toPx(),
                    center = Offset(markerX, markerY + 15.dp.toPx())
                )
            }
        }
    }
} 