import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import java.util.Properties

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.com.google.devtools)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.secrets)
}

// Load secrets from secrets.properties and expose them via BuildConfig
val secretsProps = Properties().apply {
    val f = rootProject.file("secrets.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val WEATHER_API_KEY: String = secretsProps.getProperty("WEATHER_API_KEY", "")

android {
    namespace = "com.weather"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "com.weatherapp"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Expose secrets as BuildConfig constants
        buildConfigField("String", "WEATHER_API_KEY", "\"$WEATHER_API_KEY\"")
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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
}

dependencies {
    implementation(libs.play.services.wearable)
    testImplementation(libs.androidx.compose.ui.ui.test.junit4)
    testImplementation(libs.io.mockk)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.compose.ui.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.ui.test.manifest)

    /*Sub -> ------------------ Compose------------------*/
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.kotlin.bom))
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.androidx.activity.activity.compose)
    implementation(libs.androidx.compose.ui.ui.util)
    implementation(libs.constraintlayout.compose)

    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.android.compiler)

    /*Sub -> ------------------ Navigation------------------*/
    implementation(libs.androidx.navigation.navigation.compose)

    /*Sub -> ------------------ Material 3------------------*/
    implementation(libs.compose.icons.core)
    implementation(libs.material3)
    implementation(kotlin("reflect"))

    /*Sub -> ------------------ Observe state and livedata------------------*/
    implementation(libs.androidx.compose.runtime.runtime.livedata)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)

    /*Sub -> ------------------ Timber for logging------------------*/
    implementation(libs.com.jakewharton.timber)

    /*Sub -> ------------------Coil for image loading------------------*/
    implementation(libs.io.coil.kt.coil.compose)
    implementation(libs.io.coil.kt.coil.compose.ext)

    /*Sub -> ------------------Palette------------------*/
    implementation(libs.androidx.palette.palette.ktx)

    /*Sub -> ------------------hilt------------------*/
    implementation(libs.androidx.hilt.hilt.navigation.compose)
    implementation(libs.androidx.tools.core)

    /*Sub -> ------------------Paging------------------*/
    implementation(libs.androidx.paging.paging.compose)
    implementation(libs.androidx.paging.paging.runtime.ktx)

    /*Sub -> ------------------Retrofit2------------------*/
    implementation(libs.retrofit.runtime)
    implementation(libs.retrofit.adapter.rx.java2)
    implementation(libs.retrofit.mock)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.log.interceptor)

    /*Sub -> ------------------kotlinx-coroutines------------------*/
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.android)

    /*Sub -> ------------------facebook------------------*/
    implementation(libs.fb.stetho)
    implementation(libs.fb.stetho.okhttp3)

    /*Sub -> ------------------Security------------------*/
    implementation(libs.android.security)

    /*Sub -> ------------------Wearable Data Layer------------------*/
    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.play.services)
}