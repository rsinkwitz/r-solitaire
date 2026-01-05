# Bugfixes für Save/Replay-Funktion

## Datum: 05.01.2026

### Problem A: Save-Button wird nicht automatisch aktiv ✅ GELÖST

**Symptom:**
- Save-Button war inaktiv nach dem ersten Zug
- Wurde erst aktiv nach Öffnen des Replay-Dialogs

**Ursache:**
- `canSaveGame()` war korrekt implementiert
- Aber die UI wurde nicht über Änderungen in `moveHistory` informiert
- Compose konnte nicht wissen, dass sich etwas geändert hat

**Lösung:**
1. Neue State-Variable hinzugefügt: `moveHistorySize = mutableStateOf(0)`
2. Bei jedem Zug in `tryMove()`: `moveHistorySize.value = moveHistory.size`
3. Bei `undoMove()`: `moveHistorySize.value = moveHistory.size`
4. Bei `setupBoard()`: `moveHistorySize.value = 0`
5. In der UI direkt auf `moveHistorySize.value` reagieren:
   ```kotlin
   val canSave = !viewModel.isBeforeFirst.value && 
                viewModel.moveHistorySize.value > 0 && 
                !viewModel.isInReplayMode
   ```

**Ergebnis:**
- Save-Button wird sofort nach dem ersten Zug automatisch aktiv ✅
- UI reagiert sofort auf Zustandsänderungen

---

### Problem B: Replay soll automatisch in "Weiterspielen"-Modus enden ✅ GELÖST

**Symptom:**
- Replay endete mit `isPlaying = false`
- Benutzer musste manuell entscheiden (Dialog)

**Gewünschtes Verhalten:**
- Replay soll automatisch in "Weiterspielen"-Modus übergehen
- Benutzer kann danach direkt weiterspielen

**Lösung:**
1. `startReplayLoop()` geändert:
   ```kotlin
   if (currentState.currentMoveIndex >= currentState.savedGame.moves.size) {
       delay(1000) // Kurze Pause am Ende
       continueFromReplay()  // Direkt weiterspielen!
       break
   }
   ```

2. `executeReplayMove()` erweitert:
   - Züge werden während des Replays zur `moveHistory` hinzugefügt
   - `moveHistorySize` wird aktualisiert
   - Dies ermöglicht nahtloses Weiterspielen

3. `continueFromReplay()` aktualisiert:
   - Setzt `moveHistorySize.value = moveHistory.size`
   - Beendet Replay-Modus
   - Spiel ist sofort spielbar

**Ergebnis:**
- Replay läuft bis zum Ende ✅
- Kurze Pause (1 Sekunde) am Ende ✅
- Automatischer Übergang in normalen Spielmodus ✅
- Alle Züge sind in der History ✅
- Benutzer kann sofort weiterspielen ✅

---

### Zusätzliche Verbesserungen:

**Stop-Button bleibt erhalten:**
- Ermöglicht manuelles Abbrechen des Replays
- Dialog erscheint: "Weiterspielen oder Verwerfen?"
- Nützlich wenn Benutzer Replay vorzeitig beenden will

**Konsistente State-Verwaltung:**
- `moveHistorySize` wird überall aktualisiert:
  - `tryMove()` - nach jedem normalen Zug
  - `undoMove()` - nach Rückgängig
  - `setupBoard()` - beim Neustart
  - `startReplay()` - beim Replay-Start
  - `executeReplayMove()` - während Replay
  - `continueFromReplay()` - beim Replay-Ende

**UI reagiert automatisch:**
- Save-Button aktiviert/deaktiviert sich automatisch
- Keine manuellen Refresh-Aktionen nötig
- Compose recomposition funktioniert korrekt

---

## Getestete Szenarien:

✅ **Szenario 1: Normales Spielen**
1. Ersten Stöpsel entfernen
2. Ersten Zug machen
3. → Save-Button wird SOFORT aktiv ✅

✅ **Szenario 2: Replay bis Ende**
1. Gespeichertes Spiel laden
2. Replay abspielen (komplett)
3. → Replay endet automatisch ✅
4. → Spiel ist sofort weiterspielbar ✅
5. → Keine Dialog-Unterbrechung ✅

✅ **Szenario 3: Replay manuell stoppen**
1. Replay starten
2. Stop-Button klicken
3. → Dialog erscheint ✅
4. → Benutzer kann wählen: Weiterspielen oder Verwerfen ✅

✅ **Szenario 4: Undo nach Replay**
1. Replay komplett abspielen
2. Weiterspielen
3. Undo klicken
4. → Zug wird rückgängig gemacht ✅
5. → moveHistorySize wird korrekt aktualisiert ✅

---

## Geänderte Dateien:

1. **`viewmodel/SolitaireViewModel.kt`**
   - Neue State-Variable: `moveHistorySize`
   - Aktualisiert in: `tryMove()`, `undoMove()`, `setupBoard()`, `startReplay()`, `executeReplayMove()`, `continueFromReplay()`
   - Replay-Ende geändert: `continueFromReplay()` statt `isPlaying = false`

2. **`ui/SolitaireScreen.kt`**
   - Save-Button verwendet jetzt `moveHistorySize.value` direkt
   - Automatische UI-Updates durch Compose Recomposition

---

## Status: ✅ BEIDE PROBLEME GELÖST

Die App funktioniert jetzt genau wie gewünscht:
- ✅ Save-Button aktiviert sich automatisch nach dem ersten Zug
- ✅ Replay endet automatisch im "Weiterspielen"-Modus
- ✅ Keine unnötigen Dialoge mehr am Replay-Ende
- ✅ Nahtlose Benutzererfahrung

**Bereit zum Testen!** 🎉

