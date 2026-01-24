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

            // WAL Checkpoint durchführen um .db-shm und .db-wal Dateien zu konsolidieren
            try {
                val db = SavedGameDatabase.getDatabase(context)
                db.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
            } catch (e: Exception) {
                // Ignorieren wenn Checkpoint fehlschlägt
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

            // Kopiere nur die Haupt-DB-Datei
            dbFile.copyTo(exportFile, overwrite = true)

            // Lösche eventuelle .db-shm und .db-wal Dateien im Download-Ordner
            val shmFile = java.io.File(downloadsDir, "r_solitaire_backup_${timestamp}.db-shm")
            val walFile = java.io.File(downloadsDir, "r_solitaire_backup_${timestamp}.db-wal")
            if (shmFile.exists()) shmFile.delete()
            if (walFile.exists()) walFile.delete()

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

    suspend fun importDatabaseFromUri(uri: android.net.Uri): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val context = getApplication<Application>()

            android.util.Log.d("SolitaireVM", "DB Import via URI: ${uri.path}")

            // Zuerst alle existierenden Spiele löschen (über Repository, damit Room's Tracker funktioniert)
            val existingGames = repository.getAllGamesSync()
            for (game in existingGames) {
                repository.deleteGame(game)
            }

            // Temporäre Datei erstellen und DB-Inhalt kopieren
            val tempFile = java.io.File(context.cacheDir, "temp_import.db")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (!tempFile.exists() || tempFile.length() == 0L) {
                return@withContext "Fehler: Konnte DB-Datei nicht in Temp-Datei kopieren"
            }

            android.util.Log.d("SolitaireVM", "DB Import: Temp-Datei erstellt: ${tempFile.length()} bytes")

            // Temporär die importierte DB öffnen und Spiele extrahieren
            val importedGames = mutableListOf<SavedGame>()

            try {
                val sqliteDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                    tempFile.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                )

                val dbCursor = sqliteDb.rawQuery("SELECT * FROM saved_games ORDER BY timestamp DESC", null)

                while (dbCursor.moveToNext()) {
                    val id = dbCursor.getString(dbCursor.getColumnIndexOrThrow("id"))
                    val title = dbCursor.getString(dbCursor.getColumnIndexOrThrow("title"))
                    val timestamp = dbCursor.getLong(dbCursor.getColumnIndexOrThrow("timestamp"))
                    val initialHoleRow = dbCursor.getInt(dbCursor.getColumnIndexOrThrow("initialHoleRow"))
                    val initialHoleCol = dbCursor.getInt(dbCursor.getColumnIndexOrThrow("initialHoleCol"))
                    val movesJson = dbCursor.getString(dbCursor.getColumnIndexOrThrow("movesJson"))

                    // Parse moves
                    val movesData = kotlinx.serialization.json.Json.decodeFromString<List<com.rsinkwitz.r_solitaire.data.MoveData>>(movesJson)
                    val moves = movesData.map { com.rsinkwitz.r_solitaire.model.Move(it.fromRow, it.fromCol, it.toRow, it.toCol) }

                    importedGames.add(SavedGame(id, title, timestamp, initialHoleRow, initialHoleCol, moves))
                }

                dbCursor.close()
                sqliteDb.close()

                android.util.Log.d("SolitaireVM", "DB Import: ${importedGames.size} Spiele extrahiert")
            } catch (e: Exception) {
                e.printStackTrace()
                tempFile.delete()
                return@withContext "Fehler beim Lesen der Import-Datei:\n${e.message}"
            }

            // Temp-Datei löschen
            tempFile.delete()

            // Alle importierten Spiele in die aktuelle DB einfügen
            for (game in importedGames) {
                repository.saveGame(game)
            }

            "✓ ${importedGames.size} Spiel(e) aus DB importiert"
        } catch (e: Exception) {
            android.util.Log.e("SolitaireVM", "DB Import Fehler", e)
            e.printStackTrace()
            "Fehler beim DB-Import:\n${e.message}"
        }
    }

    suspend fun importDatabaseFromDownloads(filename: String = "r_solitaire_backup.db"): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val context = getApplication<Application>()

            // Suche Datei in mehreren möglichen Download-Ordnern
            val possibleDirs = mutableListOf<java.io.File>()

            // Standard Download-Ordner
            possibleDirs.add(java.io.File("/storage/emulated/0/Download"))
            possibleDirs.add(java.io.File("/storage/emulated/0/Downloads"))

            // Versuche auch Environment API
            try {
                @Suppress("DEPRECATION")
                val envDownload = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                if (envDownload != null) {
                    possibleDirs.add(envDownload)
                }
            } catch (e: Exception) {
                android.util.Log.d("SolitaireVM", "DB Import: Environment API nicht verfügbar")
            }

            var importFile: java.io.File? = null

            for (dir in possibleDirs) {
                if (!dir.exists() || !dir.canRead()) continue

                val testFile = java.io.File(dir, filename)
                android.util.Log.d("SolitaireVM", "DB Import: Prüfe ${testFile.absolutePath}, exists=${testFile.exists()}, canRead=${testFile.canRead()}")

                if (testFile.exists() && testFile.canRead()) {
                    importFile = testFile
                    android.util.Log.d("SolitaireVM", "DB Import: Datei gefunden: ${testFile.absolutePath}")
                    break
                }
            }

            if (importFile == null) {
                "Fehler: DB-Datei nicht gefunden\n\nDatei: ${filename}\n\nBitte im Download-Ordner ablegen."
            } else {
                // Zuerst alle existierenden Spiele löschen (über Repository, damit Room's Tracker funktioniert)
                val existingGames = repository.getAllGamesSync()
                for (game in existingGames) {
                    repository.deleteGame(game)
                }

                // Temporäre Datei erstellen und DB-Inhalt kopieren
                val tempFile = java.io.File(context.cacheDir, "temp_import.db")

                importFile.copyTo(tempFile, overwrite = true)

                if (!tempFile.exists() || tempFile.length() == 0L) {
                    return@withContext "Fehler: Konnte DB-Datei nicht in Temp-Datei kopieren"
                }

                // Temporär die importierte DB öffnen und Spiele extrahieren
                val importedGames = mutableListOf<SavedGame>()

                try {
                    val sqliteDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                        tempFile.absolutePath,
                        null,
                        android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                    )

                    val dbCursor = sqliteDb.rawQuery("SELECT * FROM saved_games ORDER BY timestamp DESC", null)

                    while (dbCursor.moveToNext()) {
                        val id = dbCursor.getString(dbCursor.getColumnIndexOrThrow("id"))
                        val title = dbCursor.getString(dbCursor.getColumnIndexOrThrow("title"))
                        val timestamp = dbCursor.getLong(dbCursor.getColumnIndexOrThrow("timestamp"))
                        val initialHoleRow = dbCursor.getInt(dbCursor.getColumnIndexOrThrow("initialHoleRow"))
                        val initialHoleCol = dbCursor.getInt(dbCursor.getColumnIndexOrThrow("initialHoleCol"))
                        val movesJson = dbCursor.getString(dbCursor.getColumnIndexOrThrow("movesJson"))

                        // Parse moves
                        val movesData = kotlinx.serialization.json.Json.decodeFromString<List<com.rsinkwitz.r_solitaire.data.MoveData>>(movesJson)
                        val moves = movesData.map { com.rsinkwitz.r_solitaire.model.Move(it.fromRow, it.fromCol, it.toRow, it.toCol) }

                        importedGames.add(SavedGame(id, title, timestamp, initialHoleRow, initialHoleCol, moves))
                    }

                    dbCursor.close()
                    sqliteDb.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    tempFile.delete()
                    return@withContext "Fehler beim Lesen der Import-Datei:\n${e.message}"
                }

                // Temp-Datei löschen
                tempFile.delete()

                // Alle importierten Spiele in die aktuelle DB einfügen
                for (game in importedGames) {
                    repository.saveGame(game)
                }

                "✓ ${importedGames.size} Spiel(e) aus DB importiert: ${filename}"
            }
        } catch (e: Exception) {
            android.util.Log.e("SolitaireVM", "DB Import Fehler", e)
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
            val allFiles = mutableSetOf<String>()

            android.util.Log.d("SolitaireVM", "YAML: Starte Suche in Download-Ordnern")

            // Mehrere mögliche Download-Ordner durchsuchen
            val possibleDirs = mutableListOf<java.io.File>()

            // Standard Download-Ordner
            possibleDirs.add(java.io.File("/storage/emulated/0/Download"))
            possibleDirs.add(java.io.File("/storage/emulated/0/Downloads"))

            // Versuche auch Environment API (funktioniert auf älteren Android-Versionen)
            try {
                @Suppress("DEPRECATION")
                val envDownload = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                if (envDownload != null) {
                    possibleDirs.add(envDownload)
                }
            } catch (e: Exception) {
                android.util.Log.d("SolitaireVM", "YAML: Environment API nicht verfügbar: ${e.message}")
            }

            for (dir in possibleDirs) {
                if (!dir.exists()) {
                    android.util.Log.d("SolitaireVM", "YAML: Ordner existiert nicht: ${dir.absolutePath}")
                    continue
                }

                if (!dir.canRead()) {
                    android.util.Log.d("SolitaireVM", "YAML: Keine Leseberechtigung: ${dir.absolutePath}")
                    continue
                }

                android.util.Log.d("SolitaireVM", "YAML: Durchsuche: ${dir.absolutePath}")

                val files = try {
                    dir.listFiles { file ->
                        file.isFile &&
                        file.name.startsWith("r_solitaire_games") &&
                        file.name.endsWith(".yaml")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SolitaireVM", "YAML: Fehler beim Lesen von ${dir.absolutePath}", e)
                    null
                }

                files?.forEach { file ->
                    android.util.Log.d("SolitaireVM", "YAML: ✓ Gefunden: ${file.name} (${file.length()} bytes)")
                    allFiles.add(file.name)
                }
            }

            android.util.Log.d("SolitaireVM", "YAML: ===== ERGEBNIS: ${allFiles.size} passende Dateien gefunden =====")
            allFiles.forEach { filename ->
                android.util.Log.d("SolitaireVM", "YAML: - $filename")
            }

            allFiles.sortedByDescending { it }
        } catch (e: Exception) {
            android.util.Log.e("SolitaireVM", "YAML: Fehler beim Suchen", e)
            e.printStackTrace()
            emptyList()
        }
    }

    fun getAvailableDbFiles(): List<String> {
        return try {
            val context = getApplication<Application>()
            val allFiles = mutableSetOf<String>()

            android.util.Log.d("SolitaireVM", "DB: Starte Suche in Download-Ordnern")

            // Mehrere mögliche Download-Ordner durchsuchen
            val possibleDirs = mutableListOf<java.io.File>()

            // Standard Download-Ordner
            possibleDirs.add(java.io.File("/storage/emulated/0/Download"))
            possibleDirs.add(java.io.File("/storage/emulated/0/Downloads"))

            // Versuche auch Environment API (funktioniert auf älteren Android-Versionen)
            try {
                @Suppress("DEPRECATION")
                val envDownload = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                if (envDownload != null) {
                    possibleDirs.add(envDownload)
                }
            } catch (e: Exception) {
                android.util.Log.d("SolitaireVM", "DB: Environment API nicht verfügbar: ${e.message}")
            }

            for (dir in possibleDirs) {
                if (!dir.exists()) {
                    android.util.Log.d("SolitaireVM", "DB: Ordner existiert nicht: ${dir.absolutePath}")
                    continue
                }

                if (!dir.canRead()) {
                    android.util.Log.d("SolitaireVM", "DB: Keine Leseberechtigung: ${dir.absolutePath}")
                    continue
                }

                android.util.Log.d("SolitaireVM", "DB: Durchsuche: ${dir.absolutePath}")

                val files = try {
                    dir.listFiles { file ->
                        file.isFile &&
                        file.name.startsWith("r_solitaire_backup") &&
                        file.name.endsWith(".db")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SolitaireVM", "DB: Fehler beim Lesen von ${dir.absolutePath}", e)
                    null
                }

                files?.forEach { file ->
                    android.util.Log.d("SolitaireVM", "DB: ✓ Gefunden: ${file.name} (${file.length()} bytes)")
                    allFiles.add(file.name)
                }
            }

            android.util.Log.d("SolitaireVM", "DB: ===== ERGEBNIS: ${allFiles.size} passende Dateien gefunden =====")
            allFiles.forEach { filename ->
                android.util.Log.d("SolitaireVM", "DB: - $filename")
            }

            allFiles.sortedByDescending { it }
        } catch (e: Exception) {
            android.util.Log.e("SolitaireVM", "DB: Fehler beim Suchen", e)
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

    suspend fun importGamesFromYamlUri(uri: android.net.Uri): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val context = getApplication<Application>()

            // Lese Datei über ContentResolver (funktioniert mit SAF)
            val yaml = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@withContext "Fehler: Datei konnte nicht gelesen werden"

            android.util.Log.d("SolitaireVM", "YAML Import via URI: ${uri.path}, Länge: ${yaml.length}")

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
                        uniqueTitle = "${game.title}-${String.format(java.util.Locale.US, "%02d", suffix)}"
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

                "YAML Import erfolgreich!\n\n${importedGames.size} Spiel(e) importiert\n\nSpiele wurden angehängt."
            }
        } catch (e: Exception) {
            android.util.Log.e("SolitaireVM", "YAML Import Fehler", e)
            e.printStackTrace()
            "Fehler beim YAML-Import:\n${e.message}"
        }
    }

    suspend fun importGamesFromYaml(filename: String = "r_solitaire_games.yaml"): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val context = getApplication<Application>()

            // Suche Datei in mehreren möglichen Download-Ordnern
            val possibleDirs = mutableListOf<java.io.File>()

            // Standard Download-Ordner
            possibleDirs.add(java.io.File("/storage/emulated/0/Download"))
            possibleDirs.add(java.io.File("/storage/emulated/0/Downloads"))

            // Versuche auch Environment API
            try {
                @Suppress("DEPRECATION")
                val envDownload = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                if (envDownload != null) {
                    possibleDirs.add(envDownload)
                }
            } catch (e: Exception) {
                android.util.Log.d("SolitaireVM", "YAML Import: Environment API nicht verfügbar")
            }

            var importFile: java.io.File? = null

            for (dir in possibleDirs) {
                if (!dir.exists() || !dir.canRead()) continue

                val testFile = java.io.File(dir, filename)
                android.util.Log.d("SolitaireVM", "YAML Import: Prüfe ${testFile.absolutePath}, exists=${testFile.exists()}, canRead=${testFile.canRead()}")

                if (testFile.exists() && testFile.canRead()) {
                    importFile = testFile
                    android.util.Log.d("SolitaireVM", "YAML Import: Datei gefunden: ${testFile.absolutePath}")
                    break
                }
            }

            if (importFile == null) {
                "Fehler: YAML-Datei nicht gefunden\n\nDatei: ${filename}\n\nBitte im Download-Ordner ablegen."
            } else {
                // Lese Datei direkt
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
                            uniqueTitle = "${game.title}-${String.format(java.util.Locale.US, "%02d", suffix)}"
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

                    "YAML Import erfolgreich!\n\n${importedGames.size} Spiel(e) importiert\n\nDatei:\n${filename}\n\nSpiele wurden angehängt."
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SolitaireVM", "YAML Import Fehler", e)
            e.printStackTrace()
            "Fehler beim YAML-Import:\n${e.message}"
        }
    }
}

