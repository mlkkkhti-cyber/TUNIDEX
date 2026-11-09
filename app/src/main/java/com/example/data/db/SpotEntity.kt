package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spots")
data class SpotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val locationName: String = "Street Spot",
    val photoUri: String? = null,
    val condition: String = "Clean Street",
    val mods: String = "",
    val licensePlate: String = "",
    val plateMasked: Boolean = true,
    val soundHeard: Boolean = false,
    val pointsEarned: Int = 100,
    val spotterNote: String = ""
)
