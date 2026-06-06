package com.boomkin.simpleweather.presentation.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boomkin.simpleweather.domain.model.City
import com.boomkin.simpleweather.domain.model.WeatherType
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListScreen(
    uiState: WeatherUIState,
    onCityClick: (String) -> Unit,
    onAddCityClick: (String) -> Unit,
    onReactivateCity: (City) -> Unit,
    onDeleteCity: (City) -> Unit,
    onSetDefaultCity: (City) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Search block
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "城市查询管理器 / FIND CITIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                // Glassmorphism search capsule
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔍", fontSize = 14.sp, color = Color.White.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "搜索并添加城市 (Sanya, Harbin, Beijing...)",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.isNotBlank()) {
                                        onAddCityClick(searchQuery)
                                        searchQuery = ""
                                    }
                                }
                            )
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = "添加",
                            color = Color(0xFF34D399),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    onAddCityClick(searchQuery)
                                    searchQuery = ""
                                }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // 2. Preset list block
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "内置区域气象台 / PRESET STATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                if (uiState.cities.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无城市记录", color = Color.White.copy(alpha = 0.4f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(uiState.cities, key = { it.id }) { city ->
                            val weather = uiState.cityWeathers[city.name]
                            val isActive = uiState.selectedCity?.id == city.id
                            
                            val cardBg = if (isActive) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f)
                            val cardBorder = if (isActive) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                                    .clickable { onCityClick(city.name) }
                            ) {
                                if (weather != null) {
                                    WeatherParticleBackground(
                                        weatherType = weather.weatherType,
                                        modifier = Modifier.matchParentSize()
                                    )
                                    // Subtle overlay for readability
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(Color.Black.copy(alpha = 0.2f))
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Weather Emoji Box
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isActive) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f))
                                                .border(1.dp, if (isActive) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val emoji = when (weather?.weatherType) {
                                                WeatherType.SUNNY -> "☀️"
                                                WeatherType.RAINY -> "🌧️"
                                                WeatherType.SNOWY -> "❄️"
                                                WeatherType.CLOUDY -> "☁️"
                                                WeatherType.STORM -> "⛈️"
                                                null -> "☁️"
                                            }
                                            Text(text = emoji, fontSize = 18.sp)
                                        }

                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = city.name,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (isActive) {
                                                    Text(
                                                        text = "✔️",
                                                        color = Color(0xFF34D399),
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                if(city.isDefault){
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0x33FBBF24))
                                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                                    ){
                                                        Text(
                                                            text = "默认",
                                                            color = Color(0xFFFBBF24),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (weather != null) "${weather.weatherDesc} • ${weather.weatherType.name}" else "加载中...",
                                                color = Color.White.copy(alpha = 0.4f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                        }
                                    }

                                    if (weather != null) {
                                        val aqi = weather.aqi
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${weather.tempCurrent.toInt()}°",
                                                    color = Color.White,
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Light
                                                )
                                                Text(
                                                    text = "AQI $aqi",
                                                    color = Color.White.copy(alpha = 0.3f),
                                                    fontSize = 9.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                            }

                                            Text(
                                                text = if (city.isDefault) "X" else "x",
                                                fontSize = 18.sp,
                                                color = if (city.isDefault) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.45f),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable(enabled = !city.isDefault){onSetDefaultCity(city)}
                                                    .padding(4.dp)
                                            )

                                            // Delete Button
                                            IconButton(
                                                onClick = { onDeleteCity(city) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "删除",
                                                    tint = Color.White.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White.copy(alpha = 0.3f),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}