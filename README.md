#  WeatherWatch – Android ↔ Wear OS Bidirectional Sync Demo

WeatherWatch is a dual-module Android project designed to **demonstrate, debug, and validate real-time two-way communication** between an Android phone and a Wear OS device.  
Built using **Jetpack Compose, MVVM, Hilt, Retrofit, and the Wear OS Data Layer**, it simulates a production-ready architecture in a compact, testable environment.

---

## Project Purpose

WeatherWatch was created to test and showcase:

- **Phone → Watch pushes** (sending updated weather data)
- **Watch → Phone pulls** (requesting data on demand)
- **Automatic fallback** to direct API calls on the watch if the phone is unreachable
- **Connection recovery** when communication returns
- **Visual debugging indicators** on the watch UI
- Clean, modular, production-style Android architecture

---

##  Key Features

###  True Bidirectional Sync
- Watch sends sync requests or new city selections.
- Phone responds with full weather payloads + metadata.
- All communication over Wear OS Data Layer (`DataClient` + `MessageClient`).

###  Smart Fallback System
If the phone is unreachable:
- The watch performs a direct OpenWeatherMap API call.
- UI shows **“Direct API”** badge.
- Once the connection returns, syncing switches back to **“From phone ✓”** automatically.

###  Debug-Friendly UI
- Connection indicators
- Sync animation states
- Badges showing data origin
- Shared constant-driven labels (no hardcoded UI strings)

### Modern Architecture
- Shared package name for correct message routing
- Clean MVVM separation for both modules
- Compose-first user interfaces
- Kotlin everywhere

---

## Architecture Overview

| Layer | Phone (`app`) | Wear OS (`wearable`) |
|-------|----------------|-----------------------|
| **UI** | Search, forecast, AI insights (Compose) | Animated glanceable weather dial |
| **ViewModel** | `WeatherViewModel` | `WearableWeatherViewModel` |
| **Data & Sync** | `WeatherRepository`, `WeatherDataCache`, `WearableDataSyncService` | `WearableWeatherRepository`, `PhoneSyncRepository`, `WearableDataManager` |
| **Transport** | Retrofit + OkHttp | Retrofit + Wear OS Data Layer |

---

Both modules share the same base package name to ensure the Data Layer routes messages correctly.

---

## Data Flows You Can Test
```
Phone-initiated:
User search → WeatherViewModel → WeatherRepository → OpenWeatherMap
        ↓
WearableDataSyncService → Wear OS Data Layer → WearableDataManager → WearableWeatherViewModel → Watch UI
```

```
Watch-initiated:
Refresh gesture → WearableWeatherViewModel
    ↓
PhoneSyncRepository checks connection
    ├─ Connected → Send /sync_request → phone responds → UI shows “From phone ✓”
    └─ Offline/timeout → WearableWeatherRepository calls API → UI shows “Direct API”
```

---

## Setup Instructions
1. **API Key**  
   Create or edit `/Users/terminal43/AndroidStudioProjects/WearOs/secrets.properties` with:
   ```
   WEATHER_API_KEY=your_openweathermap_api_key
   ```
2. **Build**
   ```
   ./gradlew :app:assembleDebug
   ./gradlew :wearable:assembleDebug
   ```
3. **Install**
    - Phone APK: `app/build/outputs/apk/debug/app-debug.apk`
    - Wear APK: `wearable/build/outputs/apk/debug/wearable-debug.apk`
4. **Run Tests**
    - Launch the phone app, search for any city, and keep the foreground service notification visible.
    - Open the watch app, observe the connection dot, and tap to refresh.
    - Toggle connectivity (Airplane mode, Bluetooth off, Wi‑Fi off) to ensure the watch falls back to direct API calls, then restore connectivity to verify recovery.

---

## Validation Checklist
- Watch reflects the same city/temperature the phone sends.
- Sync status switches between “phone” and “API” based on availability.
- Offline fallback occurs within the timeout window (~5 seconds) and recovers automatically.
- All strings, notification titles, and Wear UI labels come from shared constants (no hardcoded literals).
- Foreground services remain active while testing to prevent the OS from killing sync processes.

---

## Troubleshooting
- **No data on the watch:** Confirm both modules share identical package names and that the phone foreground service notification is present.
- **Fallback never triggers:** Disable all radios on the phone; the watch should switch to direct API calls. If not, inspect the `PhoneSyncRepository` logs.
- **API failures:** Re-check the API key in `secrets.properties`, rebuild, and ensure the device/emulator has network access.
- **Slow message delivery:** Use Logcat tags `WearableDataSyncService`, `PhoneSyncRepository`, and `WearableWeatherViewModel` to trace each hop.

---

## Usage Philosophy
Treat WeatherWatch as a **laboratory for bidirectional sync**. The focus is stability, observability, and recovery—not shipping features. Collect metrics, reproduce edge cases, and iterate on the message contracts here before promoting changes into production-grade mobile and Wear OS apps.***
