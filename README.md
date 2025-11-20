# WeatherWatch

WeatherWatch is a cross-device weather intelligence platform that pairs an Android handset app with a Wear OS companion. The mobile experience handles rich forecasts, AI-generated insights, building profiles, and preference management, while the watch delivers glanceable conditions synced over the Wear OS Data Layer with a smart fallback to direct API calls when the phone is unavailable.

---

## Core Outcomes
- Present accurate current weather, multi-day forecasts, and AI insights using OpenWeatherMap data.
- Maintain Building Profiles for facilities/locations to contextualize forecasts.
- Keep API credentials, preferences, and last-known city in user-managed settings.
- Mirror priority weather info to Wear OS with resilient sync + standalone fetch capability.

---

## Architecture Overview
- **Pattern:** End-to-end MVVM with Repository, Resource wrappers, and StateFlow-driven Compose UI.
- **Dependency Injection:** Hilt on both modules, including wearable ViewModels via `@HiltViewModel`.
- **Networking:** Retrofit + OkHttp targeting `https://api.openweathermap.org/data/2.5/`.
- **Sync Strategy:** Phone-first via `WearableDataManager` and `PhoneSyncRepository`, with a wearable direct-call fallback governed by `WearableWeatherViewModel`.

Refer to `ARCHITECTURE.md`, `IMPLEMENTATION_SUMMARY.md`, and `UNIFIED_VIEWMODEL_ARCHITECTURE.md` for diagrams, class maps, and design constraints (e.g., identical package names across modules).

---

## Module Breakdown
| Module | Purpose | Highlights |
| --- | --- | --- |
| `app` | Primary Android app | Weather search, forecasts, AI prediction engine, Building Profiles, Settings, DI graph |
| `wearable` | Wear OS companion | Animated weather UI, phone sync service, standalone API client, Hilt-based MVVM stack |

---

## Data Flow
```
User Action → Compose UI → ViewModel → Repository → Retrofit Service → OpenWeatherMap
                                                            ↓
                                                    Resource<T> stream
                                                            ↓
                                                     StateFlow → UI
```

Wearable refresh sequence:
1. Request sync from phone via Data Layer.
2. If phone responds, update UI with synced payload.
3. If phone unreachable, `WearableWeatherViewModel` issues a direct API call using the saved city from settings.

---

## Key Capabilities
- Animated Compose UI tailored for round/square watch faces.
- Smart fallback routing ensures the watch can fetch weather independently.
- Resource-based error handling (offline, HTTP error codes, unexpected failures).
- API key sourcing from `secrets.properties` or in-app Settings.
- Local caching via `WeatherDataCache` (mobile) and `WearableDataManager` (wearable).

---

## Getting Started
1. Create an OpenWeatherMap API key.
2. Add `WEATHER_API_KEY=<your key>` to `secrets.properties` (or enter through Settings at runtime).
3. Sync Gradle and build both modules.
4. Launch the phone app, search for a city, and verify forecasts + AI insights.
5. Pair a Wear OS emulator or device (see `SMART_WATCH_INTEGRATION_GUIDE.md`) and run the wearable module to confirm Data Layer sync.

---

## Testing & Quality
- Repositories and ViewModels are covered via `kotlinx-coroutines-test` flows (see `IMPLEMENTATION_SUMMARY.md` for sample tests).
- Compose UIs rely on previewable state holders and can be wired into screenshot/UI tests.
- Follow the package-name parity requirement to keep Wear OS communication functional.

---

## Reference Files
- `app/src/main/java/com/weatherapp/...` – Mobile app screens, repositories, prediction services.
- `wearable/src/main/java/com/weather/wearable/...` – Wear OS MVVM layers, constants, sync services.
- Project documentation: `ARCHITECTURE.md`, `IMPLEMENTATION_SUMMARY.md`, `CONSOLIDATION_SUMMARY.md`, `MVVM_ARCHITECTURE.md`, `SMART_WATCH_INTEGRATION_GUIDE.md`, `SIMPLIFIED_DATA_MANAGER.md`.
