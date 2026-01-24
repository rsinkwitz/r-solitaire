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
    onImportYaml: (android.net.Uri) -> Unit,  // Geändert: Uri statt String
    onExportDb: () -> Unit,
    onImportDb: (android.net.Uri) -> Unit,  // Geändert: Uri statt String
    onDeleteAll: () -> Unit,
    availableYamlFiles: List<String>,
    availableDbFiles: List<String>
) {
    var gameToDelete by remember { mutableStateOf<SavedGame?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    // SAF File Picker für YAML Import mit OpenDocument für bessere Filterung
    val yamlPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        uri?.let { onImportYaml(it) }
    }

    // SAF File Picker für DB Import mit OpenDocument für bessere Filterung
    val dbPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        uri?.let { onImportDb(it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Gespeicherte Spiele")
                Text(
                    "Tipp: YAML für Daten, DB für komplette Sicherung",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
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
                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = onExportYaml,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("YAML Export", style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                "→ Download/*.yaml",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = {
                                    yamlPickerLauncher.launch(
                                        arrayOf(
                                            "text/plain",           // .txt, .yaml oft als plain text erkannt
                                            "text/yaml",            // offizieller YAML MIME-Type
                                            "text/x-yaml",          // alternative YAML MIME-Type
                                            "application/x-yaml",   // weitere alternative
                                            "application/yaml",     // weitere alternative
                                            "*/*"                   // Fallback für alle Dateien
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("YAML Import", style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                "← *.yaml Datei",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }

                    // DB Export/Import
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = onExportDb,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("DB Export", style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                "→ Download/*.db",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = {
                                    dbPickerLauncher.launch(
                                        arrayOf(
                                            "application/vnd.sqlite3",      // offizieller SQLite MIME-Type
                                            "application/x-sqlite3",        // alternative SQLite MIME-Type
                                            "application/octet-stream",     // binäre Dateien
                                            "application/x-db",             // .db Dateien
                                            "*/*"                           // Fallback für alle Dateien
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("DB Import", style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                "← *.db Datei",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
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

