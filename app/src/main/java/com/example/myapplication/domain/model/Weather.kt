package com.example.myapplication.domain.model

data class Weather(
    val cityName: String,
    val country: String,
    val timestamp: Long,
    val date: String,
    val tempCurrent: Double,
    val tempMin: Double,
    val tempMax: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val temperature: Double,
    val weatherDesc: String,
    val weatherIcon: String,
    val visibility: Int,
    val sunrise: Long,
    val sunset: Long
){
    val iconUrl: String
        get() ="https://openweathermap.org/img/wn/${weatherIcon}@2x.png"
}

data class City(
    val id: Int =0,
    val name: String,
    val country: String,
    val isDefault: Boolean = false,
)

data class ForecastItem(
    val timestamp: Long,
    val dtTxt: String,
    val temp: Double,
    val tempMin: Double,
    val tempMax: Double,
    val weatherMain: String,
    val weatherDesc: String,
    val weatherIcon: String,
    val humidity: Int,
    val windSpeed: Double
){
    val iconUrl: String
        get() ="https://openweathermap.org/img/wn/${weatherIcon}@2x.png"
}


