package com.weather.wearable.di

import android.content.Context
import com.weather.wearable.data.api.WearableWeatherApiService
import com.weather.wearable.data.repository.WearableSettingsRepository
import com.weather.wearable.data.repository.WearableWeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WearableAppModule {

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(retrofit: Retrofit): WearableWeatherApiService {
        return retrofit.create(WearableWeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): WearableSettingsRepository {
        return WearableSettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        api: WearableWeatherApiService,
        settingsRepository: WearableSettingsRepository
    ): WearableWeatherRepository {
        return WearableWeatherRepository(api, settingsRepository)
    }

    @Provides
    @Singleton
    fun providePhoneSyncRepository(
        @ApplicationContext context: Context
    ): com.weather.wearable.data.repository.PhoneSyncRepository {
        return com.weather.wearable.data.repository.PhoneSyncRepository(context)
    }
}

