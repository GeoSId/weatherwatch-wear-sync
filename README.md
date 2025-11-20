# WeatherWatch

WeatherWatch delivers the **official roadmap goal** for this repository: a paired Android + Wear OS experience that keeps weather data synchronized over the Wear OS Data Layer while preserving a fully independent fallback on the watch. All content below is sourced from the authoritative docs in this repo (`ARCHITECTURE.md`, `IMPLEMENTATION_SUMMARY.md`, `CONSOLIDATION_SUMMARY.md`, `QUICK_REFERENCE.md`, `UNIFIED_VIEWMODEL_ARCHITECTURE.md`, `SIMPLIFIED_DATA_MANAGER.md`, `MVVM_ARCHITECTURE.md`, `MVVM_DIAGRAM.md`). Review those files before changing behavior—they define the requirements that supersede legacy code or filenames.

---

## Purpose & Core Requirements
- Provide real-time current weather, five-day forecasts, and AI insights on the phone while surfacing **glanceable**, battery-friendly conditions on Wear OS.
- Keep **identical package names** across modules to satisfy the Data Layer requirement outlined in `ARCHITECTURE.md#package-name-requirement`.
- Maintain phone-first sync via `WearableDataManager` + `PhoneSyncRepository`, with an automatic fallback to direct API calls handled inside `WearableWeatherViewModel`.
- Persist user configuration (preferred city, API key overrides, building profiles) through repositories + `SharedPreferences` wrappers—never hardcode UI strings or labels; instead, source them from general constants such as `wearable/src/main/java/com/weather/wearable/constants/WearableConstants.kt` (`Labels` replaces the old “LabelTitles” guidance).
- Securely store API keys via `secrets.properties`, mirroring the steps in `IMPLEMENTATION_SUMMARY.md` and `QUICK_REFERENCE.md`.

---

## Architecture at a Glance
- **Pattern:** Consistent MVVM on both modules with Repository → Resource → StateFlow pipelines. ViewModels are the **single source of truth** per `UNIFIED_VIEWMODEL_ARCHITECTURE.md`.
- **DI:** Hilt everywhere (`@HiltAndroidApp`, `@AndroidEntryPoint`, scoped modules for repositories and sync helpers).
- **Networking:** Retrofit + OkHttp targeting `https://api.openweathermap.org/data/2.5/` with `GET /weather` on both modules and `GET /forecast` on mobile.
- **Sync:** Wear OS Data Layer (foreground `DataLayerListenerService`, `PhoneSyncRepository`, `WearableDataManager`) backed by message + data clients. Automatic timeout fallback is implemented in `WearableWeatherViewModel`.
- **State:** Compose + StateFlow-driven UI. Wearable UI observes only the ViewModel after the consolidation effort documented in `CONSOLIDATION_SUMMARY.md`.

---

## Modules & Responsibilities
| Module | Responsibilities | Key Files |
| --- | --- | --- |
| `app` | Search, forecasting, AI insights, building profiles, preferences, phone ↔ watch sync trigger | `WeatherViewModel`, `WeatherRepository`, `WearableDataSyncService`, `WeatherDataCache` |
| `wearable` | Animated Compose UI, Data Layer listener, smart fallback ViewModel, constants enforcing no hardcoded labels | `WearableWeatherViewModel`, `PhoneSyncRepository`, `WearableDataManager`, `AnimatedWeatherScreen`, `WearableConstants` |

For UI-specific guidance on the watch module, see `wearable/README.md`.

---

## Data Lifecycles
```
Mobile: User Action → Compose UI → WeatherViewModel → WeatherRepository → Retrofit/OkHttp → OpenWeatherMap
                                               ↓
                                         Resource<T>
                                               ↓
                                         StateFlow → UI + WearableDataSyncService
```

```
Wearable Refresh:
1. ViewModel.refreshWeather()
2. Phone connected? PhoneSyncRepository.requestWeatherSync()
3. DataLayerListenerService pushes payload → WearableDataManager → ViewModel (dataSource = "phone")
4. Timeout or offline? ViewModel fetches directly via WearableWeatherRepository (dataSource = "api")
```

Both flows—including connection-state tracking, sync indicators, and fallback timing—are diagrammed in `ARCHITECTURE.md` and `UNIFIED_VIEWMODEL_ARCHITECTURE.md`.

---

## Compliance Checklist
- `Package Name Parity` (documented requirement): keep `com.weather...` namespaces identical across modules before building or deploying.
- `No Hardcoded Labels`: verify new UI strings exist in the constants files (`WearableConstants.Labels`, shared resources in the mobile module) instead of inline literals.
- `Secrets Handling`: `secrets.properties` must define `WEATHER_API_KEY=<value>`; repositories fetch keys via their Settings repositories rather than direct BuildConfig access.
- `Foreground Services`: Wear services using the Data Layer must request and display the required notification (`android.permission.FOREGROUND_SERVICE_DATA_SYNC` already declared; do not remove).
- `State Ownership`: UI must observe StateFlow outputs from ViewModels only—business logic belongs in the ViewModel or repository layers per `MVVM_ARCHITECTURE.md`.

---

## Setup & Build
1. Create/update `/Users/terminal43/AndroidStudioProjects/WearOs/secrets.properties`:
   ```
   WEATHER_API_KEY=your_openweathermap_api_key
   ```
2. Sync Gradle in Android Studio or run:
   ```
   ./gradlew :app:assembleDebug
   ./gradlew :wearable:assembleDebug
   ```
3. Install artifacts on paired devices/emulators. The wearable APK lives at `wearable/build/outputs/apk/debug/wearable-debug.apk` (also documented in `wearable/README.md`).
4. Launch the phone app, search for a city, confirm sync notifications, then open the watch app to validate the connection indicator, sync status label, and fallback behavior.

---


Consult these documents (the “roadmap”) before modifying or extending functionality; they capture the authoritative requirements for WeatherWatch.
