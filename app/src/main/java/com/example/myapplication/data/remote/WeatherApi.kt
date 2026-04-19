package com.example.myapplication.data.remote

import com.example.myapplication.data.remote.dto.ForecastResponseDto
import com.example.myapplication.data.remote.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather")
    suspend fun getCurrentWeather(
//        @Query("lat") lat: Double,
//        @Query("lon") lon: Double,
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String = "zh_cn",
        @Query("units") units: String = "metric"
    ): WeatherResponseDto

    @GET("forecast")
    suspend fun getForecast(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String = "zh_cn",
        @Query("units") units: String = "metric",
        @Query("cnt") cnt: Int = 40
    ): ForecastResponseDto

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    }
}
