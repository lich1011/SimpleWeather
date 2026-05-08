package com.boomkin.simpleweather.data.repository

import com.boomkin.simpleweather.domain.model.City
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.domain.repository.WeatherRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FakeWeatherRepositoryImpl @Inject constructor() : WeatherRepository {

    private val citiesFlow = MutableStateFlow<List<City>>(
        listOf(
            City(id = 1, name = "Beijing", country = "CN", isDefault = true),
            City(id = 2, name = "New York", country = "US", isDefault = false)
        )
    )

    private val historyFlow = MutableStateFlow<List<Weather>>(emptyList())

    override suspend fun getCurrentWeather(cityName: String): Result<Weather> {
        delay(500) // Simulate network delay
        if (cityName.isBlank()) return Result.failure(Exception("City name cannot be empty"))

        val mockWeather = Weather(
            cityName = cityName,
            country = "MockCountry",
            date = "2026-05-08 12:00:00",
            timestamp = System.currentTimeMillis(),
            tempCurrent = 25.5,
            tempMin = 18.0,
            tempMax = 28.0,
            feelsLike = 26.0,
            humidity = 60,
            pressure = 1012,
            windSpeed = 15.0,
            temperature = 25.5,
            weatherDesc = "Clear sky",
            weatherIcon = "01d",
            visibility = 10000,
            sunrise = 1680000000,
            sunset = 1680040000
        )
        return Result.success(mockWeather)
    }

    override suspend fun getForecast(cityName: String): Result<List<ForecastItem>> {
        delay(500) // Simulate network delay
        val mockForecast = List(8) { index ->
            ForecastItem(
                timestamp = System.currentTimeMillis() + (index * 3 * 3600 * 1000),
                dtTxt = "2026-05-08 ${12 + (index * 3)}:00:00",
                temp = 20.0 + index,
                tempMin = 18.0,
                tempMax = 28.0,
                weatherMain = "Clear",
                weatherDesc = "Clear sky",
                weatherIcon = "01d",
                humidity = 50,
                windSpeed = 10.0
            )
        }
        return Result.success(mockForecast)
    }

    override fun getCities(): Flow<List<City>> {
        return citiesFlow
    }

    override suspend fun addCity(cityName: String): Result<City> {
        delay(300)
        val currentList = citiesFlow.value.toMutableList()
        val existing = currentList.find { it.name.equals(cityName, ignoreCase = true) }
        if (existing != null) {
            return Result.success(existing)
        }
        val newCity = City(id = currentList.size + 1, name = cityName, country = "MockCountry", isDefault = false)
        currentList.add(newCity)
        citiesFlow.value = currentList
        return Result.success(newCity)
    }

    override suspend fun deleteCity(city: City) {
        val currentList = citiesFlow.value.toMutableList()
        currentList.removeIf { it.id == city.id }
        citiesFlow.value = currentList
    }

    override suspend fun setDefaultCity(cityId: Int) {
        val currentList = citiesFlow.value.map {
            it.copy(isDefault = it.id == cityId)
        }
        citiesFlow.value = currentList
    }

    override fun getWeatherHistory(cityName: String): Flow<List<Weather>> {
        return historyFlow.map { list -> list.filter { it.cityName == cityName } }
    }

    override suspend fun saveWeatherRecords(weather: Weather) {
        val currentList = historyFlow.value.toMutableList()
        currentList.add(weather)
        historyFlow.value = currentList
    }
}
