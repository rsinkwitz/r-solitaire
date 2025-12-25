package com.rsinkwitz.r_solitaire.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.rsinkwitz.r_solitaire.model.Hole
import com.rsinkwitz.r_solitaire.model.Move

class SolitaireViewModel : ViewModel() {

    // 7x7 Board mit Kreuzform
    var board = mutableStateOf<SnapshotStateList<List<Hole?>>>(createBoard())
        private set

    // Spielzustand
    var isBeforeFirst = mutableStateOf(true)
        private set

    private var selectedHole: Hole? = null
    private var lastSelectedHole: Hole? = null

    // Zug-Historie für Undo
    private val moveHistory = mutableListOf<Move>()

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
    }

    fun onHoleClick(row: Int, col: Int) {
        val hole = board.value.getOrNull(row)?.getOrNull(col) ?: return

        // Vor dem ersten Zug: Startstöpsel entfernen
        if (isBeforeFirst.value) {
            hole.hasPeg = false
            isBeforeFirst.value = false
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
}

