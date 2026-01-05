package com.rsinkwitz.r_solitaire.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SaveGameDialog(
    moveCount: Int,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val currentDateTime = remember {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN)
            .format(Date())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Spiel speichern") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 50) title = it },
                    label = { Text("Titel") },
                    placeholder = { Text("z.B. Perfektes Spiel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Datum: $currentDateTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Züge: $moveCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.trim().isNotEmpty()) {
                        onSave(title)
                        onDismiss()
                    }
                },
                enabled = title.trim().length >= 3
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

