package com.boomkin.simpleweather.data.repository

import android.content.Context
import com.boomkin.simpleweather.presentation.widget.WeatherWidgetStateUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import com.boomkin.simpleweather.data.local.dao.CachedWeatherDao
import com.boomkin.simpleweather.data.local.dao.CityDao
import com.boomkin.simpleweather.data.local.dao.WeatherRecordDao
import com.boomkin.simpleweather.data.local.entity.CachedWeatherEntity
import com.boomkin.simpleweather.data.local.entity.CityEntity
import com.boomkin.simpleweather.data.mapper.toCity
import com.boomkin.simpleweather.data.mapper.toEntity
import com.boomkin.simpleweather.data.mapper.toForecastItems
import com.boomkin.simpleweather.data.mapper.toHourlyForecastItems
import com.boomkin.simpleweather.data.mapper.toWeather
import com.boomkin.simpleweather.data.remote.WeatherApi
import com.boomkin.simpleweather.domain.model.City
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.HourlyForecastItem
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.domain.repository.WeatherData
import com.boomkin.simpleweather.domain.repository.WeatherRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val cityDao: CityDao,
    private val weatherRecordDao: WeatherRecordDao,
    private val cachedWeatherDao: CachedWeatherDao,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) : WeatherRepository {

    /**
     * Fetch weather by city name.  Uses a single Geocoding + single Forecast call.
     */
    override suspend fun getWeatherData(cityName: String): Result<WeatherData> {
        return try {
            // Try to use cached coordinates first
            val cachedCity = cityDao.getCityByName(cityName)
            if (cachedCity != null && cachedCity.latitude != 0.0) {
                Timber.d("getWeatherData: Using cached coordinates for ${cachedCity.name}")
                return getWeatherData(cachedCity.toCity())
            }

            // Fall back to geocoding
            Timber.d("getWeatherData: Geocoding started for city: $cityName")
            val geoResponse = api.searchCity(name = cityName)
            val result = geoResponse.results?.firstOrNull()
                ?: throw Exception("City not found: $cityName")

            Timber.d("getWeatherData: Geocoding result -> ${result.name} (${result.latitude}, ${result.longitude})")

            val response = api.getWeather(latitude = result.latitude, longitude = result.longitude)
            val weather = response.toWeather(result.name, result.country ?: "")
            val daily = response.toForecastItems()
            val hourly = response.toHourlyForecastItems()

            val weatherData = WeatherData(weather, daily, hourly)
            saveToCache(weatherData)

            Timber.i("getWeatherData: Success for $cityName — ${weather.tempCurrent}°C")
            Result.success(weatherData)
        } catch (e: Exception) {
            Timber.e(e, "getWeatherData failed for city: $cityName")
            Result.failure(e)
        }
    }

    /**
     * Fetch weather using a City object with cached coordinates. Skips geocoding entirely.
     */
    override suspend fun getWeatherData(city: City): Result<WeatherData> {
        return try {
            Timber.d("getWeatherData: Fetching for ${city.name} at (${city.latitude}, ${city.longitude})")
            val response = api.getWeather(latitude = city.latitude, longitude = city.longitude)
            val weather = response.toWeather(city.name, city.country)
            val daily = response.toForecastItems()
            val hourly = response.toHourlyForecastItems()

            val weatherData = WeatherData(weather, daily, hourly)
            saveToCache(weatherData)

            Timber.i("getWeatherData: Success for ${city.name} — ${weather.tempCurrent}°C")
            Result.success(weatherData)
        } catch (e: Exception) {
            Timber.e(e, "getWeatherData failed for city: ${city.name}")
            Result.failure(e)
        }
    }

    private suspend fun saveToCache(data: WeatherData) {
        val entity = CachedWeatherEntity(
            cityName = data.weather.cityName,
            weatherDataJson = gson.toJson(data.weather),
            dailyForecastJson = gson.toJson(data.dailyForecast),
            hourlyForecastJson = gson.toJson(data.hourlyForecast),
            lastUpdated = System.currentTimeMillis()
        )
        cachedWeatherDao.insertCachedWeather(entity)
        
        // If the saved city matches the default city, update widget
        val defaultCity = cityDao.getDefaultCity()
        if (defaultCity != null && defaultCity.name == data.weather.cityName) {
            try {
                WeatherWidget().updateAll(context)
                Timber.d("WeatherRepositoryImpl: Widget updated after cache save for default city ${defaultCity.name}")
            } catch (e: Exception) {
                Timber.e(e, "WeatherRepositoryImpl: Failed to update widget after saving cache")
            }
        }
    }

    override fun getCachedWeatherDataFlow(cityName: String): Flow<WeatherData?> {
        return cachedWeatherDao.getCachedWeatherFlow(cityName).map { entity ->
            if (entity == null) return@map null
            try {
                val weather = gson.fromJson(entity.weatherDataJson, Weather::class.java)
                val dailyType = object : TypeToken<List<ForecastItem>>() {}.type
                val daily = gson.fromJson<List<ForecastItem>>(entity.dailyForecastJson, dailyType)
                val hourlyType = object : TypeToken<List<HourlyForecastItem>>() {}.type
                val hourly = gson.fromJson<List<HourlyForecastItem>>(entity.hourlyForecastJson, hourlyType)
                WeatherData(weather, daily, hourly)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse cached weather data for $cityName")
                null
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getLastUpdateTime(cityName: String): Long {
        return cachedWeatherDao.getCachedWeather(cityName)?.lastUpdated ?: 0L
    }

    override suspend fun refreshAllCities() {
        val cities = cityDao.getAllCitiesSync()
        val results = coroutineScope {
            cities.map { cityEntity ->
                async {
                    val city = cityEntity.toCity()
                    getWeatherData(city)
                        .onSuccess { Timber.i("Background sync success for ${city.name}") }
                        .onFailure { e -> Timber.e(e, "Background sync failed for ${city.name}") }
                }
            }.awaitAll()
        }

        val ioException = results
            .mapNotNull { it.exceptionOrNull() }
            .firstOrNull { it is java.io.IOException }

        if (ioException != null) {
            throw ioException
        }
    }

    override fun getCities(): Flow<List<City>> {
        return cityDao.getAllCities().map { entities ->
            entities.map { it.toCity() }
        }
    }

    override fun getArchivedCities(): Flow<List<City>> {
        return cityDao.getArchivedCities().map { entities ->
            entities.map { it.toCity() }
        }
    }

    override suspend fun reactivateCity(city: City) {
        Timber.d("reactivateCity: ${city.name}")
        cityDao.reactivateCity(city.id, System.currentTimeMillis())
        
        // If there is no active default city, set this reactivated city as default
        val defaultCity = cityDao.getDefaultCity()
        if (defaultCity == null) {
            cityDao.setDefaultCity(city.id)
        }
        triggerWidgetUpdate("reactivateCity")
    }

    /**
     * Add a city.  Geocodes the name, then stores the resolved name + coordinates
     * so that future lookups use the canonical name (fixes duplicate-insert bug).
     */
    override suspend fun addCity(cityName: String): Result<City> {
        return try {
            // 1. Geocode first to get the canonical name
            Timber.d("addCity: Geocoding city name: $cityName")
            val geoResponse = api.searchCity(name = cityName)
            val result = geoResponse.results?.firstOrNull()
                ?: throw Exception("City not found: $cityName")

            val canonicalName = result.name
            Timber.d("addCity: Canonical name resolved: $canonicalName")

            // 2. Check DB using the canonical (geocoded) name to prevent duplicates
            val existingCity = cityDao.getCityByName(canonicalName)
            val addedCity = if (existingCity != null) {
                Timber.d("addCity: City $canonicalName already exists in DB (id=${existingCity.id})")
                if (!existingCity.isActive) {
                    Timber.d("addCity: City $canonicalName was inactive. Reactivating.")
                    cityDao.reactivateCity(existingCity.id, System.currentTimeMillis())
                }
                existingCity.toCity()
            } else {
                // 3. Insert with coordinates cached
                val newCity = CityEntity(
                    name = canonicalName,
                    country = result.country ?: "",
                    latitude = result.latitude,
                    longitude = result.longitude
                )
                Timber.d("addCity: Inserting new city: $canonicalName (${result.latitude}, ${result.longitude})")
                val id = cityDao.insertCity(newCity)
                newCity.copy(id = id.toInt()).toCity()
            }

            // Check if there is currently a default city, if not set this one as default
            val defaultCity = cityDao.getDefaultCity()
            if (defaultCity == null) {
                cityDao.setDefaultCity(addedCity.id)
            }

            triggerWidgetUpdate("addCity")
            Result.success(addedCity)
        } catch (e: Exception) {
            Timber.e(e, "addCity failed for city: $cityName")
            Result.failure(e)
        }
    }

    override suspend fun deleteCity(city: City) {
        Timber.d("deleteCity: ${city.name}")
        cityDao.softDeleteCity(city.id)
        cachedWeatherDao.deleteCachedWeather(city.name)

        // If the deleted city was default, set another active city as default
        if (city.isDefault) {
            val remainingCities = cityDao.getAllCitiesSync()
            if (remainingCities.isNotEmpty()) {
                val newDefault = remainingCities.first()
                cityDao.setDefaultCity(newDefault.id)
            }
        }
        triggerWidgetUpdate("deleteCity")
    }

    override suspend fun setDefaultCity(cityId: Int) {
        cityDao.setDefaultCity(cityId)
        triggerWidgetUpdate("setDefaultCity")
    }

    override fun getWeatherHistory(cityName: String): Flow<List<Weather>> {
        return weatherRecordDao.getRecordForCity(cityName).map { entities ->
            entities.map { it.toWeather() }
        }
    }

    override suspend fun saveWeatherRecords(weather: Weather) {
        weatherRecordDao.insert(weather.toEntity())
        // Cleanup old records, keeping only the 20 most recent per city
        weatherRecordDao.deleteOldRecords(weather.cityName)
    }

    private suspend fun triggerWidgetUpdate(tag: String) {
        try {
            val defaultCityEntity = cityDao.getDefaultCity() ?: cityDao.getAllCitiesSync().firstOrNull()
            val defaultCityName = defaultCityEntity?.name
            val weatherData = if (defaultCityName != null) {
                cachedWeatherDao.getCachedWeather(defaultCityName)
            } else null
            
            val weather = weatherData?.let {
                try {
                    gson.fromJson(it.weatherDataJson, Weather::class.java)
                } catch (e: Exception) {
                    null
                }
            }
            
            WeatherWidgetStateUpdater.updateWidgetState(context, weather, defaultCityName)
            Timber.d("WeatherRepositoryImpl: Directly updated Glance widget state & UI from $tag (city: $defaultCityName)")
        } catch (e: Exception) {
            Timber.e(e, "WeatherRepositoryImpl: Failed to directly update Glance widget state from $tag")
        }
    }
}
