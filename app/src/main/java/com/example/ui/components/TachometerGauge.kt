package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ApexRed
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.RevOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TurboCyan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun TachometerGauge(
    currentRpm: Float,
    redlineRpm: Float,
    isLimiterActive: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedRpm = remember { Animatable(currentRpm) }

    LaunchedEffect(currentRpm) {
        animatedRpm.animateTo(
            targetValue = currentRpm,
            animationSpec = tween(durationMillis = 40, easing = LinearEasing)
        )
    }

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 20f

            // Start angle: 135 degrees (bottom left), sweep: 270 degrees (clockwise to bottom right)
            val startAngle = 135f
            val totalSweep = 270f
            val maxRpmDisplay = (redlineRpm * 1.15f).coerceAtLeast(10000f)

            // Outer dial track
            drawArc(
                color = CarbonCard,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )

            // Redline arc sector
            val redlineStartFraction = (redlineRpm / maxRpmDisplay).coerceIn(0f, 1f)
            val redlineStartAngle = startAngle + redlineStartFraction * totalSweep
            val redlineSweep = (1f - redlineStartFraction) * totalSweep
            drawArc(
                brush = Brush.horizontalGradient(listOf(RevOrange, ApexRed)),
                startAngle = redlineStartAngle,
                sweepAngle = redlineSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )

            // Active RPM sweep arc
            val activeFraction = (animatedRpm.value / maxRpmDisplay).coerceIn(0f, 1f)
            val activeSweep = activeFraction * totalSweep
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to TurboCyan,
                    0.6f to RevOrange,
                    1.0f to ApexRed
                ),
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )

            // RPM Ticks (0 through 10)
            val numTicks = 10
            for (i in 0..numTicks) {
                val tickFraction = i / numTicks.toFloat()
                val tickAngleDeg = startAngle + tickFraction * totalSweep
                val tickAngleRad = (tickAngleDeg * PI / 180f).toFloat()

                val isRedzone = (i * 1000) >= redlineRpm
                val tickColor = if (isRedzone) ApexRed else TextMuted
                val tickLength = if (i % 2 == 0) 18f else 10f

                val outerX = center.x + (radius - 10f) * cos(tickAngleRad)
                val outerY = center.y + (radius - 10f) * sin(tickAngleRad)
                val innerX = center.x + (radius - 10f - tickLength) * cos(tickAngleRad)
                val innerY = center.y + (radius - 10f - tickLength) * sin(tickAngleRad)

                drawLine(
                    color = tickColor,
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = if (i % 2 == 0) 3.5f else 2f
                )
            }

            // Needle angle
            val needleAngleDeg = startAngle + activeFraction * totalSweep
            val needleAngleRad = (needleAngleDeg * PI / 180f).toFloat()
            val needleLength = radius - 24f

            val needleTip = Offset(
                center.x + needleLength * cos(needleAngleRad),
                center.y + needleLength * sin(needleAngleRad)
            )
            val needleBack = Offset(
                center.x - 22f * cos(needleAngleRad),
                center.y - 22f * sin(needleAngleRad)
            )

            // Needle line
            val needleColor = if (isLimiterActive) ApexRed else Color(0xFFFF5252)
            drawLine(
                color = needleColor,
                start = needleBack,
                end = needleTip,
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            // Center hub
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 18f,
                center = center
            )
            drawCircle(
                color = needleColor,
                radius = 7f,
                center = center
            )

            // Exhaust Flames / Sparks when limiter is banging!
            if (isLimiterActive) {
                for (spark in 0..8) {
                    val angleOffset = (Random.nextFloat() - 0.5f) * 0.4f
                    val dist = radius + 15f + Random.nextFloat() * 35f
                    val sparkAngle = needleAngleRad + angleOffset
                    val sx = center.x + dist * cos(sparkAngle)
                    val sy = center.y + dist * sin(sparkAngle)
                    drawCircle(
                        color = if (spark % 2 == 0) Color(0xFFFF9100) else Color(0xFFFFEA00),
                        radius = 3f + Random.nextFloat() * 4f,
                        center = Offset(sx, sy)
                    )
                }
            }
        }

        // Digital RPM and telemetry readouts inside hub
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(animatedRpm.value / 10).toInt() * 10}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (isLimiterActive) ApexRed else TextPrimary
            )
            Text(
                text = if (isLimiterActive) "LIMITER CUT" else "RPM x1000",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLimiterActive) ApexRed else TurboCyan
            )
        }
    }
}
