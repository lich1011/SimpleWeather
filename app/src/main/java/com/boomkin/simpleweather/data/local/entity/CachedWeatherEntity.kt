package com.boomkin.simpleweather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.HourlyForecastItem
import com.boomkin.simpleweather.domain.model.Weather

/**
 * Stores the full cached weather data for a city so it can be loaded offline instantly.
 */
@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val cityName: String,
    val weatherData: Weather,
    val dailyForecast: List<ForecastItem>,
    val hourlyForecast: List<HourlyForecastItem>,
    val lastUpdated: Long
)
