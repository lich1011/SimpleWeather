package com.boomkin.simpleweather.presentation.weather

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.boomkin.simpleweather.domain.model.WeatherType
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random

private data class ComposeParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float,
    var phase: Float = 0f,
    var amplitude: Float = 0f
)

private data class ComposeCloud(
    var x: Float,
    var y: Float,
    var radius: Float,
    var vx: Float,
    var alpha: Float
)

/**
 * 沉浸式动态天气粒子系统 (Interactive Canvas Paint)
 */
@Composable
fun WeatherParticleBackground(
    weatherType: WeatherType,
    modifier: Modifier = Modifier
) {
    // Game loop tick state
    var frameTick by remember { mutableStateOf(0L) }
    
    LaunchedEffect(weatherType) {
        while (isActive) {
            withFrameMillis { ms ->
                frameTick = ms
            }
        }
    }

    // Retain particles and animation values in state
    val particles = remember(weatherType) { mutableStateListOf<ComposeParticle>() }
    val clouds = remember(weatherType) { mutableStateListOf<ComposeCloud>() }
    var sunRaysAngle by remember { mutableStateOf(0f) }
    var lightningFlash by remember { mutableStateOf(0f) }
    var lightningCooldown by remember { mutableStateOf(120) }
    var lightningBoltPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        // Initialize particles if they are empty
        if (particles.isEmpty() && clouds.isEmpty()) {
            when (weatherType) {
                WeatherType.SUNNY -> {
                    for (i in 0 until 20) {
                        particles.add(
                            ComposeParticle(
                                x = Random.nextFloat() * width,
                                y = Random.nextFloat() * height,
                                vx = -0.2f + Random.nextFloat() * 0.4f,
                                vy = -0.2f - Random.nextFloat() * 0.4f,
                                radius = 4f + Random.nextFloat() * 15f,
                                alpha = 0.05f + Random.nextFloat() * 0.15f
                            )
                        )
                    }
                }
                WeatherType.RAINY -> {
                    for (i in 0 until 90) {
                        particles.add(
                            ComposeParticle(
                                x = Random.nextFloat() * width,
                                y = Random.nextFloat() * height - height,
                                vx = -1.5f - Random.nextFloat() * 1.5f,
                                vy = 8f + Random.nextFloat() * 6f,
                                radius = 1f + Random.nextFloat() * 1.5f,
                                alpha = 0.15f + Random.nextFloat() * 0.4f
                            )
                        )
                    }
                }
                WeatherType.SNOWY -> {
                    for (i in 0 until 70) {
                        particles.add(
                            ComposeParticle(
                                x = Random.nextFloat() * width,
                                y = Random.nextFloat() * height,
                                vx = -0.5f + Random.nextFloat() * 1.0f,
                                vy = 0.8f + Random.nextFloat() * 1.5f,
                                radius = 1.5f + Random.nextFloat() * 3.5f,
                                alpha = 0.2f + Random.nextFloat() * 0.6f,
                                phase = Random.nextFloat() * (2 * PI.toFloat()),
                                amplitude = 0.2f + Random.nextFloat() * 0.5f
                            )
                        )
                    }
                }
                WeatherType.CLOUDY -> {
                    for (i in 0 until 6) {
                        clouds.add(
                            ComposeCloud(
                                x = Random.nextFloat() * width,
                                y = Random.nextFloat() * (height * 0.5f),
                                radius = 100f + Random.nextFloat() * 120f,
                                vx = 0.1f + Random.nextFloat() * 0.2f,
                                alpha = 0.05f + Random.nextFloat() * 0.12f
                            )
                        )
                    }
                }
                WeatherType.STORM -> {
                    for (i in 0 until 140) {
                        particles.add(
                            ComposeParticle(
                                x = Random.nextFloat() * width,
                                y = Random.nextFloat() * height - height,
                                vx = -3f - Random.nextFloat() * 2f,
                                vy = 12f + Random.nextFloat() * 8f,
                                radius = 1.2f + Random.nextFloat() * 1.5f,
                                alpha = 0.2f + Random.nextFloat() * 0.5f
                            )
                        )
                    }
                }
            }
        }

        // Trigger loop updates and rendering inside Compose DrawScope
        // Read frameTick to satisfy state read and force recompositions
        val unused = frameTick

        when (weatherType) {
            WeatherType.SUNNY -> {
                // 1. Draw Sky Gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A3C61), Color(0xFF2E547E), Color(0xFF1B324D))
                    )
                )

                // 2. Draw Sun Source (Radial Glow) in top right
                val sunCenter = Offset(width * 0.85f, height * 0.15f)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to Color(0x75FFEBAF),
                        0.3f to Color(0x26FFDC82),
                        1.0f to Color(0x00FFDC82),
                        center = sunCenter,
                        radius = 320f
                    ),
                    center = sunCenter,
                    radius = 320f
                )

                // 3. Draw rotating Sunrays
                sunRaysAngle += 0.001f
                val rayCount = 24
                for (r in 0 until rayCount) {
                    val angle = (r * PI / 12).toFloat() + sunRaysAngle
                    val length = 400f + sin(sunRaysAngle * 10f + r) * 40f
                    val endOffset = Offset(
                        sunCenter.x + cos(angle) * length,
                        sunCenter.y + sin(angle) * length
                    )
                    drawLine(
                        color = Color(0x08FFF0BE),
                        start = sunCenter,
                        end = endOffset,
                        strokeWidth = 1.5f
                    )
                }

                // 4. Draw & Update ambient heat/dust particles
                particles.forEach { p ->
                    drawCircle(
                        color = Color(0xFFFFEBB4).copy(alpha = p.alpha),
                        radius = p.radius,
                        center = Offset(p.x, p.y)
                    )
                    p.x += p.vx
                    p.y += p.vy

                    if (p.y < -30f || p.x < -30f || p.x > width + 30f) {
                        p.y = height + 20f
                        p.x = Random.nextFloat() * width
                    }
                }
            }

            WeatherType.RAINY -> {
                // Draw Sky Gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D131F), Color(0xFF1B233A))
                    )
                )

                // Draw & Update falling raindrops
                particles.forEach { p ->
                    drawLine(
                        color = Color(0x66AEDFFF),
                        start = Offset(p.x, p.y),
                        end = Offset(p.x + p.vx * 1.5f, p.y + p.vy * 1.5f),
                        strokeWidth = 1.2f
                    )

                    p.x += p.vx
                    p.y += p.vy

                    if (p.y > height) {
                        p.y = -20f
                        p.x = Random.nextFloat() * width
                        
                        // Ripple hit effect
                        if (Random.nextFloat() > 0.6f) {
                            drawOval(
                                color = Color(0x26AEDFFF),
                                topLeft = Offset(p.x - 8f * p.radius, height - 12f),
                                size = Size(16f * p.radius, 4f * p.radius),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                            )
                        }
                    }
                }
            }

            WeatherType.SNOWY -> {
                // Draw Sky Gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF101622), Color(0xFF2C374E))
                    )
                )

                // Draw & Update drifting snowflakes
                particles.forEach { p ->
                    drawCircle(
                        color = Color.White.copy(alpha = p.alpha),
                        radius = p.radius,
                        center = Offset(p.x, p.y)
                    )

                    p.phase += 0.01f
                    p.x += p.vx + sin(p.phase) * p.amplitude
                    p.y += p.vy

                    if (p.y > height + 10f) {
                        p.y = -10f
                        p.x = Random.nextFloat() * width
                        p.phase = Random.nextFloat() * (2 * PI.toFloat())
                    }
                }
            }

            WeatherType.CLOUDY -> {
                // Draw Sky Gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0C111C), Color(0xFF1E2535), Color(0xFF131B29))
                    )
                )

                // Draw & Update floating clouds
                clouds.forEach { c ->
                    val cloudCenter = Offset(c.x, c.y)
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.0f to Color.White.copy(alpha = c.alpha),
                            0.6f to Color(0xFFA0B9D2).copy(alpha = c.alpha * 0.45f),
                            1.0f to Color.Transparent,
                            center = cloudCenter,
                            radius = c.radius
                        ),
                        center = cloudCenter,
                        radius = c.radius
                    )

                    c.x += c.vx
                    if (c.x - c.radius > width) {
                        c.x = -c.radius
                        c.y = Random.nextFloat() * (height * 0.5f)
                    }
                }
            }

            WeatherType.STORM -> {
                // Draw dynamic lightning color flash backdrop
                var bgStart = Color(0xFF08070D)
                var bgEnd = Color(0xFF1A142E)

                if (lightningFlash > 0.1f) {
                    bgStart = Color(110, 115, 190).copy(alpha = lightningFlash * 0.25f)
                    bgEnd = Color(140, 150, 240).copy(alpha = lightningFlash * 0.15f)
                }

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(bgStart, bgEnd)
                    )
                )

                // Lightning strike core logic
                lightningCooldown--
                if (lightningCooldown <= 0) {
                    if (Random.nextFloat() > 0.4f) {
                        lightningFlash = 1.0f
                        
                        // Generate zig-zag points
                        val points = mutableListOf<Offset>()
                        var currX = width * 0.3f + Random.nextFloat() * (width * 0.4f)
                        var currY = 0f
                        points.add(Offset(currX, currY))
                        while (currY < height * 0.8f) {
                            currX += -25f + Random.nextFloat() * 50f
                            currY += 15f + Random.nextFloat() * 40f
                            points.add(Offset(currX, currY))
                        }
                        lightningBoltPoints = points
                    }
                    lightningCooldown = 180 + Random.nextInt(260)
                }

                if (lightningFlash > 0f) {
                    lightningFlash -= 0.04f
                    if (lightningFlash < 0f) {
                        lightningFlash = 0f
                        lightningBoltPoints = emptyList()
                    }
                }

                // Draw active lightning strike path
                if (lightningFlash > 0.75f && lightningBoltPoints.size > 1) {
                    // Draw outer glow with canvas paint
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            color = android.graphics.Color.argb(
                                (lightningFlash * 255).toInt(), 175, 195, 255
                            )
                            strokeWidth = 15f
                            style = Paint.Style.STROKE
                            strokeJoin = Paint.Join.ROUND
                            strokeCap = Paint.Cap.ROUND
                            maskFilter = android.graphics.BlurMaskFilter(
                                20f, android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                        }
                        val path = android.graphics.Path().apply {
                            moveTo(lightningBoltPoints[0].x, lightningBoltPoints[0].y)
                            for (i in 1 until lightningBoltPoints.size) {
                                lineTo(lightningBoltPoints[i].x, lightningBoltPoints[i].y)
                            }
                        }
                        canvas.nativeCanvas.drawPath(path, paint)
                    }

                    // Draw inner bolt
                    for (i in 0 until lightningBoltPoints.size - 1) {
                        drawLine(
                            color = Color(0xE6E6F0FF),
                            start = lightningBoltPoints[i],
                            end = lightningBoltPoints[i + 1],
                            strokeWidth = 2.5f + Random.nextFloat() * 2f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Draw Torrential heavy storm rains
                particles.forEach { p ->
                    drawLine(
                        color = Color(0x59B4CDFF),
                        start = Offset(p.x, p.y),
                        end = Offset(p.x + p.vx * 1.3f, p.y + p.vy * 1.3f),
                        strokeWidth = 1.6f
                    )

                    p.x += p.vx
                    p.y += p.vy

                    if (p.y > height) {
                        p.y = -20f
                        p.x = Random.nextFloat() * width
                    }
                }
            }
        }
    }
}
