package com.boomkin.simpleweather.data.mapper

import com.boomkin.simpleweather.data.local.entity.CityEntity
import com.boomkin.simpleweather.data.local.entity.WeatherRecordEntity
import com.boomkin.simpleweather.data.remote.dto.GeocodingResultDto
import com.boomkin.simpleweather.data.remote.dto.OpenMeteoResponseDto
import com.boomkin.simpleweather.domain.model.City
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.HourlyForecastItem
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.domain.model.WeatherType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Map WMO weather code to Chinese description and OpenWeatherMap-compatible icon code.
 *
 * Icon codes follow the OWM naming convention (e.g. "01d") so that the existing
 * icon URL (`openweathermap.org/img/wn/...`) keeps working as a convenient fallback.
 * TODO: Migrate to local drawable resources to remove the external CDN dependency.
 */
fun getWeatherDescAndIcon(code: Int): Pair<String, String> {
    return when (code) {
        0 -> Pair("晴", "01d")
        1 -> Pair("大部晴朗", "02d")
        2 -> Pair("多云", "03d")
        3 -> Pair("阴", "04d")
        45, 48 -> Pair("雾", "50d")
        51, 53, 55 -> Pair("毛毛雨", "09d")
        56, 57 -> Pair("冻毛毛雨", "09d")
        61 -> Pair("小雨", "10d")
        63 -> Pair("中雨", "10d")
        65 -> Pair("大雨", "10d")
        66, 67 -> Pair("冻雨", "13d")
        71 -> Pair("小雪", "13d")
        73 -> Pair("中雪", "13d")
        75 -> Pair("大雪", "13d")
        77 -> Pair("雪粒", "13d")
        80 -> Pair("小阵雨", "09d")
        81 -> Pair("中阵雨", "09d")
        82 -> Pair("大阵雨", "09d")
        85 -> Pair("小阵雪", "13d")
        86 -> Pair("大阵雪", "13d")
        95 -> Pair("雷暴", "11d")
        96, 99 -> Pair("雷暴伴冰雹", "11d")
        else -> Pair("未知", "03d")
    }
}

// ---------------------------------------------------------------------------
// OpenMeteoResponseDto -> Domain
// ---------------------------------------------------------------------------

/**
 * Convert OpenMeteoResponseDto to the Domain Weather object (current conditions).
 */
fun OpenMeteoResponseDto.toWeather(cityName: String, country: String): Weather {
    val currentData = this.current
    val dailyData = this.daily

    val timestamp = System.currentTimeMillis()
    val wmoCode = currentData?.weatherCode ?: 0
    val (desc, icon) = getWeatherDescAndIcon(wmoCode)
    val dateStr = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(LocalDateTime.now())

    return Weather(
        cityName = cityName,
        country = country,
        timestamp = timestamp,
        date = dateStr,
        tempCurrent = currentData?.temperature ?: 0.0,
        tempMin = dailyData?.temperature2mMin?.firstOrNull() ?: 0.0,
        tempMax = dailyData?.temperature2mMax?.firstOrNull() ?: 0.0,
        feelsLike = currentData?.apparentTemperature ?: currentData?.temperature ?: 0.0,
        humidity = currentData?.humidity ?: 0,
        pressure = currentData?.surfacePressure?.toInt() ?: 0,
        windSpeed = currentData?.windSpeed ?: 0.0,
        weatherDesc = desc,
        weatherIcon = icon,
        weatherType = WeatherType.fromWmoCode(wmoCode),
        visibility = currentData?.visibility?.toInt() ?: 10000,
        sunrise = parseDailyTimeToEpoch(dailyData?.sunrise?.firstOrNull()),
        sunset = parseDailyTimeToEpoch(dailyData?.sunset?.firstOrNull()),
        uvIndex = currentData?.uvIndex ?: 0.0

    )
}

/**
 * Convert daily forecast data to a list of ForecastItems.
 */
fun OpenMeteoResponseDto.toForecastItems(): List<ForecastItem> {
    val items = mutableListOf<ForecastItem>()
    val dailyData = this.daily ?: return items

    for (i in dailyData.time.indices) {
        val dateStr = dailyData.time[i]
        val (desc, icon) = getWeatherDescAndIcon(dailyData.weatherCode?.getOrNull(i) ?: 0)
        val avgTemp = ((dailyData.temperature2mMax.getOrNull(i) ?: 0.0) +
                (dailyData.temperature2mMin.getOrNull(i) ?: 0.0)) / 2.0
        items.add(
            ForecastItem(
                timestamp = parseDateToEpochMillis(dateStr),
                dtTxt = dateStr,
                temp = avgTemp,
                tempMin = dailyData.temperature2mMin.getOrNull(i) ?: 0.0,
                tempMax = dailyData.temperature2mMax.getOrNull(i) ?: 0.0,
                weatherMain = desc,
                weatherDesc = desc,
                weatherIcon = icon,
                humidity = 0,    // daily endpoint does not provide average humidity
                windSpeed = 0.0, // daily endpoint does not provide average wind speed
                sunrise = dailyData.sunrise?.getOrNull(i) ?: "",
                sunset = dailyData.sunset?.getOrNull(i) ?: ""
            )
        )
    }
    return items
}

/**
 * Convert hourly forecast data to a list of HourlyForecastItems.
 * Only returns the next 24 hours to keep the list manageable.
 */
fun OpenMeteoResponseDto.toHourlyForecastItems(): List<HourlyForecastItem> {
    val items = mutableListOf<HourlyForecastItem>()
    val hourlyData = this.hourly ?: return items

    // Find the index closest to the current time and take the next 24 entries
    val currentTime = this.current?.time ?: ""
    val startIndex = hourlyData.time.indexOfFirst { it >= currentTime }.coerceAtLeast(0)
    val endIndex = minOf(startIndex + 24, hourlyData.time.size)

    for (i in startIndex until endIndex) {
        val (desc, icon) = getWeatherDescAndIcon(hourlyData.weatherCode?.getOrNull(i) ?: 0)
        items.add(
            HourlyForecastItem(
                time = hourlyData.time[i],
                temperature = hourlyData.temperature2m.getOrNull(i) ?: 0.0,
                weatherDesc = desc,
                weatherIcon = icon,
                humidity = hourlyData.relativeHumidity2m?.getOrNull(i) ?: 0,
                windSpeed = hourlyData.windSpeed10m?.getOrNull(i) ?: 0.0
            )
        )
    }
    return items
}

// ---------------------------------------------------------------------------
// Domain <-> DB Entity
// ---------------------------------------------------------------------------

fun Weather.toEntity(): WeatherRecordEntity {
    return WeatherRecordEntity(
        cityName = cityName,
        country = country,
        date = date,
        timestamp = timestamp,
        tempCurrent = tempCurrent,
        tempMin = tempMin,
        tempMax = tempMax,
        feelsLike = feelsLike,
        humidity = humidity,
        pressure = pressure,
        windSpeed = windSpeed,
        weatherDesc = weatherDesc,
        weatherIcon = weatherIcon,
        weatherType = weatherType.name,
        visibility = visibility,
        sunrise = sunrise,
        sunset = sunset
    )
}

fun WeatherRecordEntity.toWeather(): Weather {
    return Weather(
        cityName = cityName,
        country = country,
        date = date,
        timestamp = timestamp,
        tempCurrent = tempCurrent,
        tempMin = tempMin,
        tempMax = tempMax,
        feelsLike = feelsLike,
        humidity = humidity,
        pressure = pressure,
        windSpeed = windSpeed,
        weatherDesc = weatherDesc,
        weatherIcon = weatherIcon,
        weatherType = try {
            WeatherType.valueOf(weatherType)
        } catch (_: Exception) {
            WeatherType.CLOUDY
        },
        visibility = visibility,
        sunrise = sunrise,
        sunset = sunset
    )
}

// ---------------------------------------------------------------------------
// City <-> CityEntity
// ---------------------------------------------------------------------------

fun CityEntity.toCity(): City {
    return City(
        id = id,
        name = name,
        country = country,
        latitude = latitude,
        longitude = longitude,
        isDefault = isDefault
    )
}

fun City.toEntity(): CityEntity {
    return CityEntity(
        id = id,
        name = name,
        country = country,
        latitude = latitude,
        longitude = longitude,
        isDefault = isDefault
    )
}

fun GeocodingResultDto.toEntity(isDefault: Boolean = false): CityEntity {
    return CityEntity(
        name = name,
        country = country ?: "",
        latitude = latitude,
        longitude = longitude,
        isDefault = isDefault
    )
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Parse an ISO-8601 local datetime string (e.g. "2026-05-24T05:23") into epoch seconds.
 * Returns 0L on null or parse failure.
 */
private fun parseDailyTimeToEpoch(isoTime: String?): Long {
    if (isoTime == null) return 0L
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val ldt = LocalDateTime.parse(isoTime, formatter)
        ldt.atZone(ZoneId.systemDefault()).toEpochSecond()
    } catch (_: DateTimeParseException) {
        0L
    }
}

/**
 * Parse a date string (e.g. "2026-06-06") into epoch milliseconds using the system default timezone.
 * Used for ForecastItem.timestamp to align with server-reported dates.
 */
private fun parseDateToEpochMillis(dateStr: String): Long {
    return try {
        val localDate = LocalDate.parse(dateStr) // ISO_LOCAL_DATE ("yyyy-MM-dd")
        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } catch (_: DateTimeParseException) {
        0L
    }
}
