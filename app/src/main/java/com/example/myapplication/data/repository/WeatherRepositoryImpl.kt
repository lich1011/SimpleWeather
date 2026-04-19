package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.CityDao
import com.example.myapplication.data.local.dao.WeatherRecordDao
import com.example.myapplication.data.local.entity.CityEntity
import com.example.myapplication.data.mapper.toCity
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.data.mapper.toForecastItem
import com.example.myapplication.data.mapper.toWeather
import com.example.myapplication.data.remote.WeatherApi
import com.example.myapplication.domain.model.City
import com.example.myapplication.domain.model.ForecastItem
import com.example.myapplication.domain.model.Weather
import com.example.myapplication.domain.repository.WeatherRepository
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
