package com.rsinkwitz.r_solitaire.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_games")
data class SavedGameEntity(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long,
    val initialHoleRow: Int,
    val initialHoleCol: Int,
    val movesJson: String  // JSON-serialisierte Liste von Moves
)

