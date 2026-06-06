package com.boomkin.simpleweather.domain.repository

import com.boomkin.simpleweather.domain.model.City
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.HourlyForecastItem
import com.boomkin.simpleweather.domain.model.Weather
import kotlinx.coroutines.flow.Flow

/**
 * Aggregated result from a single weather API call.
 */
data class WeatherData(
    val weather: Weather,
    val dailyForecast: List<ForecastItem>,
    val hourlyForecast: List<HourlyForecastItem>
)

interface WeatherRepository {

    /**
     * Fetch current weather, daily forecast, and hourly forecast in a single API call.
     * This avoids redundant geocoding and network requests.
     */
    suspend fun getWeatherData(cityName: String): Result<WeatherData>

    /**
     * Fetch weather data using cached coordinates from a City object.
     * Skips geocoding entirely.
     */
    suspend fun getWeatherData(city: City): Result<WeatherData>

    /**
     * Get weather data from local DB cache for instant UI loading.
     */
    fun getCachedWeatherDataFlow(cityName: String): Flow<WeatherData?>

    /**
     * Get last update time of cached weather data for a city.
     */
    suspend fun getLastUpdateTime(cityName: String): Long

    /**
     * Background task helper to sync all cities.
     */
    suspend fun refreshAllCities()

    fun getCities(): Flow<List<City>>

    fun getArchivedCities(): Flow<List<City>>

    suspend fun reactivateCity(city: City)

    suspend fun addCity(cityName: String): Result<City>

    suspend fun deleteCity(city: City)

    suspend fun setDefaultCity(cityId: Int)

    fun getWeatherHistory(cityName: String): Flow<List<Weather>>

    suspend fun saveWeatherRecords(weather: Weather)
}
