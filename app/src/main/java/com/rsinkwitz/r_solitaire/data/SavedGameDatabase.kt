package com.rsinkwitz.r_solitaire.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedGameEntity::class], version = 1, exportSchema = false)
abstract class SavedGameDatabase : RoomDatabase() {
    abstract fun savedGameDao(): SavedGameDao

    companion object {
        @Volatile
        private var INSTANCE: SavedGameDatabase? = null

        fun getDatabase(context: Context): SavedGameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SavedGameDatabase::class.java,
                    "saved_games_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

