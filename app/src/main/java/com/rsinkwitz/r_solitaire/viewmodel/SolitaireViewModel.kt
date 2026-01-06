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
import com.rsinkwitz.r_solitaire.util.SoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class SolitaireViewModel(application: Application) : AndroidViewModel(application) {

    // Repository für gespeicherte Spiele
    private val repository: SavedGameRepository

    // Sound Player für Gratulations-Sound
    private val soundPlayer: SoundPlayer

    init {
        val database = SavedGameDatabase.getDatabase(application)
        repository = SavedGameRepository(database.savedGameDao())
        soundPlayer = SoundPlayer(application)
    }

    // 7x7 Board mit Kreuzform
    var board = mutableStateOf<SnapshotStateList<List<Hole?>>>(createBoard())
        private set

    // Spielzustand
    var isBeforeFirst = mutableStateOf(true)
        private set

    // Gewinn-Status
    var hasWon = mutableStateOf(false)
        private set

    // Initial entfernter Stöpsel (für Save-Funktion)
    private var initialHoleRow: Int = -1
    private var initialHoleCol: Int = -1

    // Prüft ob eine Position das Start-Loch ist (für grünen Rand)
    fun isInitialHole(row: Int, col: Int): Boolean {
        return initialHoleRow == row && initialHoleCol == col && initialHoleRow >= 0
    }

    private var selectedHole: Hole? = null
    private var lastSelectedHole: Hole? = null

    // Zug-Historie für Undo
    private val moveHistory = mutableListOf<Move>()

    // State für Anzahl der Züge (für automatische UI-Updates)
    var moveHistorySize = mutableStateOf(0)
        private set

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
        moveHistorySize.value = 0
        isBeforeFirst.value = true
        hasWon.value = false
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
                moveHistorySize.value = moveHistory.size

                // Trigger Recomposition
                board.value = board.value.toMutableStateList()

                // Prüfe Gewinn-Bedingung
                checkWinCondition()
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
        moveHistorySize.value = moveHistory.size

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

    private fun checkWinCondition() {
        // Gewonnen wenn: 1 Peg übrig UND dieser im initialen Startloch
        if (getRemainingPegs() == 1 && !isBeforeFirst.value && initialHoleRow >= 0) {
            val initialHole = board.value.getOrNull(initialHoleRow)?.getOrNull(initialHoleCol)
            if (initialHole?.hasPeg == true && !hasWon.value) {
                hasWon.value = true
                // Gratulations-Sound abspielen
                soundPlayer.playCongratulationsSound()
            }
        }
    }

    fun dismissWinDialog() {
        hasWon.value = false
    }

    fun testCongratulationsSound() {
        soundPlayer.playCongratulationsSound()
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
        moveHistorySize.value = 0
        selectedHole = null
        lastSelectedHole = null

        // Initial entfernten Stöpsel merken
        initialHoleRow = savedGame.initialHoleRow
        initialHoleCol = savedGame.initialHoleCol

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
                    // Replay beendet - automatisch in "Weiterspielen"-Modus wechseln
                    delay(1000) // Kurze Pause am Ende
                    continueFromReplay()
                    break
                }

                val move = currentState.savedGame.moves[currentState.currentMoveIndex]

                // === PHASE 1: Peg wird rot und steht still ===
                replayState.value = currentState.copy(
                    animatingPegFromRow = move.fromRow,
                    animatingPegFromCol = move.fromCol,
                    animatingPegToRow = move.toRow,
                    animatingPegToCol = move.toCol,
                    animationProgress = 0f
                )
                delay(currentState.speed.delayMs / 4)  // 25% - Stillstand am Start

                // === PHASE 2: Flüssige Bewegung zum Ziel ===
                val animationSteps = 20  // Anzahl der Animations-Frames
                val stepDelay = (currentState.speed.delayMs / 2) / animationSteps

                for (step in 1..animationSteps) {
                    val progress = step.toFloat() / animationSteps
                    replayState.value = replayState.value?.copy(
                        animationProgress = progress
                    )
                    delay(stepDelay)
                }

                // === PHASE 3: Stillstand am Ziel (noch rot) ===
                delay(currentState.speed.delayMs / 4)  // 25% - Stillstand am Ende

                // === PHASE 4: Zug ausführen und zurück zu blau ===
                executeReplayMove(move)

                // Animation beenden
                replayState.value = replayState.value?.copy(
                    currentMoveIndex = currentState.currentMoveIndex + 1,
                    animatingPegFromRow = null,
                    animatingPegFromCol = null,
                    animatingPegToRow = null,
                    animatingPegToCol = null,
                    animationProgress = 0f
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

            // Zug zur Historie hinzufügen (für continueFromReplay)
            moveHistory.add(move)
            moveHistorySize.value = moveHistory.size

            board.value = board.value.toMutableStateList()

            // Prüfe Gewinn-Bedingung auch während Replay
            checkWinCondition()
        }
    }

    fun pauseReplay() {
        replayJob?.cancel()
        replayState.value = replayState.value?.copy(
            isPlaying = false,
            animatingPegFromRow = null,
            animatingPegFromCol = null,
            animatingPegToRow = null,
            animatingPegToCol = null,
            animationProgress = 0f
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
        moveHistorySize.value = moveHistory.size

        initialHoleRow = state.savedGame.initialHoleRow
        initialHoleCol = state.savedGame.initialHoleCol

        replayJob?.cancel()
        replayState.value = null
    }

    // ========== Export/Import für Backup ==========

    fun exportDatabaseToDownloads(): String {
        return try {
            val context = getApplication<Application>()
            val dbFile = context.getDatabasePath("saved_games_database")

            if (!dbFile.exists()) {
                return "Fehler: Datenbank nicht gefunden bei: ${dbFile.absolutePath}"
            }

            // Export direkt in öffentlichen Download-Ordner mit Datum/Zeit
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val exportFile = java.io.File(downloadsDir, "r_solitaire_backup_${timestamp}.db")

            // Kopiere Datenbank
            dbFile.copyTo(exportFile, overwrite = true)

            if (exportFile.exists() && exportFile.length() > 0) {
                "✓ DB exportiert nach Download-Ordner: ${exportFile.name}"
            } else {
                "Fehler: Datei wurde nicht erstellt"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Fehler beim DB-Export:\n${e.message}\n\nFalls 'Permission denied':\nBitte erlauben Sie Storage-Zugriff in den App-Einstellungen."
        }
    }

    suspend fun importDatabaseFromDownloads(filename: String = "r_solitaire_backup.db"): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val context = getApplication<Application>()
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )

            // Suche nach neuester DB-Datei wenn kein spezifischer Dateiname angegeben
            val importFile = if (filename == "r_solitaire_backup.db") {
                val dbFiles = downloadsDir.listFiles { file ->
                    file.name.startsWith("r_solitaire_backup_") && file.name.endsWith(".db")
                }?.sortedByDescending { it.lastModified() }

                dbFiles?.firstOrNull() ?: java.io.File(downloadsDir, filename)
            } else {
                java.io.File(downloadsDir, filename)
            }

            if (!importFile.exists()) {
                "Fehler: DB-Datei nicht gefunden\n\nErwartet:\n/storage/emulated/0/Download/${importFile.name}"
            } else {
                // Zuerst alle existierenden Spiele löschen (über Repository, damit Room's Tracker funktioniert)
                val existingGames = repository.getAllGamesSync()
                for (game in existingGames) {
                    repository.deleteGame(game)
                }

                // Jetzt importierte Spiele laden und einfügen
                val importedDbFile = importFile

                // Temporär die importierte DB öffnen und Spiele extrahieren
                // (Wir verwenden einen eigenen SQLite-Reader statt Room)
                val importedGames = mutableListOf<SavedGame>()

                try {
                    val sqliteDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                        importedDbFile.absolutePath,
                        null,
                        android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                    )

                    val cursor = sqliteDb.rawQuery("SELECT * FROM saved_games ORDER BY timestamp DESC", null)

                    while (cursor.moveToNext()) {
                        val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                        val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                        val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                        val initialHoleRow = cursor.getInt(cursor.getColumnIndexOrThrow("initialHoleRow"))
                        val initialHoleCol = cursor.getInt(cursor.getColumnIndexOrThrow("initialHoleCol"))
                        val movesJson = cursor.getString(cursor.getColumnIndexOrThrow("movesJson"))

                        // Parse moves
                        val movesData = kotlinx.serialization.json.Json.decodeFromString<List<com.rsinkwitz.r_solitaire.data.MoveData>>(movesJson)
                        val moves = movesData.map { com.rsinkwitz.r_solitaire.model.Move(it.fromRow, it.fromCol, it.toRow, it.toCol) }

                        importedGames.add(SavedGame(id, title, timestamp, initialHoleRow, initialHoleCol, moves))
                    }

                    cursor.close()
                    sqliteDb.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@withContext "Fehler beim Lesen der Import-Datei:\n${e.message}"
                }

                // Alle importierten Spiele in die aktuelle DB einfügen
                for (game in importedGames) {
                    repository.saveGame(game)
                }

                "DB Import erfolgreich!\n\n${importedGames.size} Spiel(e) importiert\n\nDatei:\n${importFile.name}\n\nSchließen Sie den Dialog, um die Liste zu aktualisieren."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Fehler beim DB-Import:\n${e.message}"
        }
    }

    fun getDatabasePath(): String {
        val context = getApplication<Application>()
        return context.getDatabasePath("saved_games_database").absolutePath
    }

    // ========== Alle Spiele löschen ==========

    suspend fun deleteAllGames(): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val games = repository.getAllGamesSync()
            val count = games.size

            // Lösche alle Spiele
            for (game in games) {
                repository.deleteGame(game)
            }

            "Alle Spiele gelöscht!\n\n$count Spiel(e) wurden entfernt."
        } catch (e: Exception) {
            e.printStackTrace()
            "Fehler beim Löschen:\n${e.message}"
        }
    }

    // ========== Datei-Listen für Import ==========

    fun getAvailableYamlFiles(): List<String> {
        return try {
            val context = getApplication<Application>()

            // Suche in mehreren möglichen Download-Ordnern
            val possibleDirs = listOf(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                java.io.File(android.os.Environment.getExternalStorageDirectory(), "Download"),
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            )

            val allFiles = mutableSetOf<String>()

            for (dir in possibleDirs) {
                if (dir == null || !dir.exists()) continue

                val yamlFiles = dir.listFiles { file ->
                    file.name.startsWith("r_solitaire_games") && file.name.endsWith(".yaml")
                }

                yamlFiles?.forEach { file ->
                    allFiles.add(file.name)
                }
            }

            allFiles.sortedByDescending { it }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getAvailableDbFiles(): List<String> {
        return try {
            val context = getApplication<Application>()

            // Suche in mehreren möglichen Download-Ordnern
            val possibleDirs = listOf(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                java.io.File(android.os.Environment.getExternalStorageDirectory(), "Download"),
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            )

            val allFiles = mutableSetOf<String>()

            for (dir in possibleDirs) {
                if (dir == null || !dir.exists()) continue

                val dbFiles = dir.listFiles { file ->
                    file.name.startsWith("r_solitaire_backup") && file.name.endsWith(".db")
                }

                dbFiles?.forEach { file ->
                    allFiles.add(file.name)
                }
            }

            allFiles.sortedByDescending { it }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ========== YAML Export/Import (menschenlesbar) ==========

    suspend fun exportGamesToYaml(): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val context = getApplication<Application>()
            val games = repository.getAllGamesSync()

            if (games.isEmpty()) {
                return@withContext "Fehler: Keine gespeicherten Spiele zum Exportieren"
            }

            val yaml = com.rsinkwitz.r_solitaire.util.YamlExporter.exportToYaml(games)

            // Speichere in Download-Ordner mit Datum/Zeit
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val filename = "r_solitaire_games_${timestamp}.yaml"

            // Versuche verschiedene Download-Pfade
            val possibleDirs = listOf(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                java.io.File(android.os.Environment.getExternalStorageDirectory(), "Download"),
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            )

            var exportFile: java.io.File? = null
            var successPath: String? = null

            for (dir in possibleDirs) {
                if (dir == null) continue

                try {
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }

                    val testFile = java.io.File(dir, filename)
                    testFile.writeText(yaml)

                    if (testFile.exists() && testFile.length() > 0) {
                        exportFile = testFile
                        successPath = testFile.absolutePath
                        break
                    }
                } catch (e: Exception) {
                    // Versuche nächsten Pfad
                    continue
                }
            }

            if (exportFile != null && successPath != null) {
                "✓ ${games.size} Spiel(e) exportiert nach Download-Ordner: ${exportFile.name}"
            } else {
                "Fehler: YAML-Datei konnte nicht erstellt werden\n\nVersucht wurden alle Download-Ordner.\n\nBitte erlauben Sie Storage-Zugriff in den App-Einstellungen."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Fehler beim YAML-Export:\n${e.message}"
        }
    }

    suspend fun importGamesFromYaml(filename: String = "r_solitaire_games.yaml"): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val context = getApplication<Application>()

            // Suche Datei in mehreren möglichen Download-Ordnern
            val possibleDirs = listOf(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                java.io.File(android.os.Environment.getExternalStorageDirectory(), "Download"),
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            )

            var importFile: java.io.File? = null

            for (dir in possibleDirs) {
                if (dir == null || !dir.exists()) continue

                val testFile = java.io.File(dir, filename)
                if (testFile.exists()) {
                    importFile = testFile
                    break
                }

                // Falls kein spezifischer Name: Suche neueste Datei
                if (filename == "r_solitaire_games.yaml") {
                    val yamlFiles = dir.listFiles { file ->
                        file.name.startsWith("r_solitaire_games_") && file.name.endsWith(".yaml")
                    }?.sortedByDescending { it.lastModified() }

                    if (yamlFiles?.isNotEmpty() == true) {
                        importFile = yamlFiles.first()
                        break
                    }
                }
            }

            if (importFile == null || !importFile.exists()) {
                "Fehler: YAML-Datei nicht gefunden\n\nDatei: ${filename}\n\nGesucht in allen Download-Ordnern."
            } else {
                val yaml = importFile.readText()
                val importedGames = com.rsinkwitz.r_solitaire.util.YamlExporter.importFromYaml(yaml)

                if (importedGames.isEmpty()) {
                    "Fehler: Keine Spiele in YAML gefunden\nBitte Format prüfen"
                } else {
                    // Hole bestehende Titel
                    val existingGames = repository.getAllGamesSync()
                    val existingTitles = existingGames.map { it.title }.toSet()

                    // Speichere importierte Spiele mit eindeutigen Titeln
                    for (game in importedGames) {
                        var uniqueTitle = game.title
                        var suffix = 1

                        // Bei Konflikt: Anhänge -01, -02, etc.
                        while (existingTitles.contains(uniqueTitle)) {
                            uniqueTitle = "${game.title}-${String.format("%02d", suffix)}"
                            suffix++
                        }

                        val gameToSave = if (uniqueTitle != game.title) {
                            game.copy(
                                id = java.util.UUID.randomUUID().toString(),
                                title = uniqueTitle
                            )
                        } else {
                            game.copy(id = java.util.UUID.randomUUID().toString())
                        }

                        repository.saveGame(gameToSave)
                    }

                    "YAML Import erfolgreich!\n\n${importedGames.size} Spiel(e) importiert\n\nDatei:\n${importFile.name}\n\nSpiele wurden angehängt."
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Fehler beim YAML-Import:\n${e.message}"
        }
    }
}

