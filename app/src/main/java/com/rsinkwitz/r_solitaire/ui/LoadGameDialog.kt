package com.rsinkwitz.r_solitaire.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rsinkwitz.r_solitaire.model.SavedGame
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LoadGameDialog(
    savedGames: List<SavedGame>,
    onDismiss: () -> Unit,
    onReplay: (SavedGame) -> Unit,
    onDelete: (SavedGame) -> Unit
) {
    var gameToDelete by remember { mutableStateOf<SavedGame?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gespeicherte Spiele") },
        text = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
            ) {
                if (savedGames.isEmpty()) {
                    Text(
                        text = "Keine gespeicherten Spiele vorhanden",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedGames) { game ->
                            SavedGameItem(
                                game = game,
                                onReplay = { onReplay(game) },
                                onDelete = { gameToDelete = game }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )

    // Delete confirmation dialog
    gameToDelete?.let { game ->
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            title = { Text("Löschen bestätigen") },
            text = { Text("Spiel '${game.title}' wirklich löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(game)
                        gameToDelete = null
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { gameToDelete = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun SavedGameItem(
    game: SavedGame,
    onReplay: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN) }
    val formattedDate = remember(game.timestamp) { dateFormat.format(Date(game.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Züge: ${game.moves.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onReplay) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Replay",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Löschen",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

