package com.boomkin.simpleweather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_records")
data class WeatherRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int=0,
    val cityName: String,
    val country: String,
    val date: String,
    val timestamp: Long,
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
)
