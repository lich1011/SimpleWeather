package com.boomkin.simpleweather.presentation.weather

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun WeatherNavigation(viewModel: WeatherViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = "city_list") {
        composable("city_list") {
            CityListScreen(
                uiState = uiState,
                onCityClick = { cityName ->
                    val city = uiState.cities.find { it.name == cityName }
                    if (city != null) {
                        viewModel.selectCity(city)
                        navController.navigate("city_detail/${city.name}")
                    }
                },
                onAddCityClick = { cityName ->
                    viewModel.addCity(cityName)
                }
            )
        }

        composable("city_detail/{cityName}") { backStackEntry ->
            val cityName = backStackEntry.arguments?.getString("cityName")
            
            // If we somehow got here without selecting, try to select
            if (uiState.selectedCity?.name != cityName && cityName != null) {
                val city = uiState.cities.find { it.name == cityName }
                if (city != null) {
                    viewModel.selectCity(city)
                }
            }

            CityDetailScreen(
                uiState = uiState,
                onBackClick = {
                    navController.popBackStack()
                },
                onRefresh = {
                    viewModel.refreshWeather()
                }
            )
        }
    }
}
