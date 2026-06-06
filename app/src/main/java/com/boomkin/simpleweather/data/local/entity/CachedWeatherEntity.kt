package com.boomkin.simpleweather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the full cached weather data for a city so it can be loaded offline instantly.
 */
@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val cityName: String,
    val weatherDataJson: String, // We'll serialize Weather object
    val dailyForecastJson: String, // We'll serialize List<ForecastItem>
    val hourlyForecastJson: String, // We'll serialize List<HourlyForecastItem>
    val lastUpdated: Long
)
