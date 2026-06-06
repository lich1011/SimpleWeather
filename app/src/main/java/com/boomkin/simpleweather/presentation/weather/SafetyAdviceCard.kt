package com.boomkin.simpleweather.presentation.weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boomkin.simpleweather.domain.model.Weather
import com.boomkin.simpleweather.domain.model.WeatherType
import com.boomkin.simpleweather.ui.theme.WeatherTheme

// ==========================
// 避险提示卡片
// ==========================
@Composable
fun SafetyAdviceCard(
    data: Weather,
    theme: WeatherTheme,
    onNavigateToCities: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = when (data.weatherType) {
        WeatherType.STORM -> Color(0x334E1D7C)
        WeatherType.SNOWY -> Color(0x331E3D59)
        else -> Color.White.copy(alpha = 0.05f)
    }

    val cardBorder = when (data.weatherType) {
        WeatherType.STORM -> Color(0x339B59B6)
        WeatherType.SNOWY -> Color(0x33AED6F1)
        else -> Color.White.copy(alpha = 0.1f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(30.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("ℹ️", fontSize = 12.sp)
                Text(
                    text = "Android 智能避险提示",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            Text("⚠️", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
        }

        val adviceText = when (data.weatherType) {
            WeatherType.STORM -> "强雷电大风预警： 当前风力达 ${data.windSpeed}m/s 并伴有强雷暴闪电。请尽快寻找坚固遮蔽场所，避免户外运动、水边久留并注意雷电避险。"
            WeatherType.SNOWY -> "大雪结冰预警： 室外体感降至 ${data.feelsLike.toInt()}°C。路面积雪较深并有严重打滑现象，驾车请安装防滑链、减速慢行，老人小孩减少出门。"
            WeatherType.SUNNY -> "紫外线极强提示： 阳光暴晒，建议涂抹物理防护霜、佩戴墨镜出行。多喝温水防止脱水，适合在清晨或傍晚散心。"
            WeatherType.RAINY -> "微风小雨提示： 细雨连绵，相对湿度 ${data.humidity}%。出行记得携带好雨具，小心地面湿滑，雨天视线较差驾车需特别小心。"
            WeatherType.CLOUDY -> "阴天多云气象： 光线十分温和柔雅，紫外线较弱。空气质量良好，非常适合户外散心、远足慢跑或去公园游玩放松。"
        }

        Text(
            text = adviceText,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Button(
            onClick = onNavigateToCities,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.05f),
                contentColor = Color.White.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "切换其他区域气候",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "↗️", fontSize = 11.sp)
            }
        }
    }
}
