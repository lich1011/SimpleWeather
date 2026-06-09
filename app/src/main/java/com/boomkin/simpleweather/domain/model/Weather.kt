package com.boomkin.simpleweather.domain.model

/**
 * 天气类型枚举，支持中英文关键词匹配 + WMO weather code 直接映射。
 *
 * @param cnKeywords 中文描述关键词（来自 getWeatherDescAndIcon 的返回值）
 * @param enKeywords 英文描述关键词（兼容旧数据或多语言场景）
 */
enum class WeatherType(
    val cnKeywords: List<String>,
    val enKeywords: List<String>
) {
    SUNNY(
        cnKeywords = listOf("晴", "大部晴朗"),
        enKeywords = listOf("clear", "sun", "fair")
    ),
    RAINY(
        cnKeywords = listOf("雨", "毛毛雨", "冻毛毛雨", "冻雨", "阵雨"),
        enKeywords = listOf("rain", "drizzle", "shower")
    ),
    SNOWY(
        cnKeywords = listOf("雪", "雪粒", "阵雪"),
        enKeywords = listOf("snow", "sleet", "flurry")
    ),
    CLOUDY(
        cnKeywords = listOf("云", "阴", "雾"),
        enKeywords = listOf("cloud", "overcast", "fog", "mist")
    ),
    STORM(
        cnKeywords = listOf("雷暴", "雷"),
        enKeywords = listOf("storm", "thunder")
    );

    companion object {
        /**
         * 从天气描述文本推断天气类型（中英文均可匹配）。
         */
//        fun fromDescription(desc: String): WeatherType {
//            return entries.firstOrNull { type ->
//                type.cnKeywords.any { desc.contains(it) } ||
//                type.enKeywords.any { desc.contains(it, ignoreCase = true) }
//            } ?: CLOUDY
//        }

        /**
         * 从 WMO weather code 直接映射天气类型（最可靠的方式）。
         * @see <a href="https://open-meteo.com/en/docs#weathervariables">WMO Weather interpretation codes</a>
         */
        fun fromWmoCode(code: Int): WeatherType = when (code) {
            0, 1 -> SUNNY
            2, 3, 45, 48 -> CLOUDY
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> RAINY
            71, 73, 75, 77, 85, 86 -> SNOWY
            95, 96, 99 -> STORM
            else -> CLOUDY
        }
    }
}

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
    val weatherDesc: String,
    val weatherIcon: String,
    val weatherType: WeatherType,
    val visibility: Int,
    val sunrise: Long,
    val sunset: Long,
    val uvIndex: Double = 0.0,
    val aqi: Int? = null
)

data class City(
    val id: Int = 0,
    val name: String,
    val country: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
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
    val windSpeed: Double,
    val sunrise: String = "",
    val sunset: String = ""
)

data class HourlyForecastItem(
    val time: String,
    val temperature: Double,
    val weatherDesc: String,
    val weatherIcon: String,
    val humidity: Int,
    val windSpeed: Double
)

fun Weather.isNight(): Boolean {
    if (sunrise == 0L || sunset == 0L) {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return hour < 6 || hour >= 18
    }
    val currentSec = timestamp / 1000
    return currentSec < sunrise || currentSec > sunset
}
