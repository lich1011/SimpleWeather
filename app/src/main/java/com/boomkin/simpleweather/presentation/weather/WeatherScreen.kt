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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.ui.theme.WeatherTheme
import com.boomkin.simpleweather.ui.theme.getWeatherTheme
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

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
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
                WeatherParticleBackground(weatherType = weather.weatherType, modifier = Modifier.fillMaxSize())

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
                        CurrentLocationHeader(data = weather, theme = theme, onBackClick = onNavigateToCities)
                    }

                    // 气象巨幅 Hero 卡片
                    StaggeredAnimatedVisibility(visible, 100) {
                        HeroWeatherBanner(data = weather, theme = theme)
                    }

                    // 避险提示卡片
                    StaggeredAnimatedVisibility(visible, 200) {
                        SafetyAdviceCard(data = weather, theme = theme, onNavigateToCities = onNavigateToCities)
                    }

                    // Bento 网格数据中心
                    StaggeredAnimatedVisibility(visible, 300) {
                        WeatherStats(data = weather, theme = theme)
                    }

                    // 24小时逐时预报
                    if (uiState.hourlyForecast.isNotEmpty()) {
                        StaggeredAnimatedVisibility(visible, 400) {
                            HourlyForecastPanel(hourly = uiState.hourlyForecast)
                        }
                    }

                    // 未来 5 天温差图谱
                    if (uiState.forecast.isNotEmpty()) {
                        StaggeredAnimatedVisibility(visible, 500) {
                            DailyForecastPanel(daily = uiState.forecast, theme = theme)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("请先在首页选择或搜索城市记录", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
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
            animationSpec = tween(durationMillis = 500, delayMillis = delayMillis, easing = FastOutSlowInEasing)
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
                    contentDescription = "返回",
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
                Text(
                    text = "🧭",
                    fontSize = 18.sp,
                    modifier = Modifier.rotate(compassAngle)
                )
            }

            Column {
                Text(
                    text = "当前城市 / VIEWING",
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
                    text = data.weatherDesc,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AQI $aqi",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}
