# ✅ Version 1.3 - Überarbeitetes YAML-System!

## 🎯 Alle Ihre Anforderungen umgesetzt!

**Version:** 1.3 (versionCode 4)  
**Datum:** 06.01.2026

---

## 📊 Was ist neu?

### 1. ✅ Übereinanderliegende Spalten

**Neues Position-Encoding:**
- Zeilen 1,2,6,7: Spalten beginnen bei **3** (Positionen 13-15, 23-25, 63-65, 73-75)
- Zeilen 3,4,5: Spalten 1-7 (Positionen 31-37, 41-47, 51-57)

### Brett-Layout mit Positionen:
```
       13  14  15      Zeile 1
       23  24  25      Zeile 2
31  32  33  34  35  36  37   Zeile 3
41  42  43  44  45  46  47   Zeile 4  
51  52  53  54  55  56  57   Zeile 5
       63  64  65      Zeile 6
       73  74  75      Zeile 7
```

**Vorteil:** Spalten mit gleicher Nummer stehen **übereinander**!
- Spalte 3: 13, 23, 33, 43, 53, 63, 73
- Spalte 4: 14, 24, 34, 44, 54, 64, 74 (Zentrum)
- Spalte 5: 15, 25, 35, 45, 55, 65, 75

---

### 2. ✅ Export/Import-Buttons im Load-Dialog

**Alle Buttons sind jetzt im "Gespeicherte Spiele"-Dialog:**

1. **YAML Export** - Exportiert als menschenlesbare YAML
2. **YAML Import** - Importiert YAML (mit Dialog)
3. **DB Export** - Exportiert als Binär-DB
4. **DB Import** - Importiert DB (mit Warnung!)
5. **Alle löschen** - Löscht alle Spiele (mit Bestätigung)

**TopAppBar:** Nur noch Save, Load, Undo, Restart

---

### 3. ✅ YAML-Import hängt an

**Vorher:** Import hätte alle Spiele ersetzt  
**Jetzt:** Import **fügt Spiele hinzu**

**Bei Titel-Konflikten:**
- Original: "Mein Spiel"
- Konflikt 1: "Mein Spiel-01"
- Konflikt 2: "Mein Spiel-02"
- etc.

---

### 4. ✅ Dateinamen mit Datum/Zeit

**Export-Dateien haben jetzt Zeitstempel:**

**YAML:**
```
r_solitaire_games_20260106_183045.yaml
r_solitaire_games_20260106_190512.yaml
```

**DB:**
```
r_solitaire_backup_20260106_183100.db
r_solitaire_backup_20260106_191530.db
```

**Format:** `YYYYMMDD_HHMMSS`

**Import:** Sucht automatisch die **neueste** Datei!

---

## 🎮 Neuer Workflow:

### Export (YAML):
```
1. App öffnen
2. Listen-Button (📋) tippen
3. "YAML Export" Button
4. Meldung: "r_solitaire_games_20260106_183045.yaml"
```

### Export (DB):
```
1. Listen-Button (📋) tippen
2. "DB Export" Button  
3. Meldung: "r_solitaire_backup_20260106_183100.db"
```

### Import (YAML):
```
1. Listen-Button (📋) tippen
2. "YAML Import" Button
3. Dialog: "Spiele werden angehängt"
4. Bestätigen
5. Import erfolgt, Spiele sind in Liste
```

### Import (DB):
```
1. Listen-Button (📋) tippen
2. "DB Import" Button
3. ⚠️ WARNUNG: "Dies ERSETZT alle Spiele!"
4. Bestätigen nur wenn sicher!
5. DB wird ersetzt, App lädt neu
```

### Alle löschen:
```
1. Listen-Button (📋) tippen
2. "Alle löschen" Button (rot)
3. Dialog: "ALLE 5 Spiele löschen?"
4. Bestätigen
5. Alle Spiele gelöscht
```

---

## 📄 YAML-Format (aktualisiert):

```yaml
# R-Solitaire Spielstände Export
# Format: Menschenlesbar
# Position: Übereinanderliegende Spalten
# 
# Brett-Layout mit Positionen:
#        13  14  15      Zeile 1
#        23  24  25      Zeile 2
# 31  32  33  34  35  36  37   Zeile 3
# 41  42  43  44  45  46  47   Zeile 4  
# 51  52  53  54  55  56  57   Zeile 5
#        63  64  65      Zeile 6
#        73  74  75      Zeile 7

games:
  - title: "Perfektes Spiel vom Zentrum"
    date: "2026-01-06T18:30:00"
    start_hole: 44
    moves:
      - "24->44"
      - "42->24"
      # ... weitere Züge
    move_count: 31
    result: "complete"
```

---

## 🎨 Position-Beispiele:

| Position | Zeile | Spalte | Bedeutung |
|----------|-------|--------|-----------|
| 13 | 1 | 3 | Oben links |
| 14 | 1 | 4 | Oben Mitte |
| 15 | 1 | 5 | Oben rechts |
| 24 | 2 | 4 | Zweite Zeile, Mitte |
| 33 | 3 | 3 | Linker Arm, Mitte |
| **44** | 4 | 4 | **Zentrum** |
| 55 | 5 | 5 | Rechter Arm, Mitte |
| 64 | 6 | 4 | Sechste Zeile, Mitte |
| 73 | 7 | 3 | Unten links |

---

## 🔄 Vergleich Alt vs. Neu:

### Position-Encoding:

| Zeile/Spalte | Alt (v1.2) | Neu (v1.3) |
|--------------|------------|------------|
| Zeile 1, Spalte 1 | 11 | 13 ✅ |
| Zeile 1, Spalte 2 | 12 | 14 ✅ |
| Zeile 1, Spalte 3 | 13 | 15 ✅ |
| Zeile 4, Spalte 4 | 44 | 44 (gleich) |
| Zeile 7, Spalte 1 | 71 | 73 ✅ |

**Vorteil:** Spalte 4 ist überall 4 (14, 24, 34, 44, 54, 64, 74)

### UI-Änderungen:

| Feature | v1.2 | v1.3 |
|---------|------|------|
| Export-Buttons | TopAppBar (3 Buttons) | Load-Dialog (5 Buttons) ✅ |
| YAML-Import | Ersetzt | Hängt an ✅ |
| Titel-Konflikte | Keine Behandlung | "-01", "-02" ✅ |
| Dateinamen | Fest | Mit Zeitstempel ✅ |
| Alle löschen | - | Mit Dialog ✅ |

---

## 📁 Datei-Beispiele:

### Export (mehrfach):
```
Download/
├── r_solitaire_games_20260106_183045.yaml  (Export 1)
├── r_solitaire_games_20260106_190512.yaml  (Export 2)
├── r_solitaire_games_20260106_193025.yaml  (Export 3)
├── r_solitaire_backup_20260106_183100.db   (DB Export 1)
└── r_solitaire_backup_20260106_191530.db   (DB Export 2)
```

### Import:
- **YAML Import:** Sucht automatisch neueste `r_solitaire_games_*.yaml`
- **DB Import:** Sucht automatisch neueste `r_solitaire_backup_*.db`

**Oder:** Spezifischen Dateinamen manuell umbenennen:
```
r_solitaire_games.yaml  → App importiert diese
r_solitaire_backup.db   → App importiert diese
```

---

## 🔍 Load-Dialog Layout:

```
┌─────────────────────────────────────┐
│  Gespeicherte Spiele                │
├─────────────────────────────────────┤
│  [YAML Export] [YAML Import]        │  ← Zeile 1
│  [DB Export]   [DB Import]          │  ← Zeile 2
│  [Alle löschen]                     │  ← Zeile 3 (rot)
├─────────────────────────────────────┤
│  📋 Perfektes Spiel      [▶] [🗑]   │  ← Spiel 1
│  📋 Test Spiel           [▶] [🗑]   │  ← Spiel 2
│  ...                                 │
├─────────────────────────────────────┤
│                    [Schließen]      │
└─────────────────────────────────────┘
```

---

## ⚠️ Wichtige Hinweise:

### YAML Import:
- ✅ **Fügt Spiele hinzu** (ersetzt nicht!)
- ✅ Bei Titel-Konflikt: Automatisches Suffix "-01", "-02"
- ✅ Dialog zeigt: "Spiele werden angehängt"

### DB Import:
- ⚠️ **ERSETZT alle aktuellen Spiele!**
- ⚠️ Dialog zeigt deutliche Warnung
- ⚠️ Nur verwenden wenn Backup vorhanden!

### Alle löschen:
- ⚠️ **Löscht ALLE Spiele unwiderruflich!**
- ⚠️ Dialog zeigt Anzahl der Spiele
- ⚠️ Erst Backup erstellen!

---

## 🔧 Technische Änderungen:

### YamlExporter.kt:
```kotlin
// Neues Position-Encoding
fun toPosition(row: Int, col: Int): Int {
    val rowOneBased = row + 1
    return when (rowOneBased) {
        1, 2, 6, 7 -> {
            val colOneBased = col + 3  // Spalten beginnen bei 3!
            colOneBased + 10 * rowOneBased
        }
        else -> {
            val colOneBased = col + 1
            colOneBased + 10 * rowOneBased
        }
    }
}
```

### SolitaireViewModel.kt:
```kotlin
// Export mit Zeitstempel
val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
val exportFile = File(downloadsDir, "r_solitaire_games_${timestamp}.yaml")

// Import mit Anhängen und Konflikt-Behandlung
var uniqueTitle = game.title
var suffix = 1
while (existingTitles.contains(uniqueTitle)) {
    uniqueTitle = "${game.title}-${String.format("%02d", suffix)}"
    suffix++
}
```

### LoadGameDialog.kt:
```kotlin
// 5 neue Callback-Parameter
fun LoadGameDialog(
    // ...existing...
    onExportYaml: () -> Unit,
    onImportYaml: () -> Unit,
    onExportDb: () -> Unit,
    onImportDb: () -> Unit,
    onDeleteAll: () -> Unit
)
```

---

## 📚 Migration v1.2 → v1.3:

### Alte YAML-Dateien:
**Problem:** Alte Dateien haben altes Position-Encoding (11 statt 13)

**Lösung:**
1. **Option A:** Neu exportieren (empfohlen)
2. **Option B:** Manuell anpassen:
   - Zeile 1: 11→13, 12→14, 13→15
   - Zeile 2: 21→23, 22→24, 23→25
   - Zeile 6: 61→63, 62→64, 63→65
   - Zeile 7: 71→73, 72→74, 73→75
   - Zeilen 3,4,5: Unverändert

---

## ✅ Zusammenfassung:

### Implementiert:
1. ✅ **Übereinanderliegende Spalten** - Zeilen 1,2,6,7 beginnen mit Spalte 3
2. ✅ **Buttons im Load-Dialog** - Alle 5 Buttons verschoben
3. ✅ **YAML-Import anhängen** - Keine Überschreibung mehr
4. ✅ **Titel-Konflikt-Behandlung** - Automatisches "-01", "-02" Suffix
5. ✅ **Dateinamen mit Zeitstempel** - Für YAML und DB
6. ✅ **"Alle löschen" Funktion** - Mit Bestätigungsdialog
7. ✅ **Import mit Dialogen** - YAML, DB beide mit Bestätigung

### APK:
- **Datei:** `r-solitaire-release.apk`
- **Version:** 1.3 (versionCode 4)
- **Erstellt:** Gerade eben
- **Größe:** ~10.6 MB
- **Signiert:** ✅

---

## 🚀 Update installieren:

```bash
adb install -r app/build/outputs/apk/release/r-solitaire-release.apk
```

**Ihre Spiele bleiben erhalten!** (Update, nicht Neuinstallation)

---

## 🎯 Test-Workflow:

```
1. Update installieren
2. App öffnen
3. Listen-Button (📋) tippen
4. Alle 5 Buttons sehen
5. "YAML Export" testen → Datei mit Zeitstempel
6. "YAML Import" testen → Spiele werden angehängt
7. Position-Encoding prüfen (Spalten übereinander)
```

---

**Alle Ihre Anforderungen sind umgesetzt!** 🎉

- ✅ Spalten übereinander (13-15, 23-25, 63-65, 73-75)
- ✅ Buttons im Load-Dialog
- ✅ 5 Buttons: Export-YAML, Import-YAML, Export-DB, Import-DB, Alle löschen
- ✅ YAML-Import anhängen mit Konflikt-Behandlung
- ✅ Dateinamen mit Datum/Zeit
- ✅ Alle Dialoge mit Bestätigung

**APK v1.3 ist bereit zum Testen!** 🚀

