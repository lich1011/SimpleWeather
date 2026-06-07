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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.boomkin.simpleweather.R
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = stringResource(R.string.safety_advice_title),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_warning),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(12.dp)
            )
        }

        val adviceText = when (data.weatherType) {
            WeatherType.STORM -> stringResource(R.string.safety_advice_storm, data.windSpeed)
            WeatherType.SNOWY -> stringResource(
                R.string.safety_advice_snowy,
                data.feelsLike.toInt()
            )

            WeatherType.SUNNY -> stringResource(R.string.safety_advice_sunny)
            WeatherType.RAINY -> stringResource(R.string.safety_advice_rainy, data.humidity)
            WeatherType.CLOUDY -> stringResource(R.string.safety_advice_cloudy)
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
                    text = stringResource(R.string.switch_city_button),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_outward),
                    contentDescription = null,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
