package com.boomkin.simpleweather.presentation.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boomkin.simpleweather.domain.model.ForecastItem
import com.boomkin.simpleweather.domain.model.HourlyForecastItem
import com.boomkin.simpleweather.ui.theme.WeatherTheme
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
                text = "24小时逐时预报 / HOURLY TIMELINE",
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
                        ForecastIcon(desc = hour.weatherDesc, fontSize = 16f)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${hour.temperature.toInt()}°",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "💧${hour.humidity}%",
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

@Composable
fun ForecastIcon(
    desc: String,
    modifier: Modifier = Modifier,
    fontSize: Float = 20f
) {
    val emoji = when {
        desc.contains("雨", ignoreCase = true) || desc.contains("rain", ignoreCase = true) -> "🌧️"
        desc.contains("雪", ignoreCase = true) || desc.contains("snow", ignoreCase = true) -> "❄️"
        desc.contains("雷", ignoreCase = true) || desc.contains("storm", ignoreCase = true) -> "⛈️"
        desc.contains("晴", ignoreCase = true) || desc.contains("clear", ignoreCase = true) || desc.contains("sun", ignoreCase = true) -> "☀️"
        desc.contains("多云", ignoreCase = true) || desc.contains("cloud", ignoreCase = true) -> "⛅"
        else -> "☁️"
    }
    Text(
        text = emoji,
        fontSize = fontSize.sp,
        modifier = modifier
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
                text = "未来 5 天降水与温差 / 5-DAY GRAPH",
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
                        ForecastIcon(desc = day.weatherDesc, fontSize = 14f)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = day.weatherDesc,
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
                            text = "${day.tempMin.toInt()}°",
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
                            text = "${day.tempMax.toInt()}°",
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
        val weekDays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return dateStr
        val cal = Calendar.getInstance().apply { time = date }
        weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1]
    } catch (_: Exception) {
        dateStr
    }
}
