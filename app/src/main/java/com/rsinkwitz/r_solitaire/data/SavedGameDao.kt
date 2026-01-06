package com.rsinkwitz.r_solitaire.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedGameDao {
    @Query("SELECT * FROM saved_games ORDER BY timestamp DESC")
    fun getAllGames(): Flow<List<SavedGameEntity>>

    @Query("SELECT * FROM saved_games ORDER BY timestamp DESC")
    fun getAllGamesSync(): List<SavedGameEntity>

    @Insert
    suspend fun insertGame(game: SavedGameEntity)

    @Delete
    suspend fun deleteGame(game: SavedGameEntity)

    @Query("SELECT * FROM saved_games WHERE id = :gameId")
    suspend fun getGameById(gameId: String): SavedGameEntity?

    @Query("DELETE FROM saved_games WHERE id = :gameId")
    suspend fun deleteGameById(gameId: String)
}

