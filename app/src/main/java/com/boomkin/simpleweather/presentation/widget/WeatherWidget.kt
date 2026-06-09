package com.boomkin.simpleweather.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.unit.ColorProvider
import com.boomkin.simpleweather.R
import com.boomkin.simpleweather.MainActivity
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.domain.model.WeatherType
import com.boomkin.simpleweather.domain.model.isNight
import androidx.glance.LocalContext
import com.boomkin.simpleweather.util.LocalizationUtil
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.currentState
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.glance.text.TextAlign

object WeatherWidgetKeys {
    val KEY_HAS_WEATHER = booleanPreferencesKey("has_weather")
    val KEY_CITY_NAME = stringPreferencesKey("city_name")
    val KEY_WEATHER_DESC = stringPreferencesKey("weather_desc")
    val KEY_WEATHER_TYPE = stringPreferencesKey("weather_type")
    val KEY_TEMP_CURRENT = doublePreferencesKey("temp_current")
    val KEY_FEELS_LIKE = doublePreferencesKey("feels_like")
    val KEY_HUMIDITY = intPreferencesKey("humidity")
    val KEY_TARGET_CITY_NAME = stringPreferencesKey("target_city_name")
    val KEY_SUNRISE = longPreferencesKey("sunrise")
    val KEY_SUNSET = longPreferencesKey("sunset")
    val KEY_TIMESTAMP = longPreferencesKey("timestamp")
}

object WeatherWidgetStateUpdater {
    suspend fun updateWidgetState(context: Context, weather: Weather?, targetCityName: String?) {
        try {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(WeatherWidget::class.java)
            for (glanceId in glanceIds) {
                updateAppWidgetState(context, glanceId) { prefs ->
                    if (weather != null) {
                        prefs[WeatherWidgetKeys.KEY_HAS_WEATHER] = true
                        prefs[WeatherWidgetKeys.KEY_CITY_NAME] = weather.cityName
                        prefs[WeatherWidgetKeys.KEY_WEATHER_DESC] = weather.weatherDesc
                        prefs[WeatherWidgetKeys.KEY_WEATHER_TYPE] = weather.weatherType.name
                        prefs[WeatherWidgetKeys.KEY_TEMP_CURRENT] = weather.tempCurrent
                        prefs[WeatherWidgetKeys.KEY_FEELS_LIKE] = weather.feelsLike
                        prefs[WeatherWidgetKeys.KEY_HUMIDITY] = weather.humidity
                        prefs[WeatherWidgetKeys.KEY_TARGET_CITY_NAME] = weather.cityName
                        prefs[WeatherWidgetKeys.KEY_SUNRISE] = weather.sunrise
                        prefs[WeatherWidgetKeys.KEY_SUNSET] = weather.sunset
                        prefs[WeatherWidgetKeys.KEY_TIMESTAMP] = weather.timestamp
                    } else {
                        prefs[WeatherWidgetKeys.KEY_HAS_WEATHER] = false
                        if (targetCityName != null) {
                            prefs[WeatherWidgetKeys.KEY_TARGET_CITY_NAME] = targetCityName
                        } else {
                            prefs.remove(WeatherWidgetKeys.KEY_TARGET_CITY_NAME)
                        }
                    }
                }
            }
            WeatherWidget().updateAll(context)
            timber.log.Timber.d("WeatherWidgetStateUpdater: Widget state updated successfully (hasWeather: ${weather != null})")
        } catch (e: Exception) {
            timber.log.Timber.e(e, "WeatherWidgetStateUpdater: Failed to update widget state")
        }
    }
}

class WeatherWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        // Define sizes that match launcher grids (1x2, 1x4, 2x2)
        private val GRID_1_2 = DpSize(100.dp, 40.dp)
        private val GRID_1_4 = DpSize(220.dp, 40.dp)
        private val GRID_2_2 = DpSize(100.dp, 100.dp)
    }

    // Configure Glance to rebuild layout based on actual widget size bounds
    override val sizeMode = SizeMode.Responsive(setOf(GRID_1_2, GRID_1_4, GRID_2_2))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val hasWeather = prefs[WeatherWidgetKeys.KEY_HAS_WEATHER] ?: false
            val targetCityName = prefs[WeatherWidgetKeys.KEY_TARGET_CITY_NAME]

            val weather = if (hasWeather) {
                val cityName = prefs[WeatherWidgetKeys.KEY_CITY_NAME] ?: ""
                val weatherDesc = prefs[WeatherWidgetKeys.KEY_WEATHER_DESC] ?: ""
                val weatherTypeName = prefs[WeatherWidgetKeys.KEY_WEATHER_TYPE] ?: "CLOUDY"
                val weatherType = try {
                    WeatherType.valueOf(weatherTypeName)
                } catch (e: Exception) {
                    WeatherType.CLOUDY
                }
                val tempCurrent = prefs[WeatherWidgetKeys.KEY_TEMP_CURRENT] ?: 0.0
                val feelsLike = prefs[WeatherWidgetKeys.KEY_FEELS_LIKE] ?: 0.0
                val humidity = prefs[WeatherWidgetKeys.KEY_HUMIDITY] ?: 0
                val sunrise = prefs[WeatherWidgetKeys.KEY_SUNRISE] ?: 0L
                val sunset = prefs[WeatherWidgetKeys.KEY_SUNSET] ?: 0L
                val timestamp = prefs[WeatherWidgetKeys.KEY_TIMESTAMP] ?: 0L

                Weather(
                    cityName = cityName,
                    country = "",
                    timestamp = timestamp,
                    date = "",
                    tempCurrent = tempCurrent,
                    tempMin = tempCurrent,
                    tempMax = tempCurrent,
                    feelsLike = feelsLike,
                    humidity = humidity,
                    pressure = 0,
                    windSpeed = 0.0,
                    weatherDesc = weatherDesc,
                    weatherIcon = "",
                    weatherType = weatherType,
                    visibility = 0,
                    sunrise = sunrise,
                    sunset = sunset
                )
            } else {
                null
            }

            val size = LocalSize.current
            WidgetContent(weather, targetCityName, size)
        }
    }

    @Composable
    private fun WidgetContent(weather: Weather?, targetCityName: String?, size: DpSize) {
        val primaryTextColor = Color.White
        val secondaryTextColor = Color(0xAAFFFFFF)

        val backgroundRes = if (weather != null) {
            when (weather.weatherType) {
                WeatherType.SUNNY -> R.drawable.bg_widget_sunny
                WeatherType.RAINY -> R.drawable.bg_widget_rainy
                WeatherType.SNOWY -> R.drawable.bg_widget_snowy
                WeatherType.CLOUDY -> R.drawable.bg_widget_cloudy
                WeatherType.STORM -> R.drawable.bg_widget_storm
            }
        } else {
            R.drawable.bg_widget_default
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(backgroundRes))
                .clickable(actionStartActivity<MainActivity>())
        ) {
            if (weather != null) {
                val iconRes = when (weather.weatherType) {
                    WeatherType.SUNNY -> {
                        if (weather.isNight()) R.drawable.vd_weather_night_clear
                        else R.drawable.vd_weather_sunny
                    }
                    WeatherType.RAINY -> R.drawable.vd_weather_rainy
                    WeatherType.SNOWY -> R.drawable.vd_weather_snowy
                    WeatherType.CLOUDY -> R.drawable.vd_weather_cloudy
                    WeatherType.STORM -> R.drawable.vd_weather_storm
                }

                // Determine layout according to size constraints
                if (size.height < 80.dp) {
                    if (size.width > 180.dp) {
                        // 1x4 Wide horizontal layout
                        WidgetContentHorizontalWide(weather, iconRes, primaryTextColor, secondaryTextColor)
                    } else {
                        // 1x2 Compact horizontal layout
                        WidgetContentHorizontalCompact(weather, iconRes, primaryTextColor, secondaryTextColor)
                    }
                } else {
                    // 2x2 Vertical layout
                    WidgetContentVertical(weather, iconRes, primaryTextColor, secondaryTextColor)
                }
            } else {
                // Loading / Empty fallback state
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val context = LocalContext.current
                    val text = if (targetCityName != null) {
                        context.getString(R.string.widget_loading_format, targetCityName)
                    } else {
                        context.getString(R.string.widget_no_data)
                    }
                    Text(
                        text = text,
                        style = TextStyle(
                            color = ColorProvider(secondaryTextColor),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun WidgetContentHorizontalCompact(
        weather: Weather,
        iconRes: Int,
        primaryTextColor: Color,
        secondaryTextColor: Color
    ) {
        val context = LocalContext.current
        Row(
            modifier = GlanceModifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weather.cityName,
                    style = TextStyle(
                        color = ColorProvider(primaryTextColor),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = LocalizationUtil.localizeWeatherDesc(weather.weatherDesc, context),
                    style = TextStyle(
                        color = ColorProvider(secondaryTextColor),
                        fontSize = 9.sp
                    )
                )
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = LocalizationUtil.localizeWeatherDesc(weather.weatherDesc, context),
                    modifier = GlanceModifier.size(24.dp)
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = context.getString(R.string.temp_degree_format, weather.tempCurrent.toInt()),
                    style = TextStyle(
                        color = ColorProvider(primaryTextColor),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }

    @Composable
    private fun WidgetContentHorizontalWide(
        weather: Weather,
        iconRes: Int,
        primaryTextColor: Color,
        secondaryTextColor: Color
    ) {
        val context = LocalContext.current
        Row(
            modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: City & Desc
            Column {
                Text(
                    text = weather.cityName,
                    style = TextStyle(
                        color = ColorProvider(primaryTextColor),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = LocalizationUtil.localizeWeatherDesc(weather.weatherDesc, context),
                    style = TextStyle(
                        color = ColorProvider(secondaryTextColor),
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Middle: Icon & Temp
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = LocalizationUtil.localizeWeatherDesc(weather.weatherDesc, context),
                    modifier = GlanceModifier.size(28.dp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = context.getString(R.string.temp_degree_format, weather.tempCurrent.toInt()),
                    style = TextStyle(
                        color = ColorProvider(primaryTextColor),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Right: Additional Stats (Feels like & Humidity)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = context.getString(R.string.widget_feels_like_format, weather.feelsLike.toInt()),
                    style = TextStyle(
                        color = ColorProvider(secondaryTextColor),
                        fontSize = 9.sp
                    )
                )
                Text(
                    text = context.getString(R.string.widget_humidity_format, weather.humidity),
                    style = TextStyle(
                        color = ColorProvider(secondaryTextColor),
                        fontSize = 9.sp
                    )
                )
            }
        }
    }

    @Composable
    private fun WidgetContentVertical(
        weather: Weather,
        iconRes: Int,
        primaryTextColor: Color,
        secondaryTextColor: Color
    ) {
        val context = LocalContext.current
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = weather.cityName,
                style = TextStyle(
                    color = ColorProvider(primaryTextColor),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = LocalizationUtil.localizeWeatherDesc(weather.weatherDesc, context),
                    modifier = GlanceModifier.size(36.dp)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = context.getString(R.string.temp_degree_format, weather.tempCurrent.toInt()),
                    style = TextStyle(
                        color = ColorProvider(primaryTextColor),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = LocalizationUtil.localizeWeatherDesc(weather.weatherDesc, context),
                style = TextStyle(
                    color = ColorProvider(secondaryTextColor),
                    fontSize = 12.sp
                )
            )
        }
    }
}
