package com.rsinkwitz.r_solitaire.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    onDelete: (SavedGame) -> Unit,
    onExportYaml: () -> Unit,
    onImportYaml: (String) -> Unit,
    onExportDb: () -> Unit,
    onImportDb: (String) -> Unit,
    onDeleteAll: () -> Unit,
    availableYamlFiles: List<String>,
    availableDbFiles: List<String>
) {
    var gameToDelete by remember { mutableStateOf<SavedGame?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var showYamlFilePicker by remember { mutableStateOf(false) }
    var showDbFilePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gespeicherte Spiele") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Export/Import Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // YAML Export/Import
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onExportYaml,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("YAML Export", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = { showYamlFilePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("YAML Import", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // DB Export/Import
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onExportDb,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("DB Export", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = { showDbFilePicker = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("DB Import", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Alle löschen
                    Button(
                        onClick = { showDeleteAllConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = savedGames.isNotEmpty()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Alle löschen")
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // Spieleliste
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
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
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )

    // Delete single game confirmation
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

    // Delete all confirmation
    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = { Text("Alle Spiele löschen") },
            text = { Text("Wirklich ALLE ${savedGames.size} Spiel(e) löschen?\n\nDiese Aktion kann nicht rückgängig gemacht werden!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAll()
                        showDeleteAllConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("ALLE LÖSCHEN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // YAML File Picker
    if (showYamlFilePicker) {
        AlertDialog(
            onDismissRequest = { showYamlFilePicker = false },
            title = { Text("YAML Import - Datei auswählen") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (availableYamlFiles.isEmpty()) {
                        Text("Keine YAML-Dateien im Download-Ordner gefunden.")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Erwartet: r_solitaire_games_*.yaml",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text("${availableYamlFiles.size} Datei(en) gefunden:")
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(availableYamlFiles) { filename ->
                                Button(
                                    onClick = {
                                        onImportYaml(filename)
                                        showYamlFilePicker = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = filename,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showYamlFilePicker = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // DB File Picker
    if (showDbFilePicker) {
        AlertDialog(
            onDismissRequest = { showDbFilePicker = false },
            title = { Text("DB Import - Datei auswählen") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "⚠️ WARNUNG: DB Import ERSETZT alle aktuellen Spiele!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))

                    if (availableDbFiles.isEmpty()) {
                        Text("Keine DB-Dateien im Download-Ordner gefunden.")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Erwartet: r_solitaire_backup_*.db",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text("${availableDbFiles.size} Datei(en) gefunden:")
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(availableDbFiles) { filename ->
                                Button(
                                    onClick = {
                                        onImportDb(filename)
                                        showDbFilePicker = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text(
                                        text = filename,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDbFilePicker = false }) {
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

