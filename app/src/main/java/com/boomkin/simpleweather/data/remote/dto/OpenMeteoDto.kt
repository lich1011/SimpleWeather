package com.boomkin.simpleweather.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Open-Meteo API response DTO
 */
data class OpenMeteoResponseDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("current") val current: CurrentWeatherDto?,
    @SerializedName("hourly") val hourly: HourlyWeatherDto?,
    @SerializedName("daily") val daily: DailyWeatherDto?
)

data class CurrentWeatherDto(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("surface_pressure") val surfacePressure: Double?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    @SerializedName("visibility") val visibility: Double?,
    @SerializedName("uv_index") val uvIndex: Double?
)

data class HourlyWeatherDto(
    @SerializedName("time") val time: List<String>,
    @SerializedName("temperature_2m") val temperature2m: List<Double>,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: List<Int>?,
    @SerializedName("wind_speed_10m") val windSpeed10m: List<Double>?
)

data class DailyWeatherDto(
    @SerializedName("time") val time: List<String>,
    @SerializedName("temperature_2m_max") val temperature2mMax: List<Double>,
    @SerializedName("temperature_2m_min") val temperature2mMin: List<Double>,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("sunrise") val sunrise: List<String>?,
    @SerializedName("sunset") val sunset: List<String>?
)

/**
 * Open-Meteo Air Quality API response DTO
 */

data class AirQualityResponseDto(
    @SerializedName("current") val current: AirQualityCurrentDto?
)

data class AirQualityCurrentDto(
    @SerializedName("us_aqi") val usAqi: Int?
)

/**
 * Open-Meteo Geocoding API response DTO
 */
data class GeocodingResponseDto(
    @SerializedName("results") val results: List<GeocodingResultDto>?
)

data class GeocodingResultDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("country") val country: String?
)
