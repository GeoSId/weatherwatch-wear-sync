package com.weather.data.prediction

import com.weather.domain.model.ForecastResponse
import com.weather.domain.model.WeatherPrediction
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Weather prediction service that uses simple statistical models to
 * simulate AI-based weather predictions.
 * 
 * This service demonstrates how historical weather data can be analyzed
 * to make predictions about future weather patterns using statistical methods
 * that are similar to those used in actual machine learning models.
 */
class WeatherPredictionService @Inject constructor() {

    /**
     * Generate AI-based weather predictions using the forecast data.
     * 
     * A real implementation would use a TensorFlow Lite model or call an external
     * ML service, but this simulates predictions with statistical methods.
     * 
     * The prediction algorithm works as follows:
     * 1. Analyze the existing forecast data to determine baseline statistics
     * 2. Calculate trends using linear regression on temperature, wind, and humidity
     * 3. Project these trends forward with controlled randomness
     * 4. Adjust predictions based on meteorological principles (e.g., high humidity → rain)
     * 5. Calculate confidence levels based on prediction distance
     * 
     * @param forecastData The forecast data to base predictions on
     * @return A list of weather predictions for 7 days beyond the forecast
     */
    fun generatePredictions(forecastData: ForecastResponse): List<WeatherPrediction> {
        val predictions = mutableListOf<WeatherPrediction>()
        val forecastList = forecastData.list
        
        if (forecastList.isEmpty()) return emptyList()
        
        // Get the latest data point to use as our starting point for predictions
        val lastForecast = forecastList.last()
        
        // Calculate some baseline statistics from existing forecast
        // These serve as the foundation for our predictive model
        val temps = forecastList.map { it.main.temp }
        val avgTemp = temps.average().toFloat()
        val tempVariance = calculateVariance(temps, avgTemp) // How much temperature fluctuates
        
        val windSpeeds = forecastList.map { it.wind.speed }
        val avgWindSpeed = windSpeeds.average().toFloat()
        val windVariance = calculateVariance(windSpeeds, avgWindSpeed)
        
        val humidities = forecastList.map { it.main.humidity }
        val avgHumidity = humidities.average().toInt()
        
        // Get weather conditions distribution - this helps predict future conditions
        // based on their frequency in the forecast period
        val conditions = forecastList.mapNotNull { it.weather.firstOrNull()?.main }
        val conditionDistribution = conditions
            .groupingBy { it }
            .eachCount()
            .mapValues { it.value.toFloat() / conditions.size }
        
        // Determine trend patterns using linear regression
        // This tells us if temperatures, wind speeds, etc. are generally 
        // increasing, decreasing, or staying stable
        val tempTrend = determineTrend(temps)
        val windTrend = determineTrend(windSpeeds)
        val humidityTrend = determineTrend(humidities.map { it.toFloat() })
        
        // Generate predictions for 7 days beyond the forecast
        val calendar = Calendar.getInstance().apply {
            time = Date(lastForecast.dt * 1000)
        }
        
        for (day in 1..7) {
            // Add 24 hours to get next day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val predictionTime = calendar.time.time / 1000
            
            // Apply trends with some randomness for realistic predictions
            // The further out we predict, the more the trend influences the result
            // We multiply by day to increase effect over time
            val tempModifier = tempTrend * day * 0.5f + Random.nextFloat() * tempVariance
            val windModifier = windTrend * day * 0.3f + Random.nextFloat() * windVariance
            val humidityModifier = (humidityTrend * day * 3f).toInt()
            
            // Constrain predictions to reasonable values
            // We don't want extreme jumps - real weather typically changes gradually
            val predictedTemp = max(min(avgTemp + tempModifier, avgTemp + 10), avgTemp - 5)
            val predictedWind = max(avgWindSpeed + windModifier, 0f) // Wind can't be negative
            val predictedHumidity = max(min(avgHumidity + humidityModifier, 100), 0) // 0-100%
            
            // Predict weather condition based on distribution and meteorological factors
            val predictedCondition = predictWeatherCondition(
                conditionDistribution, 
                predictedTemp,
                predictedHumidity
            )
            
            predictions.add(
                WeatherPrediction(
                    dt = predictionTime,
                    temp = predictedTemp,
                    condition = predictedCondition,
                    humidity = predictedHumidity,
                    windSpeed = predictedWind,
                    confidence = calculateConfidence(day) // Confidence decreases with time
                )
            )
        }
        
        return predictions
    }
    
    /**
     * Calculates the statistical variance of a set of values.
     * 
     * Variance measures how far each value in the dataset is from the mean.
     * A high variance indicates more volatile weather patterns.
     * 
     * @param values List of measurements (temperature, wind speed, etc.)
     * @param mean The average of those measurements
     * @return The variance as a float
     */
    private fun calculateVariance(values: List<Float>, mean: Float): Float {
        if (values.isEmpty()) return 0f
        return values.map { (it - mean) * (it - mean) }.sum() / values.size
    }
    
    /**
     * Performs a simple linear regression to determine the trend in a series of values.
     * 
     * This is a core algorithm for our prediction model - it determines whether a
     * measurement (like temperature) is trending upward, downward, or stable.
     * 
     * The formula used is the standard linear regression slope calculation:
     * slope = (n∑xy - ∑x∑y) / (n∑x² - (∑x)²)
     * 
     * @param values List of measurements over time
     * @return The slope/trend: positive = increasing, negative = decreasing, near-zero = stable
     */
    private fun determineTrend(values: List<Float>): Float {
        if (values.size < 2) return 0f
        
        // Simple linear regression for trend
        var sumX = 0f
        var sumY = 0f
        var sumXY = 0f
        var sumX2 = 0f
        
        for (i in values.indices) {
            sumX += i
            sumY += values[i]
            sumXY += i * values[i]
            sumX2 += i * i
        }
        
        val n = values.size
        val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        return slope
    }
    
    /**
     * Predicts a weather condition based on historical distribution and current meteorological factors.
     * 
     * This implements simplified meteorological rules:
     * - High humidity + high temperature → thunderstorms
     * - High humidity + medium temperature → rain
     * - High humidity + low temperature → drizzle
     * - Very hot → clear or partly cloudy
     * - Below freezing → snow
     * - Otherwise → probabilistic selection based on historical distribution
     * 
     * @param distribution Historical distribution of weather conditions
     * @param temp Predicted temperature
     * @param humidity Predicted humidity
     * @return The predicted weather condition (e.g., "Clear", "Rain", "Snow")
     */
    private fun predictWeatherCondition(
        distribution: Map<String, Float>,
        temp: Float,
        humidity: Int
    ): String {
        // Base prediction on distribution but adjust for temperature and humidity
        
        // Higher chance of rain/storms with high humidity
        if (humidity > 85) {
            if (temp > 25) return "Thunderstorm"
            if (temp > 15) return "Rain"
            return "Drizzle"
        }
        
        // Very high temperatures likely mean clear or clouds
        if (temp > 30) {
            return if (Random.nextFloat() > 0.3f) "Clear" else "Clouds"
        }
        
        // Very cold temperatures might mean snow
        if (temp < 0) {
            return "Snow"
        }
        
        // Otherwise select from distribution with some randomness
        // This is a technique called "weighted random selection" based on prior probabilities
        val rand = Random.nextFloat()
        var cumulative = 0f
        
        distribution.forEach { (condition, probability) ->
            cumulative += probability
            if (rand <= cumulative) {
                return condition
            }
        }
        
        // Fallback to most common condition
        return distribution.entries.maxByOrNull { it.value }?.key ?: "Clouds"
    }
    
    /**
     * Calculates the confidence level for a prediction based on how far in the future it is.
     * 
     * In real AI weather models, confidence typically decreases over time as uncertainty increases.
     * This simulates that pattern by reducing confidence by 10% for each day into the future.
     * 
     * @param daysAhead How many days ahead the prediction is for
     * @return A confidence value between 0.4 (40%) and 0.9 (90%)
     */
    private fun calculateConfidence(daysAhead: Int): Float {
        // Confidence decreases the further we predict
        return max(0.9f - (daysAhead * 0.1f), 0.4f)
    }
} 