package com.weather.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FuturisticWeatherCard(
    modifier: Modifier = Modifier,
    temperature: String,
    weatherCondition: String,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = modifier
            .scale(glowScale)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0E1621).copy(alpha = 0.85f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Draw futuristic background elements
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00B4DB).copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.2f, size.height * 0.2f),
                                radius = size.width * 0.5f
                            ),
                            radius = size.width * 0.4f,
                            center = Offset(size.width * 0.7f, size.height * 0.3f)
                        )
                        
                        // Rotating hexagonal grid
                        val hexSize = size.minDimension * 0.15f
                        for (i in 0 until 6) {
                            val angle = Math.toRadians((rotation + i * 60).toDouble())
                            val x = center.x + cos(angle) * size.width * 0.3f
                            val y = center.y + sin(angle) * size.height * 0.3f
                            
                            val path = Path().apply {
                                for (j in 0 until 6) {
                                    val hexAngle = Math.toRadians((j * 60).toDouble())
                                    val hx = x.toFloat() + cos(hexAngle) * hexSize
                                    val hy = y.toFloat() + sin(hexAngle) * hexSize
                                    if (j == 0) moveTo(hx.toFloat(), hy.toFloat()) else lineTo(hx.toFloat(),
                                        hy.toFloat()
                                    )
                                }
                                close()
                            }
                            
                            drawPath(
                                path = path,
                                color = Color(0xFF00B4DB).copy(alpha = 0.2f),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = temperature,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = weatherCondition,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF64B5F6)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Custom content
                    content()
                }
            }
        }
        
        // Pulsating glow effect
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .blur(20.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00B4DB).copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.5f,
                center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
        }
    }
}

@Composable
fun WeatherDataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

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
        modifier = modifier
            .size(80.dp)
            .scale(scale)
    ) {
        when (weatherCondition.lowercase()) {
            "clear", "sunny" -> SunAnimation()
            "clouds", "cloudy" -> CloudAnimation()
            "rain", "drizzle" -> RainAnimation()
            "snow" -> SnowAnimation()
            "thunderstorm" -> ThunderstormAnimation()
            else -> SunAnimation()  // Default
        }
    }
}

@Composable
fun SunAnimation() {
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
            .size(80.dp)
            .drawBehind {
                // Sun core
                drawCircle(
                    color = Color(0xFFFFC107),
                    radius = size.minDimension * 0.25f,
                    center = center
                )
                
                // Sun rays
                for (i in 0 until 12) {
                    val angle = Math.toRadians(((rotation + i * 30) % 360).toDouble())
                    val startX = center.x + cos(angle) * size.width * 0.3f
                    val startY = center.y + sin(angle) * size.height * 0.3f
                    val endX = center.x + cos(angle) * size.width * 0.4f
                    val endY = center.y + sin(angle) * size.height * 0.4f
                    
                    drawLine(
                        color = Color(0xFFFFC107),
                        start = Offset(startX.toFloat(), startY.toFloat()),
                        end = Offset(endX.toFloat(), endY.toFloat()),
                        strokeWidth = 3.dp.toPx()
                    )
                }
                
                // Glow effect
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFC107).copy(alpha = 0.5f),
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
fun CloudAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .offset(x = offsetX.dp)
            .drawBehind {
                // Main cloud
                drawOval(
                    color = Color.White.copy(alpha = 0.8f),
                    size = size.copy(width = size.width * 0.8f, height = size.height * 0.4f),
                    topLeft = Offset(size.width * 0.1f, size.height * 0.4f)
                )
                
                // Cloud parts
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = size.minDimension * 0.15f,
                    center = Offset(size.width * 0.25f, size.height * 0.4f)
                )
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = size.minDimension * 0.2f,
                    center = Offset(size.width * 0.5f, size.height * 0.35f)
                )
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = size.minDimension * 0.15f,
                    center = Offset(size.width * 0.75f, size.height * 0.4f)
                )
            }
    )
}

@Composable
fun RainAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Cloud animation
    val cloudOffsetX by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier.size(80.dp)
    ) {
        // Cloud
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = cloudOffsetX.dp)
                .drawBehind {
                    drawOval(
                        color = Color.White.copy(alpha = 0.8f),
                        size = size.copy(width = size.width * 0.8f, height = size.height * 0.3f),
                        topLeft = Offset(size.width * 0.1f, size.height * 0.2f)
                    )
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.12f,
                        center = Offset(size.width * 0.25f, size.height * 0.2f)
                    )
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width * 0.5f, size.height * 0.15f)
                    )
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.12f,
                        center = Offset(size.width * 0.75f, size.height * 0.2f)
                    )
                }
        )
        
        // Rain drops
        for (i in 0 until 5) {
            val dropDelay = i * 300
            val dropOffsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 40f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1500,
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
                        durationMillis = 1500,
                        delayMillis = dropDelay,
                        easing = FastOutLinearInEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
            
            Box(
                modifier = Modifier
                    .size(3.dp, 10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF64B5F6).copy(alpha = dropAlpha))
                    .align(Alignment.TopCenter)
                    .offset(
                        x = (i * 15 - 30).dp,
                        y = (dropOffsetY + 30).dp
                    )
            )
        }
    }
}

@Composable
fun SnowAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    
    Box(
        modifier = Modifier.size(80.dp)
    ) {
        // Cloud
        Box(
            modifier = Modifier
                .size(80.dp)
                .drawBehind {
                    drawOval(
                        color = Color.White.copy(alpha = 0.8f),
                        size = size.copy(width = size.width * 0.8f, height = size.height * 0.3f),
                        topLeft = Offset(size.width * 0.1f, size.height * 0.2f)
                    )
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.12f,
                        center = Offset(size.width * 0.25f, size.height * 0.2f)
                    )
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width * 0.5f, size.height * 0.15f)
                    )
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension * 0.12f,
                        center = Offset(size.width * 0.75f, size.height * 0.2f)
                    )
                }
        )
        
        // Snowflakes
        for (i in 0 until 5) {
            val flakeDelay = i * 300
            val flakeOffsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 40f,
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
                initialValue = -3f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000,
                        delayMillis = flakeDelay,
                        easing = EaseInOutSine
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            val flakeAlpha by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000,
                        delayMillis = flakeDelay,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = flakeAlpha))
                    .align(Alignment.TopCenter)
                    .offset(
                        x = ((i * 15 - 30) + flakeOffsetX).dp,
                        y = (flakeOffsetY + 30).dp
                    )
            )
        }
    }
}

@Composable
fun ThunderstormAnimation() {
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
        modifier = Modifier.size(80.dp)
    ) {
        // Cloud
        Box(
            modifier = Modifier
                .size(80.dp)
                .drawBehind {
                    drawOval(
                        color = Color(0xFF424242).copy(alpha = 0.8f),
                        size = size.copy(width = size.width * 0.8f, height = size.height * 0.3f),
                        topLeft = Offset(size.width * 0.1f, size.height * 0.2f)
                    )
                    
                    drawCircle(
                        color = Color(0xFF424242).copy(alpha = 0.8f),
                        radius = size.minDimension * 0.12f,
                        center = Offset(size.width * 0.25f, size.height * 0.2f)
                    )
                    
                    drawCircle(
                        color = Color(0xFF424242).copy(alpha = 0.8f),
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width * 0.5f, size.height * 0.15f)
                    )
                    
                    drawCircle(
                        color = Color(0xFF424242).copy(alpha = 0.8f),
                        radius = size.minDimension * 0.12f,
                        center = Offset(size.width * 0.75f, size.height * 0.2f)
                    )
                }
        )
        
        // Lightning bolt
        Box(
            modifier = Modifier
                .size(30.dp, 40.dp)
                .align(Alignment.Center)
                .offset(y = 10.dp)
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
                        color = Color(0xFFFFEB3B).copy(alpha = 0.5f + (flash * 0.5f)),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    
                    drawPath(
                        path = path,
                        color = Color(0xFFFFEB3B).copy(alpha = flash * 0.8f)
                    )
                }
        )
        
        // Flash effect
        if (flash > 0.7f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFFFEB3B).copy(alpha = flash * 0.15f))
            )
        }
    }
} 