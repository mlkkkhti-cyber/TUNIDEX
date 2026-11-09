package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpotDao {
    @Query("SELECT * FROM spots ORDER BY timestamp DESC")
    fun getAllSpots(): Flow<List<SpotEntity>>

    @Query("SELECT * FROM spots WHERE carId = :carId ORDER BY timestamp DESC")
    fun getSpotsForCar(carId: String): Flow<List<SpotEntity>>

    @Query("SELECT COUNT(DISTINCT carId) FROM spots")
    fun getDistinctCarCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM spots")
    fun getTotalSpotsCount(): Flow<Int>

    @Query("SELECT SUM(pointsEarned) FROM spots")
    fun getTotalPoints(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: SpotEntity): Long

    @Delete
    suspend fun deleteSpot(spot: SpotEntity)
}
