package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CarCatalog
import com.example.data.db.SpotEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GarageScreen(
    viewModel: TunidexViewModel,
    onNavigateToSpotCam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSpots by viewModel.allSpots.collectAsState()
    val userStats by viewModel.userStats.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBlack)
            .padding(16.dp)
    ) {
        // Garage Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SPOTTER GARAGE",
                    color = RevOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "My Vehicle Spottings",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${allSpots.size} Total Spots",
                    color = TurboCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (allSpots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CarbonCard)
                            .border(1.dp, CarbonBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = RevOrange,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Garage is Empty",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Head out to the street or a car meet and log your first spot with the Spot Cam!",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onNavigateToSpotCam,
                        colors = ButtonDefaults.buttonColors(containerColor = RevOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_garage_spot_btn")
                    ) {
                        Text(text = "LAUNCH SPOT CAM", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("garage_spots_list")
            ) {
                items(allSpots, key = { it.id }) { spot ->
                    GarageSpotCard(
                        spot = spot,
                        onDeleteClick = { viewModel.deleteSpot(spot) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GarageSpotCard(
    spot: SpotEntity,
    onDeleteClick: () -> Unit
) {
    val car = CarCatalog.getCarById(spot.carId) ?: return
    val tierColor = car.tier.color
    val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(spot.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(CarbonDark),
                contentAlignment = Alignment.Center
            ) {
                if (!spot.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = spot.photoUri,
                        contentDescription = car.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CarSilhouetteCanvas(
                        tier = car.tier,
                        isSpotted = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    )
                }

                // Delete button overlay
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Spot",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Tier Tag overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(tierColor.copy(alpha = 0.35f))
                        .border(1.dp, tierColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = car.tier.shortName, color = tierColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Info Body
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = car.brand.uppercase(), color = tierColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text(text = car.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "+${spot.pointsEarned} XP", color = RevOrange, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = RevOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${spot.locationName} • $dateStr", color = TextSecondary, fontSize = 11.sp)
                }

                if (spot.mods.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Mods: ${spot.mods}", color = TurboCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                if (spot.spotterNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "\"${spot.spotterNote}\"", color = TextMuted, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }
    }
}
