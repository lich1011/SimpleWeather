package com.boomkin.simpleweather.presentation.weather

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import com.boomkin.simpleweather.R
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.domain.model.isNight
import com.boomkin.simpleweather.ui.theme.WeatherTheme
import com.boomkin.simpleweather.ui.theme.getWeatherTheme
import com.boomkin.simpleweather.util.LocalizationUtil
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherHomeScreen(
    uiState: WeatherUIState,
    onRefresh: () -> Unit,
    onNavigateToCities: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it.asString(context))
        }
    }

    // 控制入场渐显动效
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.currentWeather) {
        if (uiState.currentWeather != null) {
            visible = false
            delay(50)
            visible = true
        }
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        val weather = uiState.currentWeather
        if (weather != null) {
            val theme = remember(weather.weatherType) { getWeatherTheme(weather.weatherType) }

            Box(modifier = Modifier.fillMaxSize()) {
                // 1. 底层沉浸式天气粒子背景
                WeatherParticleBackground(
                    weatherType = weather.weatherType,
                    isNight = weather.isNight(),
                    modifier = Modifier.fillMaxSize()
                )

                // 2. 气象色彩渐变叠加层
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    theme.gradientStart.copy(alpha = 0.85f),
                                    theme.gradientEnd.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Bottom Sheet State
                var showBottomSheet by rememberSaveable { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                // 3. 内容滚动区
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .statusBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 首页头部定位栏
                    StaggeredAnimatedVisibility(visible, 0) {
                        CurrentLocationHeader(
                            data = weather,
                            theme = theme,
                            onBackClick = onNavigateToCities
                        )
                    }

                    // 气象巨幅 Hero 卡片
                    StaggeredAnimatedVisibility(visible, 100) {
                        HeroWeatherBanner(data = weather, theme = theme)
                    }

                    // 今日气象详情折叠卡片
                    StaggeredAnimatedVisibility(visible, 200) {
                        TodayDetailsCard(
                            data = weather,
                            theme = theme,
                            onClick = { showBottomSheet = true }
                        )
                    }

                    // 未来 5 天温差图谱
                    if (uiState.forecast.isNotEmpty()) {
                        StaggeredAnimatedVisibility(visible, 300) {
                            DailyForecastPanel(daily = uiState.forecast, theme = theme)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Modal Bottom Sheet displaying collapsed details
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState,
                        containerColor = Color(0xFF0F172A).copy(alpha = 0.95f),
                        scrimColor = Color.Black.copy(alpha = 0.6f),
                        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            SafetyAdviceCard(
                                data = weather,
                                theme = theme,
                                onNavigateToCities = {
                                    showBottomSheet = false
                                    onNavigateToCities()
                                }
                            )

                            WeatherStats(data = weather, theme = theme)

                            if (uiState.hourlyForecast.isNotEmpty()) {
                                HourlyForecastPanel(hourly = uiState.hourlyForecast)
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.select_city_prompt),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ==========================
// 动效入场包装器
// ==========================
@Composable
fun StaggeredAnimatedVisibility(
    visible: Boolean,
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { 80 },
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(durationMillis = 500, delayMillis = delayMillis)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

// ==========================
// 定位头部卡片 — P2#13: 修正文案 + P1#4/#5: AQI 标注 ≈
// ==========================
@Composable
fun CurrentLocationHeader(
    data: Weather,
    theme: WeatherTheme,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "compass")
    val compassAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val aqi = data.aqi

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button),
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_compass),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(compassAngle)
                )
            }

            Column {
                Text(
                    text = stringResource(R.string.viewing_city_label),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = theme.primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = data.cityName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = LocalizationUtil.localizeWeatherDesc(data.weatherDesc),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (aqi != null) stringResource(
                    R.string.aqi_format,
                    aqi
                ) else stringResource(R.string.aqi_unavailable_short),
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
fun TodayDetailsCard(
    data: Weather,
    theme: WeatherTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.today_details_title),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Text(
                    text = stringResource(R.string.expand_more_label),
                    color = theme.primaryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stats = listOf(
                    Triple(
                        painterResource(R.drawable.vd_weather_sunny),
                        stringResource(R.string.temp_celsius_format, data.tempCurrent.toInt()),
                        stringResource(R.string.temp_label)
                    ),
                    Triple(
                        painterResource(R.drawable.ic_thermometer),
                        stringResource(R.string.temp_celsius_format, data.feelsLike.toInt()),
                        stringResource(R.string.feels_like_label)
                    ),
                    Triple(
                        painterResource(R.drawable.ic_drop),
                        stringResource(R.string.humidity_percent_format, data.humidity),
                        stringResource(R.string.humidity_label)
                    ),
                    Triple(
                        painterResource(R.drawable.ic_sparkle),
                        data.aqi?.toString() ?: "-",
                        stringResource(R.string.aqi_label)
                    )
                )

                stats.forEach { (painter, value, label) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painter,
                            contentDescription = null,
                            tint = if (painter == painterResource(R.drawable.vd_weather_sunny)) theme.primaryColor else Color.White.copy(
                                alpha = 0.8f
                            ),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = value,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}
