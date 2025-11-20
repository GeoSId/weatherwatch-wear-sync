import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import java.util.Properties

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.com.google.devtools)
}

android {
    namespace = "com.weather.wearable"
    compileSdk = 36

    // Load secrets from root secrets.properties
    val secretsProps = Properties().apply {
        val secretsFile = rootProject.file("secrets.properties")
        if (secretsFile.exists()) {
            secretsFile.inputStream().use { load(it) }
        }
    }
    val weatherApiKey = (secretsProps.getProperty("WEATHER_API_KEY") ?: "")

    defaultConfig {
        applicationId = "com.weatherapp"  // MUST match phone app for Data Layer sync!
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            // Provide the fallback API key in debug builds only
            buildConfigField("String", "FALLBACK_API_KEY", "\"$weatherApiKey\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Do not ship the fallback API key in release builds
            buildConfigField("String", "FALLBACK_API_KEY", "\"\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.8"
    }
}

dependencies {
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.espresso.espresso.core)
    
    // Wear OS Core
    implementation(libs.androidx.wear)
    
    // Wear Compose
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.ui.tooling)
    implementation(libs.androidx.wear.compose.navigation)

    /*Sub -> ------------------ Material 3------------------*/
    implementation(libs.material3)
    implementation(kotlin("reflect"))

    // Activity and Lifecycle
    implementation(libs.androidx.activity.activity.compose)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.lifecycle.viewmodel.ktx)
    
    // Coroutines
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services) {
        exclude(group = "com.google.protobuf")
    }
    
    // Health Services for sensors
    implementation(libs.androidx.health.health.services.client)
    
    // Tiles
    implementation(libs.androidx.wear.tiles) {
        exclude(group = "com.google.protobuf")
    }
    implementation(libs.androidx.wear.tiles.material) {
        exclude(group = "com.google.protobuf")
    }
    
    // Complications
    implementation(libs.androidx.wear.watchface.complications.data.source) {
        exclude(group = "com.google.protobuf")
    }
    
    // Only use protobuf-javalite
    implementation(libs.com.google.protobuf.javalite)
    
    // Add Gson for JSON serialization
    implementation(libs.gson)
    
    // Networking dependencies for fallback API calls (optional)
    implementation(libs.retrofit.runtime)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.log.interceptor)
    implementation(libs.play.services.wearable)
    
    // Hilt Dependency Injection
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.android.compiler)
    implementation(libs.androidx.hilt.hilt.navigation.compose)
}