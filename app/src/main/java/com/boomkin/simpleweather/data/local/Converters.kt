package com.boomkin.simpleweather.data.local

import androidx.room.TypeConverter
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.HourlyForecastItem
import com.boomkin.simpleweather.domain.model.Weather
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromWeather(weather: Weather): String {
        return gson.toJson(weather)
    }

    @TypeConverter
    fun toWeather(weatherJson: String): Weather {
        return gson.fromJson(weatherJson, Weather::class.java)
    }

    @TypeConverter
    fun fromForecastList(forecast: List<ForecastItem>): String {
        return gson.toJson(forecast)
    }

    @TypeConverter
    fun toForecastList(forecastJson: String): List<ForecastItem> {
        val type = object : TypeToken<List<ForecastItem>>() {}.type
        return gson.fromJson(forecastJson, type)
    }

    @TypeConverter
    fun fromHourlyForecastList(hourlyForecast: List<HourlyForecastItem>): String {
        return gson.toJson(hourlyForecast)
    }

    @TypeConverter
    fun toHourlyForecastList(hourlyForecastJson: String): List<HourlyForecastItem> {
        val type = object : TypeToken<List<HourlyForecastItem>>() {}.type
        return gson.fromJson(hourlyForecastJson, type)
    }
}
