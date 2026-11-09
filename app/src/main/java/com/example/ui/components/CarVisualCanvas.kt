package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.CarTier

@Composable
fun CarSilhouetteCanvas(
    tier: CarTier,
    isSpotted: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonGlow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val tierAccent = tier.color
    val strokeColor = if (isSpotted) tierAccent else Color(0xFF475569)
    val bodyFillColor = if (isSpotted) tierAccent.copy(alpha = 0.12f) else Color(0xFF0F172A)
    val underglowColor = if (isSpotted) tierAccent.copy(alpha = 0.45f * pulseGlow) else Color.Transparent

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Underglow neon gradient
        if (isSpotted) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(underglowColor, Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.78f),
                    radius = w * 0.5f
                ),
                radius = w * 0.45f,
                center = Offset(w * 0.5f, h * 0.78f)
            )
        }

        // Aerodynamic sports coupe silhouette path
        val carPath = Path().apply {
            // Start from front bumper splitter
            moveTo(w * 0.08f, h * 0.68f)
            // Front splitter low lip
            lineTo(w * 0.16f, h * 0.68f)
            // Front wheel arch
            cubicTo(w * 0.18f, h * 0.52f, w * 0.28f, h * 0.52f, w * 0.30f, h * 0.68f)
            // Side skirt rocker panel
            lineTo(w * 0.68f, h * 0.68f)
            // Rear wheel arch
            cubicTo(w * 0.70f, h * 0.50f, w * 0.82f, h * 0.50f, w * 0.84f, h * 0.68f)
            // Rear diffuser / exhaust exit
            lineTo(w * 0.92f, h * 0.68f)
            // Rear tail section and wing support
            lineTo(w * 0.94f, h * 0.56f)
            // Rear GT wing spoiler
            lineTo(w * 0.96f, h * 0.48f)
            lineTo(w * 0.86f, h * 0.49f)
            lineTo(w * 0.84f, h * 0.56f)
            // Fastback rear glass / roofline
            cubicTo(w * 0.74f, h * 0.46f, w * 0.60f, h * 0.38f, w * 0.48f, h * 0.38f)
            // Windshield down to low hood
            cubicTo(w * 0.36f, h * 0.38f, w * 0.22f, h * 0.52f, w * 0.08f, h * 0.58f)
            // Nose cone to splitter
            lineTo(w * 0.08f, h * 0.68f)
            close()
        }

        // Fill body
        drawPath(
            path = carPath,
            color = bodyFillColor,
            style = Fill
        )

        // Stroke body contours
        drawPath(
            path = carPath,
            color = strokeColor,
            style = Stroke(
                width = if (isSpotted) 3.5f else 2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Cockpit window accent
        val windowPath = Path().apply {
            moveTo(w * 0.38f, h * 0.42f)
            lineTo(w * 0.48f, h * 0.41f)
            lineTo(w * 0.66f, h * 0.47f)
            lineTo(w * 0.38f, h * 0.51f)
            close()
        }
        drawPath(
            path = windowPath,
            color = if (isSpotted) strokeColor.copy(alpha = 0.35f) else Color(0xFF1E293B),
            style = Fill
        )
        drawPath(
            path = windowPath,
            color = strokeColor.copy(alpha = 0.7f),
            style = Stroke(width = 1.5f)
        )

        // Wheels
        drawWheel(w * 0.24f, h * 0.68f, w * 0.08f, strokeColor, isSpotted)
        drawWheel(w * 0.76f, h * 0.68f, w * 0.085f, strokeColor, isSpotted)

        // Headlight & Taillight laser lines
        if (isSpotted) {
            // Amber / Cyan headlights
            drawLine(
                color = Color(0xFF00E5FF),
                start = Offset(w * 0.08f, h * 0.59f),
                end = Offset(w * 0.16f, h * 0.57f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            // Neon red LED tail bar
            drawLine(
                color = Color(0xFFFF1744),
                start = Offset(w * 0.90f, h * 0.56f),
                end = Offset(w * 0.94f, h * 0.57f),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawWheel(
    cx: Float,
    cy: Float,
    radius: Float,
    accentColor: Color,
    isSpotted: Boolean
) {
    // Tire
    drawCircle(
        color = Color(0xFF0B0F17),
        radius = radius,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = accentColor.copy(alpha = if (isSpotted) 0.8f else 0.4f),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 2.5f)
    )
    // Rim center
    drawCircle(
        color = if (isSpotted) accentColor else Color(0xFF334155),
        radius = radius * 0.55f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )
    // Brake caliper accent
    if (isSpotted) {
        drawArc(
            color = Color(0xFFFF9E40),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - radius * 0.45f, cy - radius * 0.45f),
            size = Size(radius * 0.9f, radius * 0.9f),
            style = Stroke(width = 3.5f)
        )
    }
}
