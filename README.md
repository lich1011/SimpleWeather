# SimpleWeather

SimpleWeather is a clean, modern Android weather application built using Kotlin, Jetpack Compose, Hilt, Room, and Retrofit. It fetches weather forecasts from Open-Meteo API and features offline caching.

---

## Database & Schemas (`app/schemas/`)

Room exports the database schema to the `app/schemas/com.boomkin.simpleweather.data.local.WeatherDatabase/` directory.

### Why are schema files named like `7.json`?
Room automatically generates these schema files named after the database version (`<version>.json`) when compilation runs with the `room.schemaLocation` compiler argument.
* **Auto-generation:** Room's annotation compiler (KSP) requires these exact numeric names to maintain schema history.
* **Migration Testing:** The automated migration testing framework (`MigrationTestHelper`) looks for these numeric filenames to perform migration schema verification tests. **Do not rename these files**, as doing so will break the compiler and automated test suites.

### Database Version History

| Version | Schema File | Description |
| :---: | :--- | :--- |
| **5** | [5.json](file:///Users/luokai/Github/SimpleWeather/app/schemas/com.boomkin.simpleweather.data.local.WeatherDatabase/5.json) | Base weather record caching and city configuration. |
| **6** | [6.json](file:///Users/luokai/Github/SimpleWeather/app/schemas/com.boomkin.simpleweather.data.local.WeatherDatabase/6.json) | Identical structure to version 5; version incremented for clean migration verification. |
| **7** | [7.json](file:///Users/luokai/Github/SimpleWeather/app/schemas/com.boomkin.simpleweather.data.local.WeatherDatabase/7.json) | **Current version.** Added `geocoding_cache` table to cache coordinates resolved from city names, reducing external OpenMeteo geocoding API calls. |
