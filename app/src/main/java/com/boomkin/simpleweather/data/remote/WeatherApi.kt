package com.boomkin.simpleweather.data.remote

import com.boomkin.simpleweather.data.remote.dto.AirQualityResponseDto
import com.boomkin.simpleweather.data.remote.dto.GeocodingResponseDto
import com.boomkin.simpleweather.data.remote.dto.OpenMeteoResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    //https://open-meteo.com/en/docs
    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "zh"
    ): GeocodingResponseDto

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,surface_pressure,apparent_temperature,visibility,uv_index",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,relative_humidity_2m,wind_speed_10m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code,sunrise,sunset",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponseDto

    @GET("https://air-quality-api.open-meteo.com/v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "us_aqi"
    ): AirQualityResponseDto

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }
}
