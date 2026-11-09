package com.example.model

import com.example.ui.theme.TierBronze
import com.example.ui.theme.TierDiamond
import com.example.ui.theme.TierGold
import com.example.ui.theme.TierPlatinum
import com.example.ui.theme.TierSilver
import androidx.compose.ui.graphics.Color

enum class CarTier(
    val title: String,
    val shortName: String,
    val color: Color,
    val basePoints: Int,
    val rarityRank: String
) {
    BRONZE("Tier 1 - Tuner & Daily", "Tier 1", TierBronze, 100, "Common"),
    SILVER("Tier 2 - Sport & Performance", "Tier 2", TierSilver, 250, "Uncommon"),
    GOLD("Tier 3 - Supercar & Exotic", "Tier 3", TierGold, 600, "Rare"),
    PLATINUM("Tier 4 - Hypercar & Megacar", "Tier 4", TierPlatinum, 1500, "Ultra Rare"),
    DIAMOND("Tier 5 - Mythic & Legend", "Tier 5", TierDiamond, 3000, "Legendary")
}

enum class EngineRevSound(
    val label: String,
    val cylinders: Int,
    val baseRumbleHz: Float,
    val turboSpool: Boolean
) {
    FLAT_6("4.0L Mezger / GT3 Flat-6 Screamer", 6, 68f, false),
    INLINE_6_TURBO("2.6L Twin-Turbo RB26 / 2JZ", 6, 60f, true),
    SCREAMING_V10("5.2L Naturally Aspirated V10", 10, 75f, false),
    SYMPHONIC_V12("6.5L F1-Harmonic 65° V12", 12, 82f, false),
    TWIN_TURBO_V8("4.0L Flat-Plane Twin-Turbo V8", 8, 62f, true),
    SUPERCHARGED_V8("6.2L Crossplane Supercharged V8", 8, 48f, true),
    QUAD_TURBO_W16("8.0L Quad-Turbo 64V W16", 16, 56f, true),
    ROTARY_TURBO("1.3L Twin-Rotor Sequential Turbo", 2, 86f, true)
}

data class Car(
    val id: String,
    val name: String,
    val brand: String,
    val year: Int,
    val tier: CarTier,
    val category: String,
    val horsepower: Int,
    val topSpeedMph: Int,
    val zeroToSixtySec: Float,
    val engineLayout: String,
    val redlineRpm: Int,
    val soundType: EngineRevSound,
    val rarityScore: Int,
    val funFact: String,
    val badgeTitle: String,
    val originCountry: String
)
