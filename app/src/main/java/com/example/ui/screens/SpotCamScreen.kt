package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CarCatalog
import com.example.model.Car
import com.example.ui.components.CarSilhouetteCanvas
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpotCamScreen(
    viewModel: TunidexViewModel,
    preselectedCar: Car?,
    modifier: Modifier = Modifier
) {
    var selectedCar by remember(preselectedCar) {
        mutableStateOf(preselectedCar ?: CarCatalog.cars.first { it.id == "skyline_r34_gtr" })
    }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var locationInput by remember { mutableStateOf("Cars & Coffee Meet") }
    var condition by remember { mutableStateOf("Pristine / Track Ready") }
    var licensePlate by remember { mutableStateOf("R34-GT") }
    var maskPlate by remember { mutableStateOf(true) }
    var soundHeard by remember { mutableStateOf(true) }
    var selectedMods by remember { mutableStateOf(setOf("Titanium Exhaust", "Carbon Aero")) }
    var spotterNotes by remember { mutableStateOf("Spotted rolling in with incredible exhaust note!") }
    var zoomLevel by remember { mutableStateOf("1x") }
    var carDropdownExpanded by remember { mutableStateOf(false) }
    var showCameraScreen by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top HUD Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF1744))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SPOTTER VIEW • REC",
                    color = Color(0xFFFF1744),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            // Optical Zoom Selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                listOf("1x", "2x", "5x").forEach { z ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (zoomLevel == z) TurboCyan.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { zoomLevel = z }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = z,
                            color = if (zoomLevel == z) TurboCyan else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Viewfinder Camera Reticle Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CarbonDark)
                .border(1.5.dp, TurboCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .testTag("spotter_viewfinder_box"),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Spotted Car Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CarSilhouetteCanvas(
                    tier = selectedCar.tier,
                    isSpotted = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }

            // HUD Crosshairs Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val arm = 24.dp.toPx()
                val reticleColor = TurboCyan.copy(alpha = 0.8f)

                // Top-Left corner
                drawLine(reticleColor, Offset(16.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx() + arm, 16.dp.toPx()), 2.5f)
                drawLine(reticleColor, Offset(16.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx(), 16.dp.toPx() + arm), 2.5f)

                // Top-Right corner
                drawLine(reticleColor, Offset(w - 16.dp.toPx(), 16.dp.toPx()), Offset(w - 16.dp.toPx() - arm, 16.dp.toPx()), 2.5f)
                drawLine(reticleColor, Offset(w - 16.dp.toPx(), 16.dp.toPx()), Offset(w - 16.dp.toPx(), 16.dp.toPx() + arm), 2.5f)

                // Bottom-Left corner
                drawLine(reticleColor, Offset(16.dp.toPx(), h - 16.dp.toPx()), Offset(16.dp.toPx() + arm, h - 16.dp.toPx()), 2.5f)
                drawLine(reticleColor, Offset(16.dp.toPx(), h - 16.dp.toPx()), Offset(16.dp.toPx(), h - 16.dp.toPx() - arm), 2.5f)

                // Bottom-Right corner
                drawLine(reticleColor, Offset(w - 16.dp.toPx(), h - 16.dp.toPx()), Offset(w - 16.dp.toPx() - arm, h - 16.dp.toPx()), 2.5f)
                drawLine(reticleColor, Offset(w - 16.dp.toPx(), h - 16.dp.toPx()), Offset(w - 16.dp.toPx(), h - 16.dp.toPx() - arm), 2.5f)

                // Center crosshair
                drawLine(reticleColor.copy(alpha = 0.4f), Offset(w / 2f - 14f, h / 2f), Offset(w / 2f + 14f, h / 2f), 1.5f)
                drawLine(reticleColor.copy(alpha = 0.4f), Offset(w / 2f, h / 2f - 14f), Offset(w / 2f, h / 2f + 14f), 1.5f)
            }

            // Floating Action Buttons (Live Camera + Gallery)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Live Camera Button
                Button(
                    onClick = { showCameraScreen = true },
                    modifier = Modifier.testTag("open_live_camera_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TurboCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Live Cam",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE CAM",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Gallery Picker Button
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("attach_photo_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonCard.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Gallery",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (photoUri != null) "Change" else "Gallery",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Vehicle Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CarbonBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "TARGET VEHICLE IDENTIFICATION",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CarbonDark)
                            .border(1.dp, selectedCar.tier.color, RoundedCornerShape(10.dp))
                            .clickable { carDropdownExpanded = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedCar.brand} ${selectedCar.name}",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedCar.tier.title} • ${selectedCar.horsepower} HP • +${selectedCar.tier.basePoints} XP",
                                color = selectedCar.tier.color,
                                fontSize = 12.sp
                            )
                        }
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Change Car", tint = selectedCar.tier.color)
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
                                        Text("${car.brand} • ${car.tier.shortName}", color = car.tier.color, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    selectedCar = car
                                    carDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spotting Location & Quick Chips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = RevOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SPOTTING LOCATION",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CarbonDark,
                        unfocusedContainerColor = CarbonDark,
                        focusedBorderColor = RevOrange,
                        unfocusedBorderColor = CarbonBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("spot_location_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hotspot suggestions
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Daikoku PA", "Monaco Square", "Underground Meet", "Highway 1", "Racetrack", "Dealership").forEach { spot ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (locationInput == spot) RevOrange.copy(alpha = 0.25f) else CarbonDark)
                                .border(1.dp, if (locationInput == spot) RevOrange else CarbonBorder, RoundedCornerShape(6.dp))
                                .clickable { locationInput = spot }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = spot, color = if (locationInput == spot) RevOrange else TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Condition & Custom Mods
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SPEC & AFTERMARKET MODIFICATIONS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val availableMods = listOf("Titanium Exhaust", "Carbon Aero", "Widebody", "Aftermarket Rims", "Track Roll Cage")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableMods.forEach { mod ->
                        val isChecked = selectedMods.contains(mod)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChecked) TurboCyan.copy(alpha = 0.2f) else CarbonDark)
                                .border(1.dp, if (isChecked) TurboCyan else CarbonBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedMods = if (isChecked) selectedMods - mod else selectedMods + mod
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isChecked) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = TurboCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = mod,
                                    color = if (isChecked) TurboCyan else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sound heard bonus & Plate mask
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Sound heard toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = TurboCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Heard Engine Rev Live", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "+50 XP Audio Hunter Bonus", color = TurboCyan, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = soundHeard,
                        onCheckedChange = { soundHeard = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = TurboCyan,
                            uncheckedTrackColor = CarbonDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Plate privacy mask toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PrivacyTip, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "License Plate Privacy Shield", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Auto-blur plate in public dex", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = maskPlate,
                        onCheckedChange = { maskPlate = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedTrackColor = CarbonDark
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Big Glowing Submit Button
        Button(
            onClick = {
                viewModel.logSpot(
                    carId = selectedCar.id,
                    locationName = locationInput,
                    condition = condition,
                    mods = selectedMods.joinToString(", "),
                    licensePlate = if (maskPlate) "***-***" else licensePlate,
                    plateMasked = maskPlate,
                    soundHeard = soundHeard,
                    spotterNote = spotterNotes,
                    photoUri = photoUri?.toString()
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("submit_spot_button"),
            colors = ButtonDefaults.buttonColors(containerColor = selectedCar.tier.color),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "LOG SPOT TO TUNIDEX (+${selectedCar.tier.basePoints + (if (soundHeard) 50 else 0) + (if (selectedMods.isNotEmpty()) 50 else 0)} XP)",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showCameraScreen) {
        CameraScreen(
            targetCar = selectedCar,
            onPhotoCaptured = { uri ->
                photoUri = uri
                showCameraScreen = false
            },
            onClose = {
                showCameraScreen = false
            }
        )
    }
}
