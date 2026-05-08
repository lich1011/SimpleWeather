package com.boomkin.simpleweather.data.repository

import com.boomkin.simpleweather.data.local.dao.CityDao
import com.boomkin.simpleweather.data.local.dao.WeatherRecordDao
import com.boomkin.simpleweather.data.local.entity.CityEntity
import com.boomkin.simpleweather.data.mapper.toCity
import com.boomkin.simpleweather.data.mapper.toEntity
import com.boomkin.simpleweather.data.mapper.toForecastItem
import com.boomkin.simpleweather.data.mapper.toWeather
import com.boomkin.simpleweather.data.remote.WeatherApi
import com.boomkin.simpleweather.domain.model.City
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val cityDao: CityDao,
    private val weatherRecordDao: WeatherRecordDao
) : WeatherRepository {

    // You can replace this with your actual OpenWeatherMap API key
    private val apiKey = "322b272fdfbd0e6c8e37d57fb36c3e98"

    override suspend fun getCurrentWeather(cityName: String): Result<Weather> {
        return try {
            val response = api.getCurrentWeather(cityName = cityName, apiKey = apiKey)
            Result.success(response.toWeather())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getForecast(cityName: String): Result<List<ForecastItem>> {
        return try {
            val response = api.getForecast(cityName = cityName, apiKey = apiKey)
            Result.success(response.list.map { it.toForecastItem() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCities(): Flow<List<City>> {
        return cityDao.getAllCities().map { entities ->
            entities.map { it.toCity() }
        }
    }

    override suspend fun addCity(cityName: String): Result<City> {
        return try {
            val existingCity = cityDao.getCityByName(cityName)
            if (existingCity != null) {
                return Result.success(existingCity.toCity())
            }
            val newCity = CityEntity(name = cityName, country = "")
            val id = cityDao.insertCity(newCity)
            Result.success(newCity.copy(id = id.toInt()).toCity())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCity(city: City) {
        cityDao.deleteCity(city.toEntity())
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
    }
}
