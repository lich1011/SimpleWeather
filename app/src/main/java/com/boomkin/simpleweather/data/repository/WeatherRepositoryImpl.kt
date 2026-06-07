package com.boomkin.simpleweather.data.repository

import com.boomkin.simpleweather.data.local.dao.CachedWeatherDao
import com.boomkin.simpleweather.data.local.dao.CityDao
import com.boomkin.simpleweather.data.local.dao.GeocodingDao
import com.boomkin.simpleweather.data.local.dao.WeatherRecordDao
import com.boomkin.simpleweather.data.local.entity.CachedWeatherEntity
import com.boomkin.simpleweather.data.local.entity.GeocodingEntity
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val cityDao: CityDao,
    private val weatherRecordDao: WeatherRecordDao,
    private val cachedWeatherDao: CachedWeatherDao,
    private val geocodingDao: GeocodingDao
) : WeatherRepository {

    /**
     * Fetch weather by city name.  Uses a single Geocoding + single Forecast call.
     */
    override suspend fun getWeatherData(cityName: String): Result<WeatherData> {
        return try {
            // 1. Try to use cached coordinates from cities table first
            val cachedCity = cityDao.getCityByName(cityName)
            if (cachedCity != null && cachedCity.latitude != 0.0) {
                Timber.d("getWeatherData: Using cached coordinates for ${cachedCity.name}")
                return getWeatherData(cachedCity.toCity())
            }

            // 2. Try to use cached coordinates from geocoding cache next
            val queryKey = cityName.lowercase().trim()
            val cachedGeo = geocodingDao.getByQuery(queryKey)
            if (cachedGeo != null && cachedGeo.latitude != 0.0) {
                Timber.d("getWeatherData: Using cached coordinates from geocoding cache for ${cachedGeo.cityName}")
                return getWeatherData(
                    City(
                        name = cachedGeo.cityName,
                        country = cachedGeo.country,
                        latitude = cachedGeo.latitude,
                        longitude = cachedGeo.longitude
                    )
                )
            }

            // 3. Fall back to remote geocoding
            Timber.d("getWeatherData: Geocoding started for city: $cityName")
            val geoResponse = api.searchCity(name = cityName)
            val result = geoResponse.results?.firstOrNull()
                ?: throw Exception("City not found: $cityName")

            Timber.d("getWeatherData: Geocoding result -> ${result.name} (${result.latitude}, ${result.longitude})")

            val (response, aqi) = coroutineScope {
                val forecastDeferred = async { api.getWeather(latitude = result.latitude, longitude = result.longitude) }
                val aqiDeferred = async { fetchUsAqi(latitude = result.latitude, longitude = result.longitude) }
                forecastDeferred.await() to aqiDeferred.await()
            }

            val weather = response.toWeather(result.name, result.country ?: "").copy(aqi = aqi)
            val daily = response.toForecastItems()
            val hourly = response.toHourlyForecastItems()

            val weatherData = WeatherData(weather, daily, hourly)
            saveToCache(weatherData)

            // Save resolved geocoding to cache
            saveGeocodingToCache(cityName, result.name, result.country ?: "", result.latitude, result.longitude)

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
            val (response, aqi) = coroutineScope {
                val forecastDeferred = async { api.getWeather(latitude = city.latitude, longitude = city.longitude) }
                val aqiDeferred = async { fetchUsAqi(latitude = city.latitude, longitude = city.longitude) }
                forecastDeferred.await() to aqiDeferred.await()
            }

            val weather = response.toWeather(city.name, city.country).copy(aqi =aqi)
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
            weatherData = data.weather,
            dailyForecast = data.dailyForecast,
            hourlyForecast = data.hourlyForecast,
            lastUpdated = System.currentTimeMillis()
        )
        cachedWeatherDao.insertCachedWeather(entity)
    }

    override fun getCachedWeatherDataFlow(cityName: String): Flow<WeatherData?> {
        return cachedWeatherDao.getCachedWeatherFlow(cityName).map { entity ->
            if (entity == null) return@map null
            WeatherData(entity.weatherData, entity.dailyForecast, entity.hourlyForecast)
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
    }

    /**
     * Add a city.  Geocodes the name, then stores the resolved name + coordinates
     * so that future lookups use the canonical name (fixes duplicate-insert bug).
     */
    override suspend fun addCity(cityName: String): Result<City> {
        return try {
            // Check geocoding cache first
            val queryKey = cityName.lowercase().trim()
            val cachedGeo = geocodingDao.getByQuery(queryKey)
            
            val canonicalName: String
            val country: String
            val latitude: Double
            val longitude: Double

            if (cachedGeo != null) {
                Timber.d("addCity: Using cached geocoding for $cityName")
                canonicalName = cachedGeo.cityName
                country = cachedGeo.country
                latitude = cachedGeo.latitude
                longitude = cachedGeo.longitude
            } else {
                // 1. Geocode first to get the canonical name
                Timber.d("addCity: Geocoding city name: $cityName")
                val geoResponse = api.searchCity(name = cityName)
                val result = geoResponse.results?.firstOrNull()
                    ?: throw Exception("City not found: $cityName")

                canonicalName = result.name
                country = result.country ?: ""
                latitude = result.latitude
                longitude = result.longitude

                saveGeocodingToCache(cityName, canonicalName, country, latitude, longitude)
            }

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
                    country = country,
                    latitude = latitude,
                    longitude = longitude
                )
                Timber.d("addCity: Inserting new city: $canonicalName ($latitude, $longitude)")
                val id = cityDao.insertCity(newCity)
                newCity.copy(id = id.toInt()).toCity()
            }

            // Check if there is currently a default city, if not set this one as default
            val defaultCity = cityDao.getDefaultCity()
            if (defaultCity == null) {
                cityDao.setDefaultCity(addedCity.id)
            }

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
    }

    override suspend fun setDefaultCity(cityId: Int) {
        cityDao.setDefaultCity(cityId)
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

    private suspend fun fetchUsAqi(latitude: Double, longitude: Double): Int?{
        return try{
            api.getAirQuality(latitude = latitude, longitude =longitude).current?.usAqi?.roundToInt()
        }catch (e: Exception){
            Timber.w(e, "fetchUsAqi failed for ($latitude,$longitude), default AQI to 0")
            null
        }
    }

    private suspend fun saveGeocodingToCache(
        query: String,
        cityName: String,
        country: String,
        latitude: Double,
        longitude: Double
    ) {
        try {
            val queryKey = query.lowercase().trim()
            val canonicalKey = cityName.lowercase().trim()
            
            geocodingDao.insert(
                GeocodingEntity(
                    query = queryKey,
                    cityName = cityName,
                    country = country,
                    latitude = latitude,
                    longitude = longitude
                )
            )
            
            if (queryKey != canonicalKey) {
                geocodingDao.insert(
                    GeocodingEntity(
                        query = canonicalKey,
                        cityName = cityName,
                        country = country,
                        latitude = latitude,
                        longitude = longitude
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to save geocoding to cache for query=$query, city=$cityName")
        }
    }

}
