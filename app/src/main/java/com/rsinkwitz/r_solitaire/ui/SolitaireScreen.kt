package com.rsinkwitz.r_solitaire.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolitaireScreen(
    viewModel: SolitaireViewModel = viewModel()
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    var showStopReplayDialog by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val savedGames by viewModel.getSavedGames().collectAsState(initial = emptyList())
    val replayState = viewModel.replayState.value
    val isInReplayMode = viewModel.isInReplayMode

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
                        IconButton(onClick = { viewModel.restart() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Neustart")
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

            // Copyright
            Text(
                text = "© 2025 By Rainer",
                modifier = Modifier.padding(bottom = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }

    // Dialogs
    if (showSaveDialog) {
        SaveGameDialog(
            moveCount = viewModel.getRemainingPegs(),
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
            }
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

                            // Prüfe ob dieser Peg gerade animiert wird (während Replay)
                            val isAnimating = replayState?.animatingPegRow == row &&
                                            replayState?.animatingPegCol == col

                            when {
                                !hole.hasPeg -> {
                                    // Leeres Loch (blauer Ring)
                                    drawCircle(
                                        color = Color.Blue,
                                        radius = radius,
                                        center = center,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                                isAnimating -> {
                                    // Animierender Peg während Replay (rot gefüllt)
                                    drawCircle(
                                        color = Color.Red,
                                        radius = radius,
                                        center = center
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
            }
        }
    }
}

