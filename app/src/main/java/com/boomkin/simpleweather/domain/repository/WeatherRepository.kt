package com.boomkin.simpleweather.domain.repository

import com.boomkin.simpleweather.domain.model.City
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    suspend fun getCurrentWeather(cityName: String): Result<Weather>

    suspend fun getForecast(cityName: String): Result<List<ForecastItem>>

    fun getCities(): Flow<List<City>>

    suspend fun addCity(cityName: String): Result<City>

    suspend fun deleteCity(city: City)

    suspend fun setDefaultCity(cityId: Int)

    fun getWeatherHistory(cityName: String): Flow<List<Weather>>

    suspend fun saveWeatherRecords(weather: Weather)
}
