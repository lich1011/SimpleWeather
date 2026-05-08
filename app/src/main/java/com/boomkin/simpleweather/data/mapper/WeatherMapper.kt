package com.boomkin.simpleweather.data.mapper

import com.boomkin.simpleweather.data.local.entity.CityEntity
import com.boomkin.simpleweather.data.local.entity.WeatherRecordEntity
import com.boomkin.simpleweather.data.remote.dto.CityDto
import com.boomkin.simpleweather.data.remote.dto.ForecastItemDto
import com.boomkin.simpleweather.data.remote.dto.WeatherResponseDto
import com.boomkin.simpleweather.domain.model.City
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.Weather
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dataFormat: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())


/**
 * 将 WeatherResponseDto 转换为 Domain 层的 Weather 对象
 */
fun WeatherResponseDto.toWeather(): Weather {
    return Weather(
        cityName = name,
        country = sys.country ?: "",
        timestamp = dt * 1000L,
        date = dataFormat.format(Date(dt * 1000L)),
        tempCurrent = main.temp,
        tempMin = main.tempMin,
        tempMax = main.tempMax,
        feelsLike = main.feelsLike,
        humidity = main.humidity,
        pressure = main.pressure,
        windSpeed = wind.speed,
        temperature = main.temp,
        weatherDesc = weather.firstOrNull()?.description ?: "",
        weatherIcon = weather.firstOrNull()?.icon ?: "",
        visibility = visibility,
        sunrise = sys.sunrise ?: 0L,
        sunset = sys.sunset ?: 0L
    )
}

/**
 * 将 Weather 对象转换为数据库 Entity WeatherRecordEntity
 */
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
        temperature = temperature,
        weatherDesc = weatherDesc,
        weatherIcon = weatherIcon,
        visibility = visibility,
        sunrise = sunrise,
        sunset = sunset
    )
}

/**
 * 将数据库 Entity WeatherRecordEntity 转换为 Domain 层的 Weather 对象
 */
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
        temperature = temperature,
        weatherDesc = weatherDesc,
        weatherIcon = weatherIcon,
        visibility = visibility,
        sunrise = sunrise,
        sunset = sunset
    )
}

/**
 * 将 ForecastItemDto 转换为 Domain 层的 ForecastItem
 */
fun ForecastItemDto.toForecastItem(): ForecastItem {
    return ForecastItem(
        timestamp = dt * 1000L,
        dtTxt = dtTxt,
        temp = main.temp,
        tempMin = main.tempMin,
        tempMax = main.tempMax,
        weatherMain = weather.firstOrNull()?.main ?: "",
        weatherDesc = weather.firstOrNull()?.description ?: "",
        weatherIcon = weather.firstOrNull()?.icon ?: "",
        humidity = main.humidity,
        windSpeed = wind.speed
    )
}

/**
 * 将 CityEntity 转换为 Domain 层的 City
 */
fun CityEntity.toCity(): City {
    return City(
        id = id,
        name = name,
        country = country,
        isDefault = isDefault
    )
}

/**
 * 将 Domain 层的 City 转换为 CityEntity
 */
fun City.toEntity(): CityEntity {
    return CityEntity(
        id = id,
        name = name,
        country = country,
        isDefault = isDefault
    )
}

/**
 * 将 CityDto 转换为 CityEntity (常用于搜索后保存)
 */
fun CityDto.toEntity(isDefault: Boolean = false): CityEntity {
    return CityEntity(
        name = name,
        country = country,
        isDefault = isDefault
    )
}
