package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CarCatalog
import com.example.model.Car
import com.example.ui.components.TachometerGauge
import com.example.ui.theme.ApexRed
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.RevOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurboCyan
import com.example.ui.viewmodel.TunidexViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RevDynoScreen(
    viewModel: TunidexViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCar by viewModel.selectedCarForDyno.collectAsState()
    val synthesizer = viewModel.soundSynthesizer
    val currentRpm by synthesizer.rpmState.collectAsState()
    val isLimiterActive by synthesizer.isLimiterBanging.collectAsState()

    var isMuted by remember { mutableStateOf(synthesizer.isCurrentlyMuted()) }
    var carDropdownExpanded by remember { mutableStateOf(false) }
    var maxRpmAchieved by remember { mutableIntStateOf(0) }
    var isThrottleHeld by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        synthesizer.start()
        onDispose {
            synthesizer.setThrottle(false)
        }
    }

    if (currentRpm.toInt() > maxRpmAchieved) {
        maxRpmAchieved = currentRpm.toInt()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header & Sound Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VIRTUAL ENGINE DYNO",
                    color = TurboCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Live Acoustic Synthesizer",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = {
                    isMuted = synthesizer.toggleMute()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CarbonCard)
                    .border(1.dp, if (!isMuted) TurboCyan else CarbonBorder, CircleShape)
                    .testTag("dyno_audio_toggle")
            ) {
                Icon(
                    imageVector = if (!isMuted) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = "Mute Toggle",
                    tint = if (!isMuted) TurboCyan else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Vehicle Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CarbonBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { carDropdownExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedCar.brand.uppercase(),
                        color = selectedCar.tier.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = selectedCar.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selectedCar.soundType.label} • Redline ${selectedCar.redlineRpm} RPM",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Change Car", tint = TextSecondary)
            }

            DropdownMenu(
                expanded = carDropdownExpanded,
                onDismissRequest = { carDropdownExpanded = false },
                modifier = Modifier
                    .background(CarbonDark)
                    .height(300.dp)
            ) {
                CarCatalog.cars.forEach { car ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(car.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${car.soundType.label} • ${car.redlineRpm} RPM", color = car.tier.color, fontSize = 11.sp)
                            }
                        },
                        onClick = {
                            viewModel.selectCarForDyno(car)
                            maxRpmAchieved = 0
                            carDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tachometer Centerpiece
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            TachometerGauge(
                currentRpm = currentRpm,
                redlineRpm = selectedCar.redlineRpm.toFloat(),
                isLimiterActive = isLimiterActive
            )
        }

        // Dyno Telemetry Bar (Peak RPM, Redline, Limiter Status)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CarbonCard)
                .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                .padding(vertical = 10.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "PEAK RECORDED", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "$maxRpmAchieved RPM",
                    color = TurboCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "REDLINE LIMIT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${selectedCar.redlineRpm} RPM",
                    color = ApexRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "EXHAUST FLAMES", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (isLimiterActive) RevOrange else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isLimiterActive) "IGNITING!" else "IDLE",
                        color = if (isLimiterActive) RevOrange else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gas Pedal ("PRESS & HOLD TO REV")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isThrottleHeld) {
                        Brush.verticalGradient(listOf(RevOrange, Color(0xFFC2410C)))
                    } else {
                        Brush.verticalGradient(listOf(CarbonCard, CarbonDark))
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (isThrottleHeld) Color(0xFFFF9E40) else CarbonBorder,
                    shape = RoundedCornerShape(18.dp)
                )
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isThrottleHeld = true
                            synthesizer.setThrottle(true)
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            isThrottleHeld = false
                            synthesizer.setThrottle(false)
                            true
                        }
                        else -> false
                    }
                }
                .testTag("dyno_gas_pedal"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = if (isThrottleHeld) Color.Black else TurboCyan,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isThrottleHeld) "REV LIMITER ENGAGED!" else "TOUCH & HOLD TO REV",
                        color = if (isThrottleHeld) Color.Black else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isThrottleHeld) "Floor the gas pedal to hear exhaust bangs" else "Simulates real harmonic cylinder combustion",
                        color = if (isThrottleHeld) Color.Black.copy(alpha = 0.8f) else TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
