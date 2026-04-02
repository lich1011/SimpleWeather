package com.example.myapplication.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.City
import com.example.myapplication.domain.model.ForecastItem
import com.example.myapplication.domain.model.Weather
import com.example.myapplication.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUIState(
    val cities: List<City> = emptyList(),
    val selectedCity: City? = null,
    val currentWeather: Weather? = null,
    val forecast: List<ForecastItem> = emptyList(),
    val history: List<Weather> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class WeatherEvent {

    data class ShowError(val message: String) : WeatherEvent()
    object CityAdded : WeatherEvent()
    object CityDeleted : WeatherEvent()
    object CitySelected : WeatherEvent()
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUIState())
    val uiState: StateFlow<WeatherUIState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<WeatherEvent>()
    val event: SharedFlow<WeatherEvent> = _event.asSharedFlow()


    init {
//        observeSavedCities()
//        observeWeatherHistory()
        observeCities()
    }

    private fun observeCities() {
        repository.getCities().onEach {
            cities ->
            _uiState.update { it.copy(cities = cities) }
            val selected =_uiState.value.selectedCity
                ?: cities.firstOrNull { it.isDefault}
                ?: cities.firstOrNull()
            if(selected!=null && _uiState.value.selectedCity?.name != selected.name) {
                selectCity(selected)
            }
        }.launchIn(viewModelScope)
    }

    fun selectCity(city: City) {
        _uiState.update { it.copy(selectedCity = city) }
        fetchWeather(city.name)
        observeWeatherHistory(city.name)
    }

    private fun observeSavedCities() {
        viewModelScope.launch {
            repository.getCities().collect { savedCities ->
                _uiState.update { it.copy(cities = savedCities) }
                // 自动加载默认城市或第一个城市的天气
                val cityToLoad = savedCities.find { it.isDefault } ?: savedCities.firstOrNull()
                cityToLoad?.let {
                    fetchWeather(it.name)
                }
            }
        }
    }

//    private fun observeWeatherHistory() {
//        viewModelScope.launch {
//            repository.getWeatherHistory().collect { history ->
//                _uiState.update { it.copy(history = history) }
//            }
//        }
//    }

    private fun observeWeatherHistory(cityName: String) {
        repository.getWeatherHistory(cityName)
            .onEach{ history ->
                _uiState.update { it.copy(history = history) }
            }
            .launchIn(viewModelScope)
    }

    fun fetchWeather(cityName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val weatherResult = repository.getCurrentWeather(cityName)
            weatherResult.onSuccess { weather ->
                _uiState.update { it.copy(currentWeather = weather) }
                repository.saveWeatherRecords(weather)
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to fetch weather") }
                _event.emit(WeatherEvent.ShowError(e.message ?: "Failed to fetch weather"))
            }

            val forecastResult = repository.getForecast(cityName)
            forecastResult.onSuccess { forecast ->
                _uiState.update { it.copy(forecast = forecast) }
            }
//                .onFailure { e ->
//                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to fetch forecast") }
//            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun refreshWeather(){
        val city = _uiState.value.selectedCity ?: return
        fetchWeather(city.name)

    }

    fun addCity(cityName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.addCity(cityName)
                .onSuccess {
                    _event.emit(WeatherEvent.CityAdded)
                }
                .onFailure {
                    _event.emit(WeatherEvent.ShowError(it.message ?: "Failed to add city"))
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun deleteCity(city: City) {
        viewModelScope.launch {
            repository.deleteCity(city)
            _event.emit(WeatherEvent.CityDeleted)
            if(_uiState.value.selectedCity?.id == city.id) {
                _uiState.update {
                    it.copy(selectedCity = null, currentWeather = null, forecast = emptyList())
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
