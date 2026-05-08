package com.boomkin.simpleweather.presentation.weather


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.boomkin.simpleweather.domain.model.City
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.Weather
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddCityDialog by remember { mutableStateOf(false) }
    var showCityListSheet by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit){
        viewModel.event.collect{ event ->
            when(event) {
                is WeatherEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is WeatherEvent.CityAdded -> {
                    snackbarHostState.showSnackbar("City added")
                }
                is WeatherEvent.CityDeleted -> {
                    snackbarHostState.showSnackbar("City deleted")
                }

                else -> {}
            }
        }
    }
    
    Scaffold(
        snackbarHost = {SnackbarHost(snackbarHostState)},
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showCityListSheet = true }
                    ) {
                        Text(uiState.selectedCity?.name ?: "Weather")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select City")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCityDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add City")
                    }
                    IconButton(onClick = { viewModel.refreshWeather() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.currentWeather == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.currentWeather != null) {
                WeatherContent(
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                EmptyState(
                    onAddCityClick = { showAddCityDialog = true },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (showAddCityDialog) {
                AddCityDialog(
                    onDismiss = { showAddCityDialog = false },
                    onAddCity = { cityName ->
                        viewModel.addCity(cityName)
                        showAddCityDialog = false
                    }
                )
            }

            if (showCityListSheet) {
                CityListSheet(
                    cities = uiState.cities,
                    selectedCity = uiState.selectedCity,
                    onCitySelected = { city ->
                        viewModel.selectCity(city)
                    },
                    onCityDeleted = { city ->
                        viewModel.deleteCity(city)
                    },
                    onSetDefaultCity = { city ->
                        viewModel.setDefaultCity(city)
                    },
                    onDismiss = { showCityListSheet = false }
                )
            }
        }
    }
}

@Composable
fun WeatherContent(
    uiState: WeatherUIState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            uiState.currentWeather?.let { CurrentWeather(it) }
        }
        item {
            Text(
                text = "Hourly Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            HourlyForecastRow(uiState.forecast)
        }
        item {
            Text(
                text = "Daily Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(uiState.forecast) { item ->
            DailyForecastItem(item)
        }
        item {
            HistoryCard(uiState.history)
        }
    }
}

@Composable
fun CurrentWeather(weather: Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = weather.cityName,
                style = MaterialTheme.typography.headlineMedium
            )
            AsyncImage(
                model = weather.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = "${weather.tempCurrent.toInt()}°C",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = weather.weatherDesc,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetail(label = "Humidity", value = "${weather.humidity}%")
                WeatherDetail(label = "Wind", value = "${weather.windSpeed} km/h")
                WeatherDetail(label = "Feels Like", value = "${weather.feelsLike.toInt()}°C")
            }
        }
    }
}

@Composable
fun WeatherDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HourlyForecastRow(forecast: List<ForecastItem>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(forecast) { item ->
            ForecastCard(item)
        }
    }
}

@Composable
fun ForecastCard(item: ForecastItem) {
    Card(
        modifier = Modifier.width(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val timeText = item.dtTxt.split(" ").getOrNull(1)?.take(5) ?: "--:--"
            Text(text = timeText, fontSize = 12.sp)
            AsyncImage(
                model = item.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Text(text = "${item.temp.toInt()}°", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DailyForecastItem(item: ForecastItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = item.dtTxt.split(" ")[0])
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = item.iconUrl, contentDescription = null, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = item.weatherDesc, style = MaterialTheme.typography.bodyMedium)
        }
        Text(text = "${item.tempMin.toInt()}° / ${item.tempMax.toInt()}°")
    }
}

@Composable
fun HistoryCard(history: List<Weather>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Recent History", style = MaterialTheme.typography.titleSmall)
            if (history.isEmpty()) {
                Text(text = "No history available", style = MaterialTheme.typography.bodySmall)
            } else {
                history.forEach { record ->
                    Text(text = "${record.cityName}: ${record.tempCurrent.toInt()}°C", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    onAddCityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No cities have been added yet.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Please add a new city to see weather info.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddCityClick) {
            Text("Add City")
        }
    }
}

@Composable
fun AddCityDialog(onDismiss: () -> Unit, onAddCity: (String) -> Unit){
    var cityName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add City") },
        text = {
            OutlinedTextField(
                value = cityName,
                onValueChange = { cityName = it },
                label = { Text("City Name") },
                placeholder = { Text("e.g. New York")},
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {if(cityName.isNotBlank()) onAddCity(cityName.trim()) },
                enabled = cityName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListSheet(
    cities: List<City>,
    selectedCity: City?,
    onCitySelected: (City) -> Unit,
    onCityDeleted: (City) -> Unit,
    onSetDefaultCity: (City) -> Unit,
    onDismiss: () -> Unit
){
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select a city",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            cities.forEach { city ->
                ListItem(
                    headlineContent = { Text(city.name) },
                    supportingContent = if (city.isDefault) { { Text("Default") } } else null,
                    modifier = Modifier.clickable {
                        onCitySelected(city)
                        onDismiss()
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { onSetDefaultCity(city) }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Set Default",
                                    tint = if (city.isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onCityDeleted(city) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
