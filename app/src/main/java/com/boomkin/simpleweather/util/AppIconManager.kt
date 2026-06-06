package com.boomkin.simpleweather.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.boomkin.simpleweather.domain.model.WeatherType
import timber.log.Timber

object AppIconManager {

    private const val BASE_ALIAS = "com.boomkin.simpleweather.MainActivity"

    fun updateAppIcon(context: Context, weatherType: WeatherType) {
        val packageManager = context.packageManager

        val targetAlias = when (weatherType) {
            WeatherType.SUNNY -> "${BASE_ALIAS}Sunny"
            WeatherType.RAINY -> "${BASE_ALIAS}Rainy"
            WeatherType.SNOWY -> "${BASE_ALIAS}Snowy"
            WeatherType.CLOUDY -> "${BASE_ALIAS}Cloudy"
            WeatherType.STORM -> "${BASE_ALIAS}Storm"
        }

        val aliases = listOf(
            "${BASE_ALIAS}Sunny",
            "${BASE_ALIAS}Rainy",
            "${BASE_ALIAS}Snowy",
            "${BASE_ALIAS}Cloudy",
            "${BASE_ALIAS}Storm"
        )

        Timber.d("AppIconManager: Changing icon to match weather: $weatherType (targetAlias: $targetAlias)")

        try {
            for (alias in aliases) {
                val componentName = ComponentName(context, alias)
                val newSetting = if (alias == targetAlias) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }

                val currentSetting = packageManager.getComponentEnabledSetting(componentName)
                if (currentSetting != newSetting) {
                    packageManager.setComponentEnabledSetting(
                        componentName,
                        newSetting,
                        PackageManager.DONT_KILL_APP
                    )
                    Timber.d("AppIconManager: Updated component $alias to state $newSetting")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "AppIconManager: Failed to update app icon component state")
        }
    }
}
