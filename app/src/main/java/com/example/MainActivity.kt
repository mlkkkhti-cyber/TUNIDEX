package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Garage
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Car
import com.example.ui.components.CarDossierSheet
import com.example.ui.components.NewSpotCelebrationModal
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.QuestsAndQuizScreen
import com.example.ui.screens.RevDynoScreen
import com.example.ui.screens.SpotCamScreen
import com.example.ui.screens.TunidexCatalogScreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.RevOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TunidexTheme
import com.example.ui.theme.TurboCyan
import com.example.ui.viewmodel.TunidexViewModel

enum class TunidexScreen(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CATALOG(R.string.nav_tunidex, Icons.Filled.DirectionsCar, Icons.Outlined.DirectionsCar),
    SPOT_CAM(R.string.nav_spot, Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt),
    REV_DYNO(R.string.nav_dyno, Icons.Filled.Speed, Icons.Outlined.Speed),
    GARAGE(R.string.nav_garage, Icons.Filled.Garage, Icons.Outlined.Garage),
    QUESTS(R.string.nav_quests, Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TunidexTheme {
                TunidexApp()
            }
        }
    }
}

@Composable
fun TunidexApp(viewModel: TunidexViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(TunidexScreen.CATALOG) }
    var spotCamTargetCar by remember { mutableStateOf<Car?>(null) }

    val selectedCarForDossier by viewModel.selectedCarForDossier.collectAsState()
    val allSpots by viewModel.allSpots.collectAsState()
    val celebrationData by viewModel.newSpotCelebration.collectAsState()

    // BackHandler: return to Catalog if currently on another screen
    if (currentScreen != TunidexScreen.CATALOG) {
        BackHandler {
            currentScreen = TunidexScreen.CATALOG
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CarbonBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = CarbonDark,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("tunidex_bottom_nav")
            ) {
                TunidexScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = stringResource(screen.titleRes),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(screen.titleRes),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = TurboCyan,
                            indicatorColor = TurboCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CarbonBlack)
        ) {
            when (currentScreen) {
                TunidexScreen.CATALOG -> {
                    TunidexCatalogScreen(
                        viewModel = viewModel,
                        onNavigateToSpotWithCar = { car ->
                            spotCamTargetCar = car
                            currentScreen = TunidexScreen.SPOT_CAM
                        },
                        onNavigateToDynoWithCar = { car ->
                            viewModel.selectCarForDyno(car)
                            currentScreen = TunidexScreen.REV_DYNO
                        }
                    )
                }

                TunidexScreen.SPOT_CAM -> {
                    SpotCamScreen(
                        viewModel = viewModel,
                        preselectedCar = spotCamTargetCar
                    )
                }

                TunidexScreen.REV_DYNO -> {
                    RevDynoScreen(
                        viewModel = viewModel
                    )
                }

                TunidexScreen.GARAGE -> {
                    GarageScreen(
                        viewModel = viewModel,
                        onNavigateToSpotCam = {
                            currentScreen = TunidexScreen.SPOT_CAM
                        }
                    )
                }

                TunidexScreen.QUESTS -> {
                    QuestsAndQuizScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Vehicle Dossier Bottom Sheet
    selectedCarForDossier?.let { car ->
        val carSpots = allSpots.filter { it.carId == car.id }
        CarDossierSheet(
            car = car,
            spots = carSpots,
            onDismiss = { viewModel.selectCarForDossier(null) },
            onNavigateToDyno = { dynoCar ->
                viewModel.selectCarForDyno(dynoCar)
                currentScreen = TunidexScreen.REV_DYNO
            },
            onNavigateToSpot = { spotCar ->
                spotCamTargetCar = spotCar
                currentScreen = TunidexScreen.SPOT_CAM
            }
        )
    }

    // New Spot Celebration Modal
    celebrationData?.let { data ->
        NewSpotCelebrationModal(
            data = data,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }
}
