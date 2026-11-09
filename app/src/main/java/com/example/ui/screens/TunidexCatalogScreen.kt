package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Car
import com.example.model.CarTier
import com.example.ui.components.VehicleCard
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

@Composable
fun TunidexCatalogScreen(
    viewModel: TunidexViewModel,
    onNavigateToSpotWithCar: (Car) -> Unit,
    onNavigateToDynoWithCar: (Car) -> Unit,
    modifier: Modifier = Modifier
) {
    val dexEntries by viewModel.dexEntries.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val selectedTier by viewModel.selectedTierFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBlack)
    ) {
        // Top Header Banner with Stats & Level
        HeaderStatsCard(userStats = userStats)

        // Search Bar & Filter Chips
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search make, model, or class...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TurboCyan,
                    unfocusedBorderColor = CarbonBorder,
                    focusedContainerColor = CarbonCard,
                    unfocusedContainerColor = CarbonCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("catalog_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tier Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    TierFilterChip(
                        label = "ALL (${userStats.totalCarsCount})",
                        isSelected = selectedTier == null,
                        color = TurboCyan,
                        onClick = { viewModel.setTierFilter(null) }
                    )
                }
                items(CarTier.entries) { tier ->
                    val pair = userStats.tierCounts[tier] ?: Pair(0, 0)
                    TierFilterChip(
                        label = "${tier.shortName} (${pair.first}/${pair.second})",
                        isSelected = selectedTier == tier,
                        color = tier.color,
                        onClick = { viewModel.setTierFilter(tier) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Dex Vehicle Cards
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("catalog_grid")
        ) {
            items(dexEntries, key = { it.car.id }) { item ->
                VehicleCard(
                    car = item.car,
                    isSpotted = item.isSpotted,
                    spotCount = item.spotCount,
                    photoUri = item.bestPhotoUri,
                    onCardClick = { viewModel.selectCarForDossier(item.car) },
                    onQuickSpotClick = { onNavigateToSpotWithCar(item.car) },
                    onDynoClick = {
                        viewModel.selectCarForDyno(item.car)
                        onNavigateToDynoWithCar(item.car)
                    }
                )
            }
        }
    }
}

@Composable
private fun HeaderStatsCard(userStats: com.example.ui.viewmodel.UserStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CarbonBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RevOrange.copy(alpha = 0.2f))
                            .border(1.dp, RevOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = RevOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "LVL ${userStats.level} • ${userStats.rankTitle}",
                            color = RevOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${userStats.totalPoints} XP",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Dex count
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TUNIDEX PROGRESS",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${userStats.spottedCount}/${userStats.totalCarsCount}",
                        color = TurboCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { userStats.completionPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = TurboCyan,
                trackColor = CarbonDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${userStats.completionPercent}% Dex Complete",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "${userStats.xpInLevel}/${userStats.xpForNextLevel} XP to Next Lvl",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun TierFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val bg = if (isSelected) color.copy(alpha = 0.25f) else CarbonCard
    val borderCol = if (isSelected) color else CarbonBorder
    val textCol = if (isSelected) color else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .testTag("tier_chip_$label")
    ) {
        Text(
            text = label,
            color = textCol,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
