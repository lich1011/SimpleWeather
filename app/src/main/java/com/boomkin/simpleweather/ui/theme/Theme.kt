package com.boomkin.simpleweather.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boomkin.simpleweather.domain.model.WeatherType

// 1. 各天气的色彩基因定义
val AmberGold = Color(0xFFF5B041)   // Sunny 晴
val SkyBlue = Color(0xFF3498DB)     // Rainy 雨
val SoftWhite = Color(0xFFAED6F1)    // Snowy 雪
val MutedSlate = Color(0xFF85929E)   // Cloudy 云
val StormPurple = Color(0xFF9B59B6)  // Storm 雷雨

// 2. 动态获取 Material 3 主体配色方案
fun getWeatherColorScheme(weatherType: WeatherType): ColorScheme {
    val primaryColor = when (weatherType) {
        WeatherType.SUNNY -> AmberGold
        WeatherType.RAINY -> SkyBlue
        WeatherType.SNOWY -> SoftWhite
        WeatherType.CLOUDY -> MutedSlate
        WeatherType.STORM -> StormPurple
    }
    
    return darkColorScheme(
        primary = primaryColor,
        onPrimary = Color.Black,
        surface = Color(0xFF121620),
        onSurface = Color(0xFFF0F4F9),
        background = Color(0xFF07090E),
        onBackground = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFF1E2535),
        onSurfaceVariant = Color(0xFFCBD5E1)
    )
}

// 3. Skyflow 气象主题及色板基因映射
data class WeatherTheme(
    val type: WeatherType,
    val label: String,
    val cnLabel: String,
    val primaryColor: Color,
    val containerBg: Color,
    val accentBg: Color,
    val textColor: Color,
    val gradientStart: Color,
    val gradientEnd: Color
)

val WEATHER_THEMES = mapOf(
    WeatherType.SUNNY to WeatherTheme(
        type = WeatherType.SUNNY,
        label = "Sunny",
        cnLabel = "晴朗",
        primaryColor = Color(0xFFF5B041),
        containerBg = Color(0x1AF5B041), // bg-amber-500/10
        accentBg = Color(0x33FBBF24),    // bg-amber-400/20
        textColor = Color(0xFFFEF3C7),   // text-amber-100
        gradientStart = Color(0x33D97706), // from-amber-600/20
        gradientEnd = Color(0x1A78350F)    // to-amber-900/10
    ),
    WeatherType.RAINY to WeatherTheme(
        type = WeatherType.RAINY,
        label = "Rainy",
        cnLabel = "小雨",
        primaryColor = Color(0xFF3498DB),
        containerBg = Color(0x1A3B82F6), // bg-blue-500/10
        accentBg = Color(0x4060A5FA),    // bg-blue-400/25
        textColor = Color(0xFFDBEAFE),   // text-blue-100
        gradientStart = Color(0x332563EB), // from-blue-600/20
        gradientEnd = Color(0x1A172554)    // to-blue-950/10
    ),
    WeatherType.SNOWY to WeatherTheme(
        type = WeatherType.SNOWY,
        label = "Snowy",
        cnLabel = "大雪",
        primaryColor = Color(0xFFAED6F1),
        containerBg = Color(0x1A7DD3FC), // bg-sky-200/10
        accentBg = Color(0x337DD3FC),    // bg-sky-300/20
        textColor = Color(0xFFE0F2FE),   // text-sky-200
        gradientStart = Color(0x337DD3FC), // from-sky-300/20
        gradientEnd = Color(0x1A0F172A)    // to-slate-900/10
    ),
    WeatherType.CLOUDY to WeatherTheme(
        type = WeatherType.CLOUDY,
        label = "Cloudy",
        cnLabel = "多云",
        primaryColor = Color(0xFF85929E),
        containerBg = Color(0x1A94A3B8), // bg-slate-400/10
        accentBg = Color(0x33CBD5E1),    // bg-slate-300/20
        textColor = Color(0xFFCBD5E1),   // text-slate-300
        gradientStart = Color(0x3364748B), // from-slate-500/20
        gradientEnd = Color(0x1A18181B)    // to-zinc-900/10
    ),
    WeatherType.STORM to WeatherTheme(
        type = WeatherType.STORM,
        label = "Thunderstorm",
        cnLabel = "强雷阵雨",
        primaryColor = Color(0xFF9B59B6),
        containerBg = Color(0x1AA855F7), // bg-purple-500/10
        accentBg = Color(0x40C084FC),    // bg-purple-400/25
        textColor = Color(0xFFF3E8FF),   // text-purple-100
        gradientStart = Color(0x337E22CE), // from-purple-800/20
        gradientEnd = Color(0x332E1065)    // to-violet-950/20
    )
)

fun getWeatherTheme(weatherType: WeatherType): WeatherTheme {
    return WEATHER_THEMES[weatherType] ?: WEATHER_THEMES[WeatherType.CLOUDY]!!
}

// 4. M3 Shapes 适配
val SimpleWeatherShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp) // Bento 圆角
)

// 5. Typography 适配对齐 Plus Jakarta Sans 规格
val SimpleWeatherTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 64.sp,
        letterSpacing = (-1.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 0.15.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        letterSpacing = 1.sp
    )
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberGold,
    secondary = MutedSlate,
    tertiary = SkyBlue
)

@Composable
fun SimpleWeatherTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = SimpleWeatherShapes,
        typography = SimpleWeatherTypography,
        content = content
    )
}