package com.boomkin.simpleweather.presentation.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boomkin.simpleweather.R
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.HourlyForecastItem
import com.boomkin.simpleweather.ui.theme.WeatherTheme
import com.boomkin.simpleweather.util.LocalizationUtil
import java.text.SimpleDateFormat
import java.util.*

// ==========================
// 24小时逐时预报
// ==========================
@Composable
fun HourlyForecastPanel(hourly: List<HourlyForecastItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.hourly_timeline_title),
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            itemsIndexed(hourly) { _, hour ->
                val displayTime = hour.time.substringAfter("T").take(5)
                val hourVal = try {
                    hour.time.substringAfter("T").take(2).toInt()
                } catch (_: Exception) {
                    12
                }
                val isNight = hourVal < 6 || hourVal >= 18

                Column(
                    modifier = Modifier
                        .width(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = displayTime,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ForecastIcon(desc = hour.weatherDesc, isNight = isNight, sizeDp = 16f)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.temp_degree_format, hour.temperature.toInt()),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                   Row(
                       verticalAlignment = Alignment.CenterVertically,
                       horizontalArrangement = Arrangement.spacedBy(1.dp)
                   ) {
                       Icon(
                           painter = painterResource(id = R.drawable.ic_drop),
                           contentDescription = null,
                           tint = Color(0xFF7DD3FC),
                           modifier = Modifier.size(8.dp)
                       )

                       Text(
                           text = stringResource(R.string.humidity_percent_format, hour.humidity),
                           color = Color(0xFF7DD3FC),
                           fontSize = 8.sp,
                           fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                           fontWeight = FontWeight.Medium
                       )
                   }
                }
            }
        }
    }
}

@Composable
fun ForecastIcon(
    desc: String,
    modifier: Modifier = Modifier,
    isNight: Boolean = false,
    sizeDp: Float = 20f
) {
    val iconRes = when {
        desc.contains("雨", ignoreCase = true) || desc.contains("rain", ignoreCase = true) -> R.drawable.vd_weather_rainy
        desc.contains("雪", ignoreCase = true) || desc.contains("snow", ignoreCase = true) -> R.drawable.vd_weather_snowy
        desc.contains("雷", ignoreCase = true) || desc.contains("storm", ignoreCase = true) -> R.drawable.vd_weather_storm
        desc.contains("晴", ignoreCase = true) || desc.contains("clear", ignoreCase = true) || desc.contains("sun", ignoreCase = true) -> {
            if (isNight) R.drawable.vd_weather_night_clear
            else R.drawable.vd_weather_sunny
        }
        desc.contains("多云", ignoreCase = true) || desc.contains("cloud", ignoreCase = true) -> R.drawable.vd_weather_cloudy
        else -> R.drawable.vd_weather_cloudy
    }
    Image(
        painter = painterResource(iconRes),
        contentDescription = desc,
        modifier = modifier.size(sizeDp.dp)
    )
}

// ==========================
// 未来 5 天降水与温差
// ==========================
@Composable
fun DailyForecastPanel(
    daily: List<ForecastItem>,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.five_day_graph_title),
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            daily.take(5).forEach { day ->
                val weekDay = parseDayOfWeek(day.dtTxt)
                val dateMonth = day.dtTxt.substringAfter("-")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 日期描述
                    Column(modifier = Modifier.width(64.dp)) {
                        Text(
                            text = weekDay,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateMonth,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp
                        )
                    }

                    // 图标与天气描述
                    Row(
                        modifier = Modifier.width(88.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        ForecastIcon(desc = day.weatherDesc, sizeDp = 14f)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LocalizationUtil.localizeWeatherDesc(day.weatherDesc),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 渐变温差条 — P1#7: 修复二次缩放 Bug
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = stringResource(R.string.temp_degree_format, day.tempMin.toInt()),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.width(24.dp),
                            textAlign = TextAlign.End
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Canvas(modifier = Modifier.width(64.dp).height(4.dp)) {
                            val tempSpan = 45f // 温差标准分母
                            val minLeft = ((day.tempMin.toFloat() + 15f) / tempSpan).coerceIn(0f, 1f)
                            val maxRight = ((day.tempMax.toFloat() + 15f) / tempSpan).coerceIn(0f, 1f)
                            val leftX = minLeft * size.width
                            val rightX = maxRight * size.width

                            // 绘制槽线底色
                            drawLine(
                                color = Color.White.copy(alpha = 0.1f),
                                start = Offset(0f, size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = size.height,
                                cap = StrokeCap.Round
                            )

                            // 绘制动态温差渐变色条 — 修正: leftX/rightX 已是像素坐标，不再乘 size.width
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(theme.primaryColor, Color.White)
                                ),
                                start = Offset(leftX, size.height / 2f),
                                end = Offset(maxOf(leftX + 4f, rightX), size.height / 2f),
                                strokeWidth = size.height,
                                cap = StrokeCap.Round
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stringResource(R.string.temp_degree_format, day.tempMax.toInt()),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.width(24.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private fun parseDayOfWeek(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return dateStr
        val cal = Calendar.getInstance().apply { time = date }
        cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: dateStr
    } catch (_: Exception) {
        dateStr
    }
}
