package com.boomkin.simpleweather.presentation.weather

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.domain.model.WeatherType
import com.boomkin.simpleweather.ui.theme.WeatherTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ==========================
// Bento Card 容器
// ==========================
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp))
            .padding(16.dp),
        content = content
    )
}

// ==========================
// Bento 网格数据中心
// ==========================
@Composable
fun WeatherStats(
    data: Weather,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val aqi = data.aqi
    val uvIndex = data.uvIndex

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 第一排：风速与湿度
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WindCard(data = data, theme = theme, modifier = Modifier.weight(1f))
            HumidityCard(data = data, theme = theme, modifier = Modifier.weight(1f))
        }

        // 第二排：紫外线与AQI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UvCard(uvIndex = uvIndex, theme = theme, modifier = Modifier.weight(1f))
            AqiCard(aqi = aqi, theme = theme, modifier = Modifier.weight(1f))
        }

        // 第三排：日出日落轨迹 (全宽)
        SolarCycleCard(data = data, theme = theme, modifier = Modifier.fillMaxWidth())
    }
}

// 风速卡片 (包含风车旋转动画与进度条)
@Composable
fun WindCard(
    data: Weather,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val spinDuration = maxOf(1000, (15000 - data.windSpeed * 1500).toInt())
    val infiniteTransition = rememberInfiniteTransition(label = "turbine")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = spinDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    BentoCard(modifier = modifier.height(170.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "风速 / WIND",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${data.windSpeed}",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " m/s",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = if (data.windSpeed > 5.0) "风力较强" else "微风徐徐",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 风车 Canvas 动画
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)

                    // 绘制立杆
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = Offset(w / 2f, h),
                        end = center,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )

                    // 绘制风车叶片
                    drawCircle(color = Color.White, radius = 4f, center = center)
                    val bladeLength = w / 2.2f
                    for (i in 0 until 3) {
                        val angleDeg = rotationAngle + (i * 120f)
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val endX = center.x + cos(angleRad).toFloat() * bladeLength
                        val endY = center.y + sin(angleRad).toFloat() * bladeLength

                        drawLine(
                            color = Color.White.copy(alpha = 0.8f),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 风速进度条
        val progress = minOf(1f, (data.windSpeed / 15f).toFloat())
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(theme.primaryColor)
            )
        }
    }
}

// 湿度卡片 (包含露点计算与水滴液面填充)
@Composable
fun HumidityCard(
    data: Weather,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val dewPoint = (data.tempCurrent - (100 - data.humidity) / 5f).roundToInt()

    BentoCard(modifier = modifier.height(170.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "湿度 / HUMIDITY",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${data.humidity}",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = "露点温度约 ${dewPoint}°C",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 水滴液位容器
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.BottomCenter
            ) {
                val fillRatio = data.humidity / 100f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fillRatio)
                        .background(Color(0x333498DB))
                )
                Text(
                    text = "💧",
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("干燥 Dry", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
                Text("适宜 Optimum", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
                Text("潮湿 Humid", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0x80FBBF24)))
                Box(modifier = Modifier.weight(0.75f).fillMaxHeight().background(Color(0x8034D399)))
                Box(modifier = Modifier.weight(1.25f).fillMaxHeight().background(Color(0xFF3498DB)))
            }
        }
    }
}

// 紫外线卡片 (包含半圆弧仪表盘指示器) — P1#4: 标注 (估算)
@Composable
fun UvCard(
    uvIndex: Double,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val uvLevel = when {
        uvIndex <= 2 -> "极弱"
        uvIndex <= 5 -> "中等"
        uvIndex <= 7 -> "强 (High)"
        else -> "极强 (Very High)"
    }

    BentoCard(modifier = modifier.height(170.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "紫外线 / UV INDEX",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$uvIndex",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " / 11+",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = "紫外线强度：$uvLevel",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 半圆仪表盘
            Box(
                modifier = Modifier
                    .size(64.dp, 48.dp)
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 底部底弧
                    drawArc(
                        color = Color.White.copy(alpha = 0.1f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(4f, 4f),
                        size = Size(w - 8f, (h - 4f) * 2),
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )

                    // 进度指示弧
                    val uvProgress = minOf(1f, (uvIndex / 11.0).toFloat())
                    drawArc(
                        color = theme.primaryColor,
                        startAngle = 180f,
                        sweepAngle = 180f * uvProgress,
                        useCenter = false,
                        topLeft = Offset(4f, 4f),
                        size = Size(w - 8f, (h - 4f) * 2),
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = if (uvIndex >= 6) "建议物理防晒、佩戴墨镜" else "紫外线柔和，适合户外远足",
            color = Color.White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// 空气质量卡片 (AQI 彩色滑动条) — P1#4: 标注 (估算)
@Composable
fun AqiCard(
    aqi: Int,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val aqiStatus = when {
        aqi <= 50 -> "Excellent (优)"
        aqi <= 100 -> "Good (良)"
        else -> "Moderate (轻度)"
    }
    val aqiColor = when {
        aqi <= 50 -> Color(0xFF4ADE80)
        aqi <= 100 -> Color(0xFFFBBF24)
        else -> Color(0xFFEF4444)
    }
    val aqiLabel = when {
        aqi <= 50 -> "优"
        aqi <= 100 -> "良"
        else -> "轻"
    }

    BentoCard(modifier = modifier.height(170.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "空气质量 / AQI",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$aqi",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " AQI",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = aqiStatus,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // AQI 圆圈指标
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(1.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(aqiColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = aqiLabel,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // AQI 三色进度滑动条
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF4ADE80)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFBBF24)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFEF4444)))
                }

                val ratio = minOf(1f, aqi / 150f)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .fillMaxWidth(ratio)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White)
                            .border(1.dp, Color.Black, RoundedCornerShape(5.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0 (优)", color = Color.White.copy(alpha = 0.3f), fontSize = 8.sp)
                Text("100 (良)", color = Color.White.copy(alpha = 0.3f), fontSize = 8.sp)
                Text("150+ (差)", color = Color.White.copy(alpha = 0.3f), fontSize = 8.sp)
            }
        }
    }
}

// 日出日落太阳抛物线轨迹卡片
@Composable
fun SolarCycleCard(
    data: Weather,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val sunriseTime = remember(data.sunrise) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(data.sunrise * 1000))
    }
    val sunsetTime = remember(data.sunset) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(data.sunset * 1000))
    }

    BentoCard(modifier = modifier.height(180.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "日出日落 / SOLAR CYCLE",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 轨迹画布
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 绘制虚线抛物线
                val path = Path().apply {
                    moveTo(10f, h - 20f)
                    quadraticTo(w / 2f, -10f, w - 10f, h - 20f)
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.15f),
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                )

                // 实时太阳进度计算
                val currentSec = System.currentTimeMillis() / 1000
                val progress = if (currentSec < data.sunrise) 0f else if (currentSec > data.sunset) 1f else (currentSec - data.sunrise).toFloat() / (data.sunset - data.sunrise).toFloat()

                // 贝塞尔二次曲线计算 B(t)
                val t = progress
                val p0x = 10f; val p0y = h - 20f
                val p1x = w / 2f; val p1y = -10f
                val p2x = w - 10f; val p2y = h - 20f

                val sunX = (1 - t) * (1 - t) * p0x + 2 * (1 - t) * t * p1x + t * t * p2x
                val sunY = (1 - t) * (1 - t) * p0y + 2 * (1 - t) * t * p1y + t * t * p2y

                drawCircle(
                    color = Color(0xFFFDE68A),
                    radius = 7f,
                    center = Offset(sunX, sunY)
                )
                drawCircle(
                    color = Color(0xFFFDE68A).copy(alpha = 0.3f),
                    radius = 14f,
                    center = Offset(sunX, sunY)
                )

                // 绘制走过的亮色轨迹
                val progressPath = Path().apply {
                    moveTo(p0x, p0y)
                    val ctrlX = (1 - t) * p0x + t * p1x
                    val ctrlY = (1 - t) * p0y + t * p1y
                    quadraticTo(
                        ctrlX,
                        ctrlY,
                        sunX,
                        sunY
                    )
                }
                drawPath(
                    path = progressPath,
                    color = theme.primaryColor.copy(alpha = 0.7f),
                    style = Stroke(width = 2.5f)
                )
            }

            // 日出日落标签时间
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("日出", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                    Text(sunriseTime, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("日落", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                    Text(sunsetTime, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 轨迹说明浮盒
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                val dayLenSec = (data.sunset - data.sunrise).coerceAtLeast(0L)
                val hours = dayLenSec / 3600
                val mins = (dayLenSec % 3600) / 60
                Text(
                    text = "白昼时长约 $hours 小时 $mins 分钟",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "黄金时刻 Golden Hour 在日落前 1 小时",
            color = Color.White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
