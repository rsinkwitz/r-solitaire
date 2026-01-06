# ✅ Version 1.8 - Korrektur Save-Dialog Text

## 🐛 Problem:

**Fehlerhafte Beschriftung im Save-Dialog:**
- Stand: "Züge: 31"
- Aber: Die Zahl war die **Anzahl der verbleibenden Stöpsel**, nicht die Anzahl der Züge!

---

## ✅ Lösung:

### Geändert in SaveGameDialog.kt:

**Vorher:**
```kotlin
Text(text = "Züge: $moveCount")
```

**Jetzt:**
```kotlin
Text(text = "Verbleibende Stöpsel: $remainingPegs")
```

### Auch Parameter umbenannt:

**Vorher:**
```kotlin
fun SaveGameDialog(
    moveCount: Int,  // Irreführender Name!
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
)
```

**Jetzt:**
```kotlin
fun SaveGameDialog(
    remainingPegs: Int,  // Korrekt!
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
)
```

### Aufruf angepasst:

**Vorher:**
```kotlin
SaveGameDialog(
    moveCount = viewModel.getRemainingPegs(),  // Verwirrend
    // ...
)
```

**Jetzt:**
```kotlin
SaveGameDialog(
    remainingPegs = viewModel.getRemainingPegs(),  // Klar!
    // ...
)
```

---

## 📊 Save-Dialog jetzt:

```
┌─────────────────────────────────────┐
│  Spiel speichern                    │
├─────────────────────────────────────┤
│  ┌───────────────────────────────┐  │
│  │ Titel: Perfektes Spiel        │  │
│  └───────────────────────────────┘  │
│                                     │
│  Datum: 06.01.2026 15:30           │
│  Verbleibende Stöpsel: 1           │  ← KORRIGIERT!
├─────────────────────────────────────┤
│              [Abbrechen] [Speichern]│
└─────────────────────────────────────┘
```

**Vorher:** "Züge: 1" (falsch!)  
**Jetzt:** "Verbleibende Stöpsel: 1" (korrekt!)

---

## 🎯 Warum war das wichtig?

### Das Problem:

- **Züge** = Anzahl der Spielzüge (moveHistory.size)
- **Verbleibende Stöpsel** = Anzahl der Pegs auf dem Brett (getRemainingPegs())

**Der Dialog zeigte die falsche Information!**

Beispiel:
- Nach 30 Zügen: 2 Stöpsel übrig
- Dialog zeigte: "Züge: 2" ← Falsch! Es waren 30 Züge, nicht 2!
- Sollte zeigen: "Verbleibende Stöpsel: 2" ← Korrekt!

---

## ✅ Jetzt korrekt:

### Der Dialog zeigt:
- ✅ **Titel** - Vom Benutzer eingegeben
- ✅ **Datum** - Aktuelles Datum/Zeit
- ✅ **Verbleibende Stöpsel** - Wie viele Pegs noch auf dem Brett sind

### Hintergrund Info (nicht im Dialog):
- Anzahl der Züge: Wird in `SavedGame.moves.size` gespeichert
- Wird in der Load-Liste angezeigt: "Züge: 31"

---

## 📦 Version 1.8:

**Version:** 1.8 (versionCode 9)  
**APK:** `r-solitaire-release.apk`  
**Build:** Erfolgreich  
**Größe:** ~10.6 MB

```bash
adb install -r app/build/outputs/apk/release/r-solitaire-release.apk
```

---

## 🔍 Code-Änderungen:

### Dateien geändert:

1. **SaveGameDialog.kt**
   - Parameter `moveCount` → `remainingPegs`
   - Text "Züge:" → "Verbleibende Stöpsel:"

2. **SolitaireScreen.kt**
   - Aufruf Parameter `moveCount` → `remainingPegs`

3. **build.gradle.kts**
   - versionCode 8 → 9
   - versionName "1.7" → "1.8"

---

## ✅ Status:

**Problem:** Falscher Text im Save-Dialog  
**Ursache:** Irreführender Parameter-Name und falscher Text  
**Lösung:** Parameter umbenannt, Text korrigiert  
**Ergebnis:** Dialog zeigt jetzt korrekte Information

---

**Datum:** 06.01.2026  
**Version:** 1.8 (versionCode 9)  
**Fix:** Save-Dialog Text korrigiert  
**Status:** ✅ RESOLVED

**Der Save-Dialog zeigt jetzt die richtige Information!** 🎉

