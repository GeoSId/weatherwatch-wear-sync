package com.weather.di

import com.weather.data.api.WeatherApiService
import com.weather.data.prediction.WeatherPredictionService
import com.weather.data.repository.SettingsRepository
import com.weather.data.repository.WeatherRepository
import com.weather.data.wearable.WearableDataSyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(okHttpClient: OkHttpClient): WeatherApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherPredictionService(): WeatherPredictionService {
        return WeatherPredictionService()
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        api: WeatherApiService, 
        settingsRepository: SettingsRepository,
        predictionService: WeatherPredictionService
    ): WeatherRepository {
        return WeatherRepository(api, settingsRepository, predictionService)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideWearableDataSyncService(@ApplicationContext context: Context): WearableDataSyncService {
        return WearableDataSyncService(context)
    }
} 