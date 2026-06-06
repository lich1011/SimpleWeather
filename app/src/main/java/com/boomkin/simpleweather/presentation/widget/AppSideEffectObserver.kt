package com.boomkin.simpleweather.presentation.widget

import android.content.Context
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.domain.model.WeatherType
import com.boomkin.simpleweather.domain.repository.WeatherRepository
import com.boomkin.simpleweather.util.AppIconManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSideEffectObserver @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: WeatherRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isStarted = false
    private var lastIconCity: String? = null
    private var lastIconWeatherType: WeatherType? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        if (isStarted) return
        isStarted = true
        Timber.d("AppSideEffectObserver: Starting global weather side-effect observation")

        scope.launch {
            repository.getCities()
                .flatMapLatest { cities ->
                    val defaultCity = cities.firstOrNull { it.isDefault } ?: cities.firstOrNull()
                    if (defaultCity != null) {
                        repository.getCachedWeatherDataFlow(defaultCity.name)
                            .map { it?.weather to defaultCity.name }
                    } else {
                        flowOf(null to null)
                    }
                }
                .collect { (weather, cityName) ->
                    if (weather != null) {
                        if(cityName!=lastIconCity || weather.weatherType!= lastIconWeatherType){
                            Timber.d("AppSideEffectObserver: Default weather type change for ${weather.cityName} -> ${weather.weatherType}.Update launcher icon.")
                            AppIconManager.updateAppIcon(context, weather.weatherType)
                            lastIconCity = cityName
                            lastIconWeatherType = weather.weatherType
                        }
                    } else {
                        Timber.d("AppSideEffectObserver: No default weather data for $cityName. Widget will show fallback.")
                        lastIconCity = null
                        lastIconWeatherType = null
                    }
                    
                    // 2. Trigger widget update via state updater
                    try {
                        WeatherWidgetStateUpdater.updateWidgetState(context, weather, cityName)
                    } catch (e: Exception) {
                        Timber.e(e, "AppSideEffectObserver: Failed to update Glance widget state")
                    }
                }
        }
    }
}
