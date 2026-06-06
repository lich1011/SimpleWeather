package com.boomkin.simpleweather.presentation.weather

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.boomkin.simpleweather.ui.theme.getWeatherColorScheme

@Composable
fun WeatherNavigation(viewModel: WeatherViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by rememberSaveable { mutableStateOf("home") }

    val weather = uiState.currentWeather
    val colorScheme = if (weather != null) getWeatherColorScheme(weather.weatherType) else MaterialTheme.colorScheme

    MaterialTheme(colorScheme = colorScheme) {
        val backgroundColor = Color(0xFF07090E)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // Crossfade screen switcher with spring transition
            Crossfade(
                targetState = currentTab,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "screenTransition"
            ) { tab ->
                when (tab) {
                    "home" -> {
                        CityListScreen(
                            uiState = uiState,
                            onCityClick = { cityName ->
                                val city = uiState.cities.find { it.name == cityName }
                                if (city != null) {
                                    viewModel.selectCity(city)
                                    currentTab = "cities"
                                }
                            },
                            onAddCityClick = { cityName -> viewModel.addCity(cityName) },
                            onReactivateCity = { city -> viewModel.reactivateCity(city) },
                            onDeleteCity = { city -> viewModel.deleteCity(city) }
                        )
                    }
                    "cities" -> {
                        if (weather != null) {
                            WeatherHomeScreen(
                                uiState = uiState,
                                onRefresh = { viewModel.refreshWeather() },
                                onNavigateToCities = { currentTab = "home" }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("请先在首页选择或搜索添加城市记录", color = Color.White.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }
}
