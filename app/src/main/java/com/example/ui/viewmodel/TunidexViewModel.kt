package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.EngineSoundSynthesizer
import com.example.data.CarCatalog
import com.example.data.db.SpotDao
import com.example.data.db.SpotEntity
import com.example.data.db.TunidexDatabase
import com.example.model.Car
import com.example.model.CarTier
import com.example.model.QuizQuestion
import com.example.model.QuizRepository
import com.example.model.SpotQuest
import com.example.model.SpotterBadge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DexEntryUiModel(
    val car: Car,
    val isSpotted: Boolean,
    val spotCount: Int,
    val latestSpot: SpotEntity?,
    val bestPhotoUri: String?
)

data class UserStats(
    val totalPoints: Int = 0,
    val spottedCount: Int = 0,
    val totalCarsCount: Int = CarCatalog.cars.size,
    val completionPercent: Int = 0,
    val level: Int = 1,
    val xpInLevel: Int = 0,
    val xpForNextLevel: Int = 500,
    val rankTitle: String = "Novice Spotter",
    val tierCounts: Map<CarTier, Pair<Int, Int>> = emptyMap()
)

data class NewSpotCelebrationData(
    val car: Car,
    val pointsAwarded: Int,
    val isFirstTimeDexUnlock: Boolean,
    val spotLocation: String
)

class TunidexViewModel(application: Application) : AndroidViewModel(application) {

    private val spotDao: SpotDao = TunidexDatabase.getDatabase(application).spotDao()
    val soundSynthesizer = EngineSoundSynthesizer()

    val allSpots: StateFlow<List<SpotEntity>> = spotDao.getAllSpots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTierFilter = MutableStateFlow<CarTier?>(null)
    val selectedTierFilter = _selectedTierFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCarForDossier = MutableStateFlow<Car?>(null)
    val selectedCarForDossier = _selectedCarForDossier.asStateFlow()

    private val _selectedCarForDyno = MutableStateFlow(CarCatalog.cars.first { it.id == "porsche_911_gt3_rs_992" })
    val selectedCarForDyno = _selectedCarForDyno.asStateFlow()

    private val _newSpotCelebration = MutableStateFlow<NewSpotCelebrationData?>(null)
    val newSpotCelebration = _newSpotCelebration.asStateFlow()

    private val _quizAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val quizAnswers = _quizAnswers.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore = _quizScore.asStateFlow()

    private val _extraBonusPoints = MutableStateFlow(0)

    val dexEntries: StateFlow<List<DexEntryUiModel>> = combine(
        allSpots,
        _selectedTierFilter,
        _searchQuery
    ) { spots, tierFilter, query ->
        val spotsByCarId = spots.groupBy { it.carId }

        CarCatalog.cars
            .filter { car ->
                (tierFilter == null || car.tier == tierFilter) &&
                (query.isBlank() || car.name.contains(query, ignoreCase = true) ||
                 car.brand.contains(query, ignoreCase = true) ||
                 car.category.contains(query, ignoreCase = true))
            }
            .map { car ->
                val carSpots = spotsByCarId[car.id] ?: emptyList()
                val latest = carSpots.firstOrNull()
                val photo = carSpots.firstOrNull { !it.photoUri.isNullOrBlank() }?.photoUri
                DexEntryUiModel(
                    car = car,
                    isSpotted = carSpots.isNotEmpty(),
                    spotCount = carSpots.size,
                    latestSpot = latest,
                    bestPhotoUri = photo
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStats> = combine(allSpots, _extraBonusPoints) { spots, bonusPoints ->
        val spottedCarIds = spots.map { it.carId }.toSet()
        val spottedCount = spottedCarIds.size
        val totalCars = CarCatalog.cars.size
        val pct = if (totalCars > 0) ((spottedCount * 100) / totalCars) else 0

        val baseSpotPoints = spots.sumOf { it.pointsEarned }
        val totalPts = baseSpotPoints + bonusPoints

        // Level calculation: 500 XP per level with scaling
        val xpPerLevel = 500
        val currentLevel = (totalPts / xpPerLevel) + 1
        val xpInLevel = totalPts % xpPerLevel

        val rank = when {
            currentLevel >= 15 -> "Apex Legend Spotter"
            currentLevel >= 10 -> "Hypercar Specialist"
            currentLevel >= 7 -> "Master Car Hunter"
            currentLevel >= 4 -> "Street Scout"
            currentLevel >= 2 -> "Junior Spotter"
            else -> "Novice Spotter"
        }

        val tierCounts = CarTier.entries.associateWith { tier ->
            val totalInTier = CarCatalog.cars.count { it.tier == tier }
            val spottedInTier = CarCatalog.cars.count { it.tier == tier && spottedCarIds.contains(it.id) }
            Pair(spottedInTier, totalInTier)
        }

        UserStats(
            totalPoints = totalPts,
            spottedCount = spottedCount,
            totalCarsCount = totalCars,
            completionPercent = pct,
            level = currentLevel,
            xpInLevel = xpInLevel,
            xpForNextLevel = xpPerLevel,
            rankTitle = rank,
            tierCounts = tierCounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    val quests: StateFlow<List<SpotQuest>> = allSpots.combine(_quizScore) { spots, quizScoreVal ->
        val tier3PlusSpots = spots.count { spot ->
            val car = CarCatalog.getCarById(spot.carId)
            car != null && (car.tier == CarTier.GOLD || car.tier == CarTier.PLATINUM || car.tier == CarTier.DIAMOND)
        }
        val customModSpots = spots.count { it.mods.isNotBlank() }
        val soundHeardSpots = spots.count { it.soundHeard }

        listOf(
            SpotQuest(
                id = "quest_first_spot",
                title = "Street Initiate",
                description = "Log your first vehicle spot in the wild",
                xpReward = 150,
                progress = spots.size.coerceAtMost(1),
                target = 1
            ),
            SpotQuest(
                id = "quest_exotic_spot",
                title = "Exotic Spotter",
                description = "Spot 2 cars of Tier 3 (Gold) or higher",
                xpReward = 400,
                progress = tier3PlusSpots.coerceAtMost(2),
                target = 2
            ),
            SpotQuest(
                id = "quest_sound_hunter",
                title = "Exhaust Listener",
                description = "Log a spot with engine sound heard live",
                xpReward = 200,
                progress = soundHeardSpots.coerceAtMost(1),
                target = 1
            ),
            SpotQuest(
                id = "quest_mod_scout",
                title = "Tuner Culture",
                description = "Spot 2 cars with custom aftermarket modifications",
                xpReward = 300,
                progress = customModSpots.coerceAtMost(2),
                target = 2
            ),
            SpotQuest(
                id = "quest_quiz_master",
                title = "Car Historian",
                description = "Answer at least 3 quiz trivia questions correctly",
                xpReward = 350,
                progress = quizScoreVal.coerceAtMost(3),
                target = 3
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<SpotterBadge>> = combine(allSpots, _quizScore) { spots, quizScoreVal ->
        val spottedCarIds = spots.map { it.carId }.toSet()
        val hasHypercar = spots.any { spot ->
            CarCatalog.getCarById(spot.carId)?.tier == CarTier.PLATINUM
        }
        val hasMythic = spots.any { spot ->
            CarCatalog.getCarById(spot.carId)?.tier == CarTier.DIAMOND
        }
        val jdmCount = spots.count { spot ->
            CarCatalog.getCarById(spot.carId)?.originCountry == "Japan"
        }

        listOf(
            SpotterBadge(
                id = "badge_first_spot",
                title = "First Ignition",
                description = "Logged your very first vehicle spot",
                category = "Milestone",
                isUnlocked = spots.isNotEmpty()
            ),
            SpotterBadge(
                id = "badge_jdm_master",
                title = "JDM Connoisseur",
                description = "Spotted at least 3 Japanese performance icons",
                category = "Regional",
                isUnlocked = jdmCount >= 3
            ),
            SpotterBadge(
                id = "badge_hyper_hunter",
                title = "Hypercar Hunter",
                description = "Spotted a multi-million dollar Tier 4 Hypercar",
                category = "Rarity",
                isUnlocked = hasHypercar
            ),
            SpotterBadge(
                id = "badge_grail_keeper",
                title = "Grail Chaser",
                description = "Spotted a Tier 5 Mythic automotive legend",
                category = "Rarity",
                isUnlocked = hasMythic
            ),
            SpotterBadge(
                id = "badge_sound_rev",
                title = "Dyno Addict",
                description = "Tested high-RPM engine revs on the sound dyno",
                category = "Dyno",
                isUnlocked = true
            ),
            SpotterBadge(
                id = "badge_encyclopedia",
                title = "Dex Scholar",
                description = "Aced the Car Spotter trivia test",
                category = "Knowledge",
                isUnlocked = quizScoreVal >= 4
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-configure initial dyno car sound
        val initialCar = _selectedCarForDyno.value
        soundSynthesizer.setSoundProfile(initialCar.soundType, initialCar.redlineRpm)
    }

    fun setTierFilter(tier: CarTier?) {
        _selectedTierFilter.value = tier
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCarForDossier(car: Car?) {
        _selectedCarForDossier.value = car
    }

    fun selectCarForDyno(car: Car) {
        _selectedCarForDyno.value = car
        soundSynthesizer.setSoundProfile(car.soundType, car.redlineRpm)
    }

    fun dismissCelebration() {
        _newSpotCelebration.value = null
    }

    fun logSpot(
        carId: String,
        locationName: String,
        condition: String,
        mods: String,
        licensePlate: String,
        plateMasked: Boolean,
        soundHeard: Boolean,
        spotterNote: String,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val car = CarCatalog.getCarById(carId) ?: return@launch
            val existingSpots = allSpots.value.filter { it.carId == carId }
            val isFirstTime = existingSpots.isEmpty()

            val basePoints = car.tier.basePoints
            val soundBonus = if (soundHeard) 50 else 0
            val modBonus = if (mods.isNotBlank()) 50 else 0
            val firstTimeBonus = if (isFirstTime) 100 else 0
            val totalPointsEarned = basePoints + soundBonus + modBonus + firstTimeBonus

            val spot = SpotEntity(
                carId = carId,
                timestamp = System.currentTimeMillis(),
                locationName = locationName.ifBlank { "Street Spot" },
                photoUri = photoUri,
                condition = condition,
                mods = mods,
                licensePlate = licensePlate,
                plateMasked = plateMasked,
                soundHeard = soundHeard,
                pointsEarned = totalPointsEarned,
                spotterNote = spotterNote
            )

            spotDao.insertSpot(spot)

            _newSpotCelebration.value = NewSpotCelebrationData(
                car = car,
                pointsAwarded = totalPointsEarned,
                isFirstTimeDexUnlock = isFirstTime,
                spotLocation = locationName.ifBlank { "Street Spot" }
            )
        }
    }

    fun deleteSpot(spot: SpotEntity) {
        viewModelScope.launch {
            spotDao.deleteSpot(spot)
        }
    }

    fun answerQuizQuestion(question: QuizQuestion, selectedIndex: Int) {
        val currentAnswers = _quizAnswers.value.toMutableMap()
        if (currentAnswers.containsKey(question.id)) return // already answered

        currentAnswers[question.id] = selectedIndex
        _quizAnswers.value = currentAnswers

        if (selectedIndex == question.correctIndex) {
            _quizScore.value += 1
            _extraBonusPoints.value += question.xpBonus
        }
    }

    fun resetQuiz() {
        _quizAnswers.value = emptyMap()
        _quizScore.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        soundSynthesizer.stop()
    }
}
