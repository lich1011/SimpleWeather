package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 当前天气响应 DTO
 */
data class WeatherResponseDto(
    @SerializedName("coord") val coord: CoordDto,
    @SerializedName("weather") val weather: List<WeatherDescDto>,
//    @SerializedName("base") val base: String,
    @SerializedName("main") val main: MainDto,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("wind") val wind: WindDto,
    @SerializedName("clouds") val clouds: CloudsDto,
    @SerializedName("dt") val dt: Long,
    @SerializedName("sys") val sys: SysDto,
//    @SerializedName("timezone") val timezone: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
//    @SerializedName("cod") val cod: Int
)

/**
 * 坐标信息
 */
data class CoordDto(
    @SerializedName("lon") val lon: Double,
    @SerializedName("lat") val lat: Double
)

/**
 * 天气描述
 */
data class WeatherDescDto(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

/**
 * 主要天气指标 (对应 maindot)
 */
data class MainDto(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int
)

/**
 * 风力信息
 */
data class WindDto(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int,
//    @SerializedName("gust") val gust: Double?
)

/**
 * 云量信息 (对应 cloudsdot)
 */
data class CloudsDto(
    @SerializedName("all") val all: Int
)

/**
 * 系统信息 (对应 sysdto)
 */
data class SysDto(
//    @SerializedName("type") val type: Int?,
//    @SerializedName("id") val id: Int?,
    @SerializedName("country") val country: String?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?,
//    @SerializedName("pod") val pod: String?
)

/**
 * 天气预报响应 DTO
 */
data class ForecastResponseDto(
//    @SerializedName("cod") val cod: String,
//    @SerializedName("message") val message: Int,
//    @SerializedName("cnt") val cnt: Int,
    @SerializedName("list") val list: List<ForecastItemDto>,
    @SerializedName("city") val city: CityDto
)

/**
 * 预报条目信息 (对应 forecasitemdto)
 */
data class ForecastItemDto(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainDto,
    @SerializedName("weather") val weather: List<WeatherDescDto>,
//    @SerializedName("clouds") val clouds: CloudsDto,
    @SerializedName("wind") val wind: WindDto,
//    @SerializedName("visibility") val visibility: Int,
//    @SerializedName("pop") val pop: Double,
//    @SerializedName("sys") val sys: SysDto,
    @SerializedName("dt_txt") val dtTxt: String
)

/**
 * 城市信息
 */
data class CityDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
//    @SerializedName("coord") val coord: CoordDto,
    @SerializedName("country") val country: String,
//    @SerializedName("population") val population: Int,
//    @SerializedName("timezone") val timezone: Int,
//    @SerializedName("sunrise") val sunrise: Long,
//    @SerializedName("sunset") val sunset: Long
)
