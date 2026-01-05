package com.rsinkwitz.r_solitaire.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rsinkwitz.r_solitaire.data.SavedGameDatabase
import com.rsinkwitz.r_solitaire.data.SavedGameRepository
import com.rsinkwitz.r_solitaire.model.Hole
import com.rsinkwitz.r_solitaire.model.Move
import com.rsinkwitz.r_solitaire.model.ReplaySpeed
import com.rsinkwitz.r_solitaire.model.ReplayState
import com.rsinkwitz.r_solitaire.model.SavedGame
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class SolitaireViewModel(application: Application) : AndroidViewModel(application) {

    // Repository für gespeicherte Spiele
    private val repository: SavedGameRepository

    init {
        val database = SavedGameDatabase.getDatabase(application)
        repository = SavedGameRepository(database.savedGameDao())
    }

    // 7x7 Board mit Kreuzform
    var board = mutableStateOf<SnapshotStateList<List<Hole?>>>(createBoard())
        private set

    // Spielzustand
    var isBeforeFirst = mutableStateOf(true)
        private set

    // Initial entfernter Stöpsel (für Save-Funktion)
    private var initialHoleRow: Int = -1
    private var initialHoleCol: Int = -1

    private var selectedHole: Hole? = null
    private var lastSelectedHole: Hole? = null

    // Zug-Historie für Undo
    private val moveHistory = mutableListOf<Move>()

    // Replay-Status
    var replayState = mutableStateOf<ReplayState?>(null)
        private set

    private var replayJob: Job? = null

    val isInReplayMode: Boolean
        get() = replayState.value != null

    private fun createBoard(): SnapshotStateList<List<Hole?>> {
        val boardList = mutableListOf<List<Hole?>>()

        // Kreuzförmiges Brett aufbauen
        for (row in 0..6) {
            val rowList = mutableListOf<Hole?>()
            for (col in 0..6) {
                // Kreuzform: nur mittlere 3 Spalten in Zeilen 0-1 und 5-6
                // und alle Spalten in Zeilen 2-4
                val hole = if ((row in 0..1 || row in 5..6) && col in 2..4) {
                    Hole(row, col, hasPeg = true)
                } else if (row in 2..4) {
                    Hole(row, col, hasPeg = true)
                } else {
                    null
                }
                rowList.add(hole)
            }
            boardList.add(rowList)
        }

        return boardList.toMutableStateList()
    }

    fun setupBoard() {
        board.value = createBoard()
        moveHistory.clear()
        isBeforeFirst.value = true
        selectedHole = null
        lastSelectedHole = null
        initialHoleRow = -1
        initialHoleCol = -1
        replayState.value = null
        replayJob?.cancel()
    }

    fun onHoleClick(row: Int, col: Int) {
        // Im Replay-Modus sind keine Klicks erlaubt
        if (isInReplayMode) return

        val hole = board.value.getOrNull(row)?.getOrNull(col) ?: return

        // Vor dem ersten Zug: Startstöpsel entfernen
        if (isBeforeFirst.value) {
            hole.hasPeg = false
            isBeforeFirst.value = false
            initialHoleRow = row
            initialHoleCol = col
            board.value = board.value.toMutableStateList() // Trigger Recomposition
            return
        }

        // Wenn bereits ausgewählt, abwählen
        if (hole.isSelected) {
            unselectAll()
            return
        }

        // Loch auswählen
        lastSelectedHole = selectedHole
        selectedHole = hole

        // Vorherige Auswahl zurücksetzen
        if (lastSelectedHole != null && lastSelectedHole != hole) {
            lastSelectedHole!!.isSelected = false
        }

        hole.isSelected = true
        board.value = board.value.toMutableStateList() // Trigger Recomposition

        // Wenn zwei Löcher ausgewählt sind, versuche Zug
        if (lastSelectedHole != null && lastSelectedHole != hole) {
            tryMove(lastSelectedHole!!, hole)
        }
    }

    private fun tryMove(from: Hole, to: Hole) {
        val rowDiff = to.row - from.row
        val colDiff = to.col - from.col

        // Nur horizontal oder vertikal, Distanz muss 2 sein
        if ((from.row == to.row || from.col == to.col) &&
            Math.abs(rowDiff) + Math.abs(colDiff) == 2) {

            val overRow = (from.row + to.row) / 2
            val overCol = (from.col + to.col) / 2
            val overHole = board.value.getOrNull(overRow)?.getOrNull(overCol)

            // Prüfe Zugbedingungen
            if (from.hasPeg && overHole?.hasPeg == true && !to.hasPeg) {
                // Führe Zug aus
                from.hasPeg = false
                overHole.hasPeg = false
                to.hasPeg = true

                // Speichere Zug für Undo
                moveHistory.add(Move(from.row, from.col, to.row, to.col))

                // Trigger Recomposition
                board.value = board.value.toMutableStateList()
            }
        }

        unselectAll()
    }

    private fun unselectAll() {
        selectedHole?.isSelected = false
        lastSelectedHole?.isSelected = false
        selectedHole = null
        lastSelectedHole = null
        board.value = board.value.toMutableStateList()
    }

    fun undoMove() {
        if (moveHistory.isEmpty()) return

        val move = moveHistory.removeAt(moveHistory.size - 1)
        val from = board.value.getOrNull(move.fromRow)?.getOrNull(move.fromCol)
        val to = board.value.getOrNull(move.toRow)?.getOrNull(move.toCol)
        val overRow = (move.fromRow + move.toRow) / 2
        val overCol = (move.fromCol + move.toCol) / 2
        val over = board.value.getOrNull(overRow)?.getOrNull(overCol)

        if (from != null && to != null && over != null) {
            from.hasPeg = true
            over.hasPeg = true
            to.hasPeg = false

            // Trigger Recomposition
            board.value = board.value.toMutableStateList()
        }

        unselectAll()
    }

    fun restart() {
        setupBoard()
    }

    fun getRemainingPegs(): Int {
        return board.value.sumOf { row ->
            row.count { hole -> hole?.hasPeg == true }
        }
    }

    // ========== Save/Load Funktionen ==========

    fun canSaveGame(): Boolean {
        return !isBeforeFirst.value && moveHistory.isNotEmpty() && !isInReplayMode
    }

    fun saveGame(title: String) {
        if (!canSaveGame()) return

        viewModelScope.launch {
            val savedGame = SavedGame(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                timestamp = System.currentTimeMillis(),
                initialHoleRow = initialHoleRow,
                initialHoleCol = initialHoleCol,
                moves = moveHistory.toList()
            )
            repository.insertGame(savedGame)
        }
    }

    fun getSavedGames(): Flow<List<SavedGame>> {
        return repository.getAllGames()
    }

    fun deleteSavedGame(savedGame: SavedGame) {
        viewModelScope.launch {
            repository.deleteGame(savedGame)
        }
    }

    // ========== Replay-Funktionen ==========

    fun startReplay(savedGame: SavedGame) {
        // Brett zurücksetzen
        board.value = createBoard()
        isBeforeFirst.value = false
        moveHistory.clear()
        selectedHole = null
        lastSelectedHole = null

        // Initial Loch entfernen
        val initialHole = board.value.getOrNull(savedGame.initialHoleRow)
            ?.getOrNull(savedGame.initialHoleCol)
        initialHole?.hasPeg = false
        board.value = board.value.toMutableStateList()

        // Replay-Status setzen
        replayState.value = ReplayState(
            savedGame = savedGame,
            currentMoveIndex = 0,
            isPlaying = true,
            speed = ReplaySpeed.NORMAL
        )

        // Replay starten
        startReplayLoop()
    }

    private fun startReplayLoop() {
        replayJob?.cancel()

        val state = replayState.value ?: return
        if (!state.isPlaying) return

        replayJob = viewModelScope.launch {
            while (replayState.value != null) {
                val currentState = replayState.value ?: break

                if (!currentState.isPlaying) break

                if (currentState.currentMoveIndex >= currentState.savedGame.moves.size) {
                    // Replay beendet
                    delay(1000) // Kurze Pause am Ende
                    replayState.value = currentState.copy(isPlaying = false)
                    break
                }

                val move = currentState.savedGame.moves[currentState.currentMoveIndex]

                // Peg als "animierend" markieren (wird rot)
                replayState.value = currentState.copy(
                    animatingPegRow = move.fromRow,
                    animatingPegCol = move.fromCol
                )

                // Halbe Verzögerung für Animation
                delay(currentState.speed.delayMs / 2)

                // Zug ausführen
                executeReplayMove(move)

                // Restliche Verzögerung
                delay(currentState.speed.delayMs / 2)

                // Animation beenden
                replayState.value = replayState.value?.copy(
                    currentMoveIndex = currentState.currentMoveIndex + 1,
                    animatingPegRow = null,
                    animatingPegCol = null
                )
            }
        }
    }

    private fun executeReplayMove(move: Move) {
        val from = board.value.getOrNull(move.fromRow)?.getOrNull(move.fromCol)
        val to = board.value.getOrNull(move.toRow)?.getOrNull(move.toCol)
        val overRow = (move.fromRow + move.toRow) / 2
        val overCol = (move.fromCol + move.toCol) / 2
        val over = board.value.getOrNull(overRow)?.getOrNull(overCol)

        if (from != null && to != null && over != null) {
            from.hasPeg = false
            over.hasPeg = false
            to.hasPeg = true
            board.value = board.value.toMutableStateList()
        }
    }

    fun pauseReplay() {
        replayJob?.cancel()
        replayState.value = replayState.value?.copy(
            isPlaying = false,
            animatingPegRow = null,
            animatingPegCol = null
        )
    }

    fun resumeReplay() {
        replayState.value = replayState.value?.copy(isPlaying = true)
        startReplayLoop()
    }

    fun stopReplay() {
        replayJob?.cancel()
        replayState.value = null
    }

    fun setReplaySpeed(speed: ReplaySpeed) {
        val currentState = replayState.value ?: return
        val wasPlaying = currentState.isPlaying

        replayState.value = currentState.copy(speed = speed)

        if (wasPlaying) {
            pauseReplay()
            resumeReplay()
        }
    }

    fun continueFromReplay() {
        // Aktuellen Stand als Ausgangspunkt für neues Spiel übernehmen
        val state = replayState.value ?: return

        // Moves bis zum aktuellen Index übernehmen
        moveHistory.clear()
        moveHistory.addAll(
            state.savedGame.moves.take(state.currentMoveIndex)
        )

        initialHoleRow = state.savedGame.initialHoleRow
        initialHoleCol = state.savedGame.initialHoleCol

        replayJob?.cancel()
        replayState.value = null
    }
}

