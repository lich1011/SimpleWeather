package com.boomkin.simpleweather.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boomkin.simpleweather.domain.model.City
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.HourlyForecastItem
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class WeatherUIState(
    val cities: List<City> = emptyList(),
    val archivedCities: List<City> = emptyList(),
    val selectedCity: City? = null,
    val currentWeather: Weather? = null,
    val forecast: List<ForecastItem> = emptyList(),
    val hourlyForecast: List<HourlyForecastItem> = emptyList(),
    val history: List<Weather> = emptyList(),
    val cityWeathers: Map<String, Weather> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUIState())
    val uiState: StateFlow<WeatherUIState> = _uiState.asStateFlow()

    private var historyJob: Job? = null
    private var cachedDataJob: Job? = null

    // Track per-city cache observer Jobs to prevent Flow leak (issue #4)
    private val cityObserverJobs = mutableMapOf<String, Job>()

    init {
        observeCities()
        observeArchivedCities()
    }

    private fun observeArchivedCities() {
        repository.getArchivedCities().onEach { archived ->
            _uiState.update { it.copy(archivedCities = archived) }
        }.launchIn(viewModelScope)
    }

    private fun observeCities() {
        repository.getCities().onEach { cities ->
            _uiState.update { it.copy(cities = cities) }
            
            // Only create observers for NEW cities, skip existing ones
            val currentCityNames = cities.map { it.name }.toSet()
            
            // Cancel observers for removed cities
            val removedCities = cityObserverJobs.keys - currentCityNames
            removedCities.forEach { name ->
                cityObserverJobs.remove(name)?.cancel()
            }
            
            // Add observers for new cities only
            cities.forEach { city ->
                if (!cityObserverJobs.containsKey(city.name)) {
                    observeCachedCityWeather(city.name)
                }
            }
            
            val selected = _uiState.value.selectedCity
                ?: cities.firstOrNull { it.isDefault }
                ?: cities.firstOrNull()
            if (selected != null && _uiState.value.selectedCity?.name != selected.name) {
                selectCity(selected)
            }
        }.launchIn(viewModelScope)
    }

    private fun observeCachedCityWeather(cityName: String) {
        val job = repository.getCachedWeatherDataFlow(cityName).onEach { data ->
            if (data != null) {
                _uiState.update { 
                    val newMap = it.cityWeathers.toMutableMap()
                    newMap[cityName] = data.weather
                    it.copy(cityWeathers = newMap)
                }
            }
        }.launchIn(viewModelScope)
        cityObserverJobs[cityName] = job
    }

    fun selectCity(city: City) {
        Timber.d("Selected city changed to: ${city.name}")
        _uiState.update { it.copy(selectedCity = city) }
        observeSelectedCityCache(city)
        observeWeatherHistory(city.name)
        
        // Automatically make the last selected city the default so it displays on the widget
        setDefaultCity(city)
        
        // Also trigger a network refresh in the background if we want the absolute latest,
        // or rely on pull-to-refresh to do that. We'll do a silent refresh.
        fetchWeatherSilently(city)
    }

    private fun observeSelectedCityCache(city: City) {
        cachedDataJob?.cancel()
        cachedDataJob = repository.getCachedWeatherDataFlow(city.name).onEach { data ->
            if (data != null) {
                _uiState.update {
                    it.copy(
                        currentWeather = data.weather,
                        forecast = data.dailyForecast,
                        hourlyForecast = data.hourlyForecast
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeWeatherHistory(cityName: String) {
        historyJob?.cancel()
        historyJob = repository.getWeatherHistory(cityName)
            .onEach{ history ->
                _uiState.update { it.copy(history = history) }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchWeatherSilently(city: City) {
        viewModelScope.launch {
            val lastUpdateTime = repository.getLastUpdateTime(city.name)
            val diff = System.currentTimeMillis() - lastUpdateTime
            if (diff <= 5 * 60 * 1000) {
                Timber.d("Skip silent refresh for ${city.name}, cache age is ${diff / 1000}s (threshold 300s)")
                return@launch
            }
            repository.getWeatherData(city)
                .onSuccess {
                    Timber.d("Silent refresh success for ${city.name}")
                }
                .onFailure { e ->
                    Timber.e(e, "Silent refresh failed for ${city.name}")
                }
        }
    }

    fun refreshWeather(){
        val city = _uiState.value.selectedCity ?: return
        Timber.d("Manual refresh triggered for city: ${city.name}")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getWeatherData(city).onSuccess { data ->
                Timber.i("Successfully manually refreshed weather data for ${city.name}")
                repository.saveWeatherRecords(data.weather)
            }.onFailure { e ->
                Timber.e(e, "Failed to fetch weather data for ${city.name}")
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to fetch weather") }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun addCity(cityName: String) {
        Timber.d("User initiated adding city: $cityName")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.addCity(cityName)
                .onSuccess { city ->
                    Timber.i("Successfully added city: $cityName")
                    fetchWeatherSilently(city)
                }
                .onFailure {
                    Timber.e(it, "Failed to add city: $cityName")
                    _uiState.update { state -> state.copy(errorMessage = it.message ?: "Failed to add city") }
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun reactivateCity(city: City) {
        Timber.d("User reactivated city from history: ${city.name}")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.reactivateCity(city)
            fetchWeatherSilently(city)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun deleteCity(city: City) {
        Timber.d("User deleted city: ${city.name}")
        viewModelScope.launch {
            repository.deleteCity(city)
            _uiState.update {
                val newMap = it.cityWeathers.toMutableMap()
                newMap.remove(city.name)
                it.copy(cityWeathers = newMap)
            }
            if(_uiState.value.selectedCity?.id == city.id) {
                _uiState.update {
                    it.copy(selectedCity = null, currentWeather = null, forecast = emptyList(), hourlyForecast = emptyList())
                }
            }
        }
    }

    fun setDefaultCity(city: City) {
        viewModelScope.launch {
            repository.setDefaultCity(city.id)
        }
    }

    fun clearError(){
        _uiState.update { it.copy(errorMessage = null) }
    }
}
