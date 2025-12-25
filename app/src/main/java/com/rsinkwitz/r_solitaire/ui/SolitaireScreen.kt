package com.rsinkwitz.r_solitaire.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rsinkwitz.r_solitaire.viewmodel.SolitaireViewModel
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolitaireScreen(
    viewModel: SolitaireViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solitaire") },
                actions = {
                    IconButton(onClick = { viewModel.undoMove() }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Zurück")
                    }
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Neustart")
                    }
                }
            )
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

            // Info-Text
            if (viewModel.isBeforeFirst.value) {
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
}

@Composable
fun SolitaireBoard(
    viewModel: SolitaireViewModel,
    modifier: Modifier = Modifier
) {
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

