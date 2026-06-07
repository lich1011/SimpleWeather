package com.boomkin.simpleweather.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.boomkin.simpleweather.R

object LocalizationUtil {

    fun getWeatherDescResId(desc: String): Int? {
        return when (desc) {
            "晴" -> R.string.weather_desc_sunny
            "大部晴朗" -> R.string.weather_desc_mostly_sunny
            "多云" -> R.string.weather_desc_cloudy
            "阴" -> R.string.weather_desc_overcast
            "雾" -> R.string.weather_desc_fog
            "毛毛雨" -> R.string.weather_desc_drizzle
            "冻毛毛雨" -> R.string.weather_desc_freezing_drizzle
            "小雨" -> R.string.weather_desc_light_rain
            "中雨" -> R.string.weather_desc_moderate_rain
            "大雨" -> R.string.weather_desc_heavy_rain
            "冻雨" -> R.string.weather_desc_freezing_rain
            "小雪" -> R.string.weather_desc_light_snow
            "中雪" -> R.string.weather_desc_moderate_snow
            "大雪" -> R.string.weather_desc_heavy_snow
            "雪粒" -> R.string.weather_desc_snow_grains
            "小阵雨" -> R.string.weather_desc_light_showers
            "中阵雨" -> R.string.weather_desc_moderate_showers
            "大阵雨" -> R.string.weather_desc_heavy_showers
            "小阵雪" -> R.string.weather_desc_light_snow_showers
            "大阵雪" -> R.string.weather_desc_heavy_snow_showers
            "雷暴" -> R.string.weather_desc_thunderstorm
            "雷暴伴冰雹" -> R.string.weather_desc_thunderstorm_hail
            "未知" -> R.string.weather_desc_unknown
            else -> null
        }
    }

    @Composable
    fun localizeWeatherDesc(desc: String): String {
        val resId = getWeatherDescResId(desc)
        return if (resId != null) stringResource(resId) else desc
    }

    fun localizeWeatherDesc(desc: String, context: Context): String {
        val resId = getWeatherDescResId(desc)
        return if (resId != null) context.getString(resId) else desc
    }
}
