package com.rsinkwitz.r_solitaire.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rsinkwitz.r_solitaire.model.ReplaySpeed
import com.rsinkwitz.r_solitaire.viewmodel.SolitaireViewModel
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolitaireScreen(
    viewModel: SolitaireViewModel = viewModel()
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    var showStopReplayDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val savedGames by viewModel.getSavedGames().collectAsState(initial = emptyList())
    val replayState = viewModel.replayState.value
    val isInReplayMode = viewModel.isInReplayMode
    val hasWon = viewModel.hasWon.value

    // Coroutine scope for async operations
    val coroutineScope = rememberCoroutineScope()

    // State für verfügbare Export-Dateien (wird nach jedem Export aktualisiert)
    var availableYamlFiles by remember { mutableStateOf<List<String>>(emptyList()) }
    var availableDbFiles by remember { mutableStateOf<List<String>>(emptyList()) }

    // Lade verfügbare Dateien beim Öffnen des Dialogs
    LaunchedEffect(showLoadDialog) {
        if (showLoadDialog) {
            availableYamlFiles = viewModel.getAvailableYamlFiles()
            availableDbFiles = viewModel.getAvailableDbFiles()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isInReplayMode) {
                        "Replay: ${replayState?.savedGame?.title ?: ""}"
                    } else {
                        "Solitaire"
                    })
                },
                actions = {
                    if (isInReplayMode) {
                        // Replay Controls
                        IconButton(
                            onClick = {
                                if (replayState?.isPlaying == true) {
                                    viewModel.pauseReplay()
                                } else {
                                    viewModel.resumeReplay()
                                }
                            }
                        ) {
                            Icon(
                                if (replayState?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (replayState?.isPlaying == true) "Pause" else "Play"
                            )
                        }

                        IconButton(onClick = { showStopReplayDialog = true }) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                        }

                        IconButton(
                            onClick = {
                                replayState?.speed?.next()?.let { viewModel.setReplaySpeed(it) }
                            }
                        ) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = when (replayState?.speed) {
                                        ReplaySpeed.SLOW -> "1x"
                                        ReplaySpeed.NORMAL -> "2x"
                                        ReplaySpeed.FAST -> "3x"
                                        else -> "2x"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    } else {
                        // Normal Game Controls
                        IconButton(onClick = { viewModel.undoMove() }) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Zurück")
                        }
                        IconButton(onClick = { showRestartDialog = true }) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Neustart")
                        }

                        // Save-Button - enabled basiert auf moveHistorySize State
                        val canSave = !viewModel.isBeforeFirst.value &&
                                     viewModel.moveHistorySize.value > 0 &&
                                     !viewModel.isInReplayMode
                        IconButton(
                            onClick = { showSaveDialog = true },
                            enabled = canSave
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Speichern")
                        }
                        IconButton(onClick = { showLoadDialog = true }) {
                            Icon(Icons.Default.List, contentDescription = "Spiele laden")
                        }
                    }
                }
            )
        },
        snackbarHost = {
            saveSuccessMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(message)
                }
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(2000)
                    saveSuccessMessage = null
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titel
            Text(
                text = "R-Solitaire",
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            // Info-Text / Replay Progress
            if (isInReplayMode && replayState != null) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Zug ${replayState.currentMoveIndex} von ${replayState.savedGame.moves.size}",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Verbleibende Stöpsel: ${viewModel.getRemainingPegs()}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    LinearProgressIndicator(
                        progress = {
                            if (replayState.savedGame.moves.isEmpty()) 0f
                            else replayState.currentMoveIndex.toFloat() / replayState.savedGame.moves.size
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            } else if (viewModel.isBeforeFirst.value) {
                Text(
                    text = "Wähle ein Loch zum Entfernen des ersten Stöpsels",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Verbleibende Stöpsel: ${viewModel.getRemainingPegs()}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            // Spielbrett
            SolitaireBoard(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            )

            // Copyright (klickbar für Sound-Test)
            Text(
                text = "© 2025 By Rainer",
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable { viewModel.testCongratulationsSound() },
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }

    // Dialogs
    if (showSaveDialog) {
        SaveGameDialog(
            remainingPegs = viewModel.getRemainingPegs(),
            onDismiss = { showSaveDialog = false },
            onSave = { title ->
                viewModel.saveGame(title)
                saveSuccessMessage = "Spiel '$title' gespeichert"
            }
        )
    }

    if (showLoadDialog) {
        LoadGameDialog(
            savedGames = savedGames,
            onDismiss = { showLoadDialog = false },
            onReplay = { game ->
                viewModel.startReplay(game)
                showLoadDialog = false
            },
            onDelete = { game ->
                viewModel.deleteSavedGame(game)
            },
            onExportYaml = {
                coroutineScope.launch {
                    val result = viewModel.exportGamesToYaml()
                    saveSuccessMessage = result
                    // Liste der verfügbaren YAML-Dateien aktualisieren
                    availableYamlFiles = viewModel.getAvailableYamlFiles()
                }
            },
            onImportYaml = { uri ->
                coroutineScope.launch {
                    val result = viewModel.importGamesFromYamlUri(uri)
                    saveSuccessMessage = result
                }
            },
            onExportDb = {
                val result = viewModel.exportDatabaseToDownloads()
                saveSuccessMessage = result
                // Liste der verfügbaren DB-Dateien aktualisieren
                availableDbFiles = viewModel.getAvailableDbFiles()
            },
            onImportDb = { uri ->
                coroutineScope.launch {
                    val result = viewModel.importDatabaseFromUri(uri)
                    saveSuccessMessage = result
                }
            },
            onDeleteAll = {
                coroutineScope.launch {
                    val result = viewModel.deleteAllGames()
                    saveSuccessMessage = result
                }
            },
            availableYamlFiles = availableYamlFiles,
            availableDbFiles = availableDbFiles
        )
    }

    if (showStopReplayDialog) {
        AlertDialog(
            onDismissRequest = { showStopReplayDialog = false },
            title = { Text("Replay beenden") },
            text = { Text("Möchten Sie von hier weiterspielen oder das Spiel verwerfen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.continueFromReplay()
                        showStopReplayDialog = false
                    }
                ) {
                    Text("Weiterspielen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.stopReplay()
                        viewModel.restart()
                        showStopReplayDialog = false
                    }
                ) {
                    Text("Verwerfen")
                }
            }
        )
    }

    // Neustart-Bestätigungsdialog
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Neustart bestätigen") },
            text = { Text("Möchten Sie wirklich neu starten? Das aktuelle Spiel geht verloren.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restart()
                        showRestartDialog = false
                    }
                ) {
                    Text("Neustart")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Gratulations-Dialog bei Gewinn
    if (hasWon) {
        CongratulationsDialog(
            onDismiss = { viewModel.dismissWinDialog() }
        )
    }
}

@Composable
fun SolitaireBoard(
    viewModel: SolitaireViewModel,
    modifier: Modifier = Modifier
) {
    val replayState = viewModel.replayState.value

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints {
            val size = min(maxWidth.value, maxHeight.value).dp
            val cellSize = size / 7

            Canvas(
                modifier = Modifier
                    .size(size)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val col = (offset.x / cellSize.toPx()).toInt()
                            val row = (offset.y / cellSize.toPx()).toInt()
                            if (row in 0..6 && col in 0..6) {
                                viewModel.onHoleClick(row, col)
                            }
                        }
                    }
            ) {
                val cellSizePx = cellSize.toPx()
                val pegInset = 4.dp.toPx()
                val currentBoard = viewModel.board.value

                // Zuerst alle Löcher und statischen Pegs zeichnen
                for (row in 0..6) {
                    for (col in 0..6) {
                        val hole = currentBoard.getOrNull(row)?.getOrNull(col)
                        if (hole != null) {
                            val x = col * cellSizePx
                            val y = row * cellSizePx
                            val center = Offset(
                                x + cellSizePx / 2,
                                y + cellSizePx / 2
                            )
                            val radius = (cellSizePx - 2 * pegInset) / 2

                            // Ist dies der Startpunkt der Animation? Dann nicht zeichnen (wird separat animiert)
                            val isAnimationStart = replayState?.animatingPegFromRow == row &&
                                                   replayState?.animatingPegFromCol == col

                            // Ist dies das Start-Loch? Dann grüner Rand
                            val isInitialHole = viewModel.isInitialHole(row, col)

                            when {
                                !hole.hasPeg -> {
                                    // Leeres Loch (grüner Ring für Start-Loch, sonst blau)
                                    drawCircle(
                                        color = if (isInitialHole) Color.Green else Color.Blue,
                                        radius = radius,
                                        center = center,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                                isAnimationStart -> {
                                    // Wird separat als animierender Peg gezeichnet - hier nur leeres Loch zeichnen
                                    drawCircle(
                                        color = if (isInitialHole) Color.Green else Color.Blue,
                                        radius = radius,
                                        center = center,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                                hole.isSelected -> {
                                    // Ausgewählter Stöpsel (rot gefüllt)
                                    drawCircle(
                                        color = Color.Red,
                                        radius = radius,
                                        center = center
                                    )
                                }
                                else -> {
                                    // Normaler Stöpsel (blau gefüllt)
                                    drawCircle(
                                        color = Color.Blue,
                                        radius = radius,
                                        center = center
                                    )
                                }
                            }
                        }
                    }
                }

                // Animierter roter Peg separat zeichnen (über allen anderen)
                if (replayState != null &&
                    replayState.animatingPegFromRow != null &&
                    replayState.animatingPegFromCol != null &&
                    replayState.animatingPegToRow != null &&
                    replayState.animatingPegToCol != null) {

                    // Start- und Ziel-Position berechnen
                    val fromX = replayState.animatingPegFromCol * cellSizePx + cellSizePx / 2
                    val fromY = replayState.animatingPegFromRow * cellSizePx + cellSizePx / 2
                    val toX = replayState.animatingPegToCol * cellSizePx + cellSizePx / 2
                    val toY = replayState.animatingPegToRow * cellSizePx + cellSizePx / 2

                    // Lineare Interpolation basierend auf animationProgress
                    val currentX = fromX + (toX - fromX) * replayState.animationProgress
                    val currentY = fromY + (toY - fromY) * replayState.animationProgress

                    val animCenter = Offset(currentX, currentY)
                    val radius = (cellSizePx - 2 * pegInset) / 2

                    // Roter animierter Peg
                    drawCircle(
                        color = Color.Red,
                        radius = radius,
                        center = animCenter
                    )
                }
            }
        }
    }
}

