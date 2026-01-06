package com.rsinkwitz.r_solitaire.util

import com.rsinkwitz.r_solitaire.model.SavedGame
import com.rsinkwitz.r_solitaire.model.Move
import java.text.SimpleDateFormat
import java.util.*

/**
 * Konvertiert zwischen Zeile/Spalte (0-based) und menschenlesbarer Position (1-based)
 *
 * Neue Logik: Spalten sind übereinander ausgerichtet
 * Zeilen 1,2,6,7: Spalten 3-5 (Position 13-15, 23-25, 63-65, 73-75)
 * Zeilen 3,4,5: Spalten 1-7 (Position 31-37, 41-47, 51-57)
 *
 * Beispiel: Zeile 3, Spalte 3 (0-based) → Position 44 (Zentrum)
 */
object PositionConverter {

    /**
     * Konvertiert von 0-based row/col zu 1-based Position
     *
     * Zeilen 0,1,5,6 (1,2,6,7 in 1-based): Spalten 0-2 (1-3) → Positionen x3, x4, x5
     * Zeilen 2,3,4 (3,4,5 in 1-based): Spalten 0-6 (1-7) → Positionen x1-x7
     */
    fun toPosition(row: Int, col: Int): Int {
        val rowOneBased = row + 1

        return when (rowOneBased) {
            1, 2, 6, 7 -> {
                // Zeilen 1,2,6,7: Spalten beginnen bei 3
                val colOneBased = col + 3  // col 0 → 3, col 1 → 4, col 2 → 5
                colOneBased + 10 * rowOneBased
            }
            else -> {
                // Zeilen 3,4,5: Normale Nummerierung
                val colOneBased = col + 1
                colOneBased + 10 * rowOneBased
            }
        }
    }

    /**
     * Konvertiert von 1-based Position zu 0-based row/col
     */
    fun fromPosition(position: Int): Pair<Int, Int> {
        val rowOneBased = position / 10
        val colPart = position % 10

        val row = rowOneBased - 1

        val col = when (rowOneBased) {
            1, 2, 6, 7 -> {
                // Zeilen 1,2,6,7: Spalten beginnen bei 3
                colPart - 3  // 3 → col 0, 4 → col 1, 5 → col 2
            }
            else -> {
                // Zeilen 3,4,5: Normale Nummerierung
                colPart - 1
            }
        }

        return Pair(row, col)
    }

    /**
     * Formatiert einen Zug als String: "24->44"
     */
    fun formatMove(move: Move): String {
        val from = toPosition(move.fromRow, move.fromCol)
        val to = toPosition(move.toRow, move.toCol)
        return "$from->$to"
    }

    /**
     * Parst einen Zug aus String: "24->44" → Move
     */
    fun parseMove(moveString: String): Move? {
        val parts = moveString.split("->")
        if (parts.size != 2) return null

        val fromPos = parts[0].toIntOrNull() ?: return null
        val toPos = parts[1].toIntOrNull() ?: return null

        val (fromRow, fromCol) = fromPosition(fromPos)
        val (toRow, toCol) = fromPosition(toPos)

        return Move(fromRow, fromCol, toRow, toCol)
    }
}

/**
 * YAML-Export/Import für SavedGames
 */
object YamlExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    /**
     * Exportiert eine Liste von SavedGames als YAML
     */
    fun exportToYaml(games: List<SavedGame>): String {
        val yaml = StringBuilder()
        yaml.appendLine("# R-Solitaire Spielstände Export")
        yaml.appendLine("# Format: Menschenlesbar")
        yaml.appendLine("# Position: Spalte(1-based) + 10 * Zeile(1-based)")
        yaml.appendLine("# Beispiel: Position 44 = Zeile 4, Spalte 4 (Zentrum)")
        yaml.appendLine()
        yaml.appendLine("games:")

        for (game in games) {
            yaml.appendLine("  - title: \"${game.title.replace("\"", "\\\"")}\"")
            yaml.appendLine("    date: \"${dateFormat.format(Date(game.timestamp))}\"")
            yaml.appendLine("    start_hole: ${PositionConverter.toPosition(game.initialHoleRow, game.initialHoleCol)}")
            yaml.appendLine("    moves:")

            for (move in game.moves) {
                val moveStr = PositionConverter.formatMove(move)
                yaml.appendLine("      - \"$moveStr\"")
            }

            yaml.appendLine("    move_count: ${game.moves.size}")

            // Result (optional, könnte erweitert werden)
            val result = if (game.moves.size == 31) "complete" else "incomplete"
            yaml.appendLine("    result: \"$result\"")
            yaml.appendLine()
        }

        return yaml.toString()
    }

    /**
     * Importiert SavedGames aus YAML
     * Einfacher Parser - erwartet exaktes Format
     */
    fun importFromYaml(yaml: String): List<SavedGame> {
        val games = mutableListOf<SavedGame>()
        val lines = yaml.lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]  // Nicht trim() hier!
            val trimmedLine = line.trim()

            // Suche nach "  - title:" oder "- title:" (mit oder ohne führende Spaces)
            if (trimmedLine.startsWith("- title:") ||
                (line.trimStart().startsWith("- title:") && line.startsWith(" "))) {
                val game = parseGame(lines, i)
                if (game != null) {
                    games.add(game)
                }
            }

            i++
        }

        return games
    }

    private fun parseGame(lines: List<String>, startIndex: Int): SavedGame? {
        var title = ""
        var timestamp = System.currentTimeMillis()
        var startHole = 44
        val moves = mutableListOf<Move>()

        var i = startIndex
        while (i < lines.size) {
            val line = lines[i].trim()

            // Nächstes Spiel oder Ende (erkennt auch "  - title:")
            if (line.startsWith("- title:") && i != startIndex) break

            when {
                line.startsWith("- title:") || line.startsWith("title:") -> {
                    title = extractQuotedString(line) ?: ""
                }
                line.startsWith("date:") -> {
                    val dateStr = extractQuotedString(line)
                    if (dateStr != null) {
                        try {
                            timestamp = dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            // Behalte aktuellen Timestamp
                        }
                    }
                }
                line.startsWith("start_hole:") -> {
                    val posStr = line.substringAfter("start_hole:").trim()
                    startHole = posStr.toIntOrNull() ?: 44
                }
                line.startsWith("\"") && line.contains("->") -> {
                    // Move-Zeile: "24->44"
                    val moveStr = extractQuotedString(line)
                    if (moveStr != null) {
                        val move = PositionConverter.parseMove(moveStr)
                        if (move != null) {
                            moves.add(move)
                        }
                    }
                }
                line.startsWith("- \"") && line.contains("->") -> {
                    // Move-Zeile mit führendem "-": - "24->44"
                    val moveStr = extractQuotedString(line.substringAfter("-").trim())
                    if (moveStr != null) {
                        val move = PositionConverter.parseMove(moveStr)
                        if (move != null) {
                            moves.add(move)
                        }
                    }
                }
            }

            i++
        }

        if (title.isEmpty()) return null

        val (startRow, startCol) = PositionConverter.fromPosition(startHole)

        return SavedGame(
            id = UUID.randomUUID().toString(),
            title = title,
            timestamp = timestamp,
            initialHoleRow = startRow,
            initialHoleCol = startCol,
            moves = moves
        )
    }

    private fun extractQuotedString(line: String): String? {
        val firstQuote = line.indexOf('"')
        if (firstQuote == -1) return null

        val lastQuote = line.lastIndexOf('"')
        if (lastQuote == firstQuote) return null

        return line.substring(firstQuote + 1, lastQuote)
    }
}

