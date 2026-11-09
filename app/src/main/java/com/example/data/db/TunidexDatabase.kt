package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SpotEntity::class], version = 1, exportSchema = false)
abstract class TunidexDatabase : RoomDatabase() {
    abstract fun spotDao(): SpotDao

    companion object {
        @Volatile
        private var INSTANCE: TunidexDatabase? = null

        fun getDatabase(context: Context): TunidexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TunidexDatabase::class.java,
                    "tunidex_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
