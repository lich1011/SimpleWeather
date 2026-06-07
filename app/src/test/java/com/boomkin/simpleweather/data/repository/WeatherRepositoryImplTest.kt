package com.boomkin.simpleweather.data.repository

import com.boomkin.simpleweather.data.local.dao.CachedWeatherDao
import com.boomkin.simpleweather.data.local.dao.CityDao
import com.boomkin.simpleweather.data.local.dao.GeocodingDao
import com.boomkin.simpleweather.data.local.dao.WeatherRecordDao
import com.boomkin.simpleweather.data.local.entity.CachedWeatherEntity
import com.boomkin.simpleweather.data.local.entity.CityEntity
import com.boomkin.simpleweather.data.local.entity.GeocodingEntity
import com.boomkin.simpleweather.data.local.entity.WeatherRecordEntity
import com.boomkin.simpleweather.data.remote.WeatherApi
import com.boomkin.simpleweather.data.remote.dto.AirQualityResponseDto
import com.boomkin.simpleweather.data.remote.dto.GeocodingResponseDto
import com.boomkin.simpleweather.data.remote.dto.GeocodingResultDto
import com.boomkin.simpleweather.data.remote.dto.OpenMeteoResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherRepositoryImplTest {

    private lateinit var fakeApi: FakeWeatherApi
    private lateinit var fakeCityDao: FakeCityDao
    private lateinit var fakeWeatherRecordDao: FakeWeatherRecordDao
    private lateinit var fakeCachedWeatherDao: FakeCachedWeatherDao
    private lateinit var fakeGeocodingDao: FakeGeocodingDao
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setUp() {
        fakeApi = FakeWeatherApi()
        fakeCityDao = FakeCityDao()
        fakeWeatherRecordDao = FakeWeatherRecordDao()
        fakeCachedWeatherDao = FakeCachedWeatherDao()
        fakeGeocodingDao = FakeGeocodingDao()

        repository = WeatherRepositoryImpl(
            api = fakeApi,
            cityDao = fakeCityDao,
            weatherRecordDao = fakeWeatherRecordDao,
            cachedWeatherDao = fakeCachedWeatherDao,
            geocodingDao = fakeGeocodingDao
        )
    }

    @Test
    fun getWeatherData_whenCacheIsEmpty_callsApiAndCachesGeocoding() = runBlocking {
        // Given cache is empty
        val cityName = "Paris"

        // When requesting weather
        val result = repository.getWeatherData(cityName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeApi.searchCityCalls) // Calls API search
        assertEquals(2, fakeGeocodingDao.insertCalls) // Stores query and canonical mapping

        // Verify it cached the query "paris"
        val cachedGeo = fakeGeocodingDao.getByQuery("paris")
        assertNotNull(cachedGeo)
        assertEquals("Beijing", cachedGeo?.cityName) // Matches FakeApi output name
    }

    @Test
    fun getWeatherData_whenCacheHasLocation_skipsGeocodingApi() = runBlocking {
        // Given cache contains Paris
        fakeGeocodingDao.insert(
            GeocodingEntity(
                query = "paris",
                cityName = "Paris",
                country = "France",
                latitude = 48.8,
                longitude = 2.3
            )
        )
        fakeGeocodingDao.insertCalls = 0 // Reset counts

        // When requesting weather
        val result = repository.getWeatherData("Paris")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0, fakeApi.searchCityCalls) // API search skipped!
        assertEquals(0, fakeGeocodingDao.insertCalls) // No extra caching inserts needed
    }

    @Test
    fun addCity_whenCacheIsEmpty_callsApiAndCachesGeocoding() = runBlocking {
        // Given cache is empty
        val cityName = "Tokyo"

        // When adding city
        val result = repository.addCity(cityName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeApi.searchCityCalls) // Calls API
        assertEquals(2, fakeGeocodingDao.insertCalls) // Stores in cache

        val cachedGeo = fakeGeocodingDao.getByQuery("tokyo")
        assertNotNull(cachedGeo)
    }

    @Test
    fun addCity_whenCacheHasLocation_skipsGeocodingApi() = runBlocking {
        // Given cache has Tokyo
        fakeGeocodingDao.insert(
            GeocodingEntity(
                query = "tokyo",
                cityName = "Tokyo",
                country = "Japan",
                latitude = 35.6,
                longitude = 139.6
            )
        )
        fakeGeocodingDao.insertCalls = 0

        // When adding city
        val result = repository.addCity("tokyo")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0, fakeApi.searchCityCalls) // API skipped!
        assertEquals(0, fakeGeocodingDao.insertCalls)
    }

    // --- Fake Implementations ---

    private class FakeWeatherApi : WeatherApi {
        var searchCityCalls = 0
        var getWeatherCalls = 0
        var getAirQualityCalls = 0

        override suspend fun searchCity(name: String, count: Int, language: String): GeocodingResponseDto {
            searchCityCalls++
            return GeocodingResponseDto(
                results = listOf(
                    GeocodingResultDto(
                        id = 1,
                        name = "Beijing",
                        latitude = 39.9,
                        longitude = 116.4,
                        country = "China"
                    )
                )
            )
        }

        override suspend fun getWeather(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            timezone: String
        ): OpenMeteoResponseDto {
            getWeatherCalls++
            return OpenMeteoResponseDto(
                latitude = latitude,
                longitude = longitude,
                timezone = "UTC",
                current = null,
                hourly = null,
                daily = null
            )
        }

        override suspend fun getAirQuality(
            latitude: Double,
            longitude: Double,
            current: String
        ): AirQualityResponseDto {
            getAirQualityCalls++
            return AirQualityResponseDto(
                current = null
            )
        }
    }

    private class FakeCityDao : CityDao() {
        private val cities = mutableListOf<CityEntity>()
        var getCityByNameCalls = 0

        override fun getAllCities(): Flow<List<CityEntity>> = flow { emit(cities) }

        override suspend fun getAllCitiesSync(): List<CityEntity> = cities

        override fun getArchivedCities(): Flow<List<CityEntity>> = flow { emit(cities.filter { !it.isActive }) }

        override suspend fun getDefaultCity(): CityEntity? = cities.firstOrNull { it.isDefault }

        override suspend fun getCityByName(cityName: String): CityEntity? {
            getCityByNameCalls++
            return cities.firstOrNull { it.name.equals(cityName, ignoreCase = true) }
        }

        override suspend fun insertCity(city: CityEntity): Long {
            cities.add(city)
            return cities.size.toLong()
        }

        override suspend fun softDeleteCity(cityId: Int) {}

        override suspend fun reactivateCity(cityId: Int, time: Long) {}

        override suspend fun clearAllDefaults() {}

        override suspend fun markCityAsDefault(cityId: Int) {}
    }

    private class FakeWeatherRecordDao : WeatherRecordDao {
        override fun getRecordForCity(cityName: String): Flow<List<WeatherRecordEntity>> = flow { emit(emptyList()) }
        override suspend fun insert(record: WeatherRecordEntity) {}
        override suspend fun deleteOldRecords(cityName: String, keepCount: Int) {}
    }

    private class FakeCachedWeatherDao : CachedWeatherDao {
        private val cache = mutableMapOf<String, CachedWeatherEntity>()

        override suspend fun getCachedWeather(cityName: String): CachedWeatherEntity? = cache[cityName]

        override fun getCachedWeatherFlow(cityName: String): Flow<CachedWeatherEntity?> = flow { emit(cache[cityName]) }

        override suspend fun insertCachedWeather(entity: CachedWeatherEntity) {
            cache[entity.cityName] = entity
        }

        override suspend fun deleteCachedWeather(cityName: String) {
            cache.remove(cityName)
        }
    }

    private class FakeGeocodingDao : GeocodingDao {
        private val cache = mutableMapOf<String, GeocodingEntity>()
        var getByQueryCalls = 0
        var insertCalls = 0

        override suspend fun getByQuery(query: String): GeocodingEntity? {
            getByQueryCalls++
            return cache[query]
        }

        override suspend fun insert(geocoding: GeocodingEntity) {
            insertCalls++
            cache[geocoding.query] = geocoding
        }
    }
}
