package com.rsinkwitz.r_solitaire.data

import com.rsinkwitz.r_solitaire.model.Move
import com.rsinkwitz.r_solitaire.model.SavedGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MoveData(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int
)

class SavedGameRepository(private val dao: SavedGameDao) {

    fun getAllGames(): Flow<List<SavedGame>> {
        return dao.getAllGames().map { entities ->
            entities.map { entity -> entity.toSavedGame() }
        }
    }

    suspend fun insertGame(savedGame: SavedGame) {
        dao.insertGame(savedGame.toEntity())
    }

    suspend fun deleteGame(savedGame: SavedGame) {
        dao.deleteGameById(savedGame.id)
    }

    suspend fun getGameById(gameId: String): SavedGame? {
        return dao.getGameById(gameId)?.toSavedGame()
    }

    // Synchrone Methode für YAML-Export (nicht auf Main-Thread aufrufen!)
    fun getAllGamesSync(): List<SavedGame> {
        return dao.getAllGamesSync().map { entity -> entity.toSavedGame() }
    }

    // Speichere einzelnes Spiel (für YAML-Import)
    suspend fun saveGame(savedGame: SavedGame) {
        dao.insertGame(savedGame.toEntity())
    }

    private fun SavedGameEntity.toSavedGame(): SavedGame {
        val movesData = Json.decodeFromString<List<MoveData>>(movesJson)
        val moves = movesData.map {
            Move(it.fromRow, it.fromCol, it.toRow, it.toCol)
        }
        return SavedGame(id, title, timestamp, initialHoleRow, initialHoleCol, moves)
    }

    private fun SavedGame.toEntity(): SavedGameEntity {
        val movesData = moves.map {
            MoveData(it.fromRow, it.fromCol, it.toRow, it.toCol)
        }
        val movesJson = Json.encodeToString(movesData)
        return SavedGameEntity(id, title, timestamp, initialHoleRow, initialHoleCol, movesJson)
    }
}

