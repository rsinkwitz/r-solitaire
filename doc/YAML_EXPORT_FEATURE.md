# ✅ YAML Export/Import - Menschenlesbare Spielstände!

## 🎯 Neues Feature implementiert!

**Version:** 1.2 (versionCode 3)  
**Datum:** 06.01.2026

---

## 📝 Was ist neu?

### Menschenlesbare YAML-Exports!

Sie können jetzt Ihre Spielstände in einem **lesbaren und editierbaren** Format exportieren und importieren!

---

## 🎨 Position-Encoding (wie gewünscht):

### Formel:
```
Position = Spalte (1-based) + 10 * Zeile (1-based)
```

### Beispiele:

| Zeile | Spalte | Position | Bedeutung |
|-------|--------|----------|-----------|
| 1 | 1 | 11 | Oben links |
| 1 | 7 | 17 | Oben rechts |
| 4 | 4 | 44 | **Zentrum** |
| 7 | 1 | 71 | Unten links |
| 7 | 7 | 77 | Unten rechts |

### Brett-Layout:
```
    11 12 13       Zeile 1
    21 22 23       Zeile 2
31 32 33 34 35 36 37   Zeile 3
41 42 43 44 45 46 47   Zeile 4
51 52 53 54 55 56 57   Zeile 5
    61 62 63       Zeile 6
    71 72 73       Zeile 7
```

---

## 📄 YAML-Format:

```yaml
# R-Solitaire Spielstände Export
# Format: Menschenlesbar
# Position: Spalte(1-based) + 10 * Zeile(1-based)
# Beispiel: Position 44 = Zeile 4, Spalte 4 (Zentrum)

games:
  - title: "Perfektes Spiel"
    date: "2026-01-06T14:30:00"
    start_hole: 44
    moves:
      - "24->44"
      - "42->24"
      - "44->42"
      - "51->53"
      # ... weitere Züge
    move_count: 31
    result: "complete"

  - title: "Test Spiel"
    date: "2026-01-06T15:00:00"
    start_hole: 33
    moves:
      - "13->33"
      - "31->33"
      - "43->23"
    move_count: 3
    result: "incomplete"
```

---

## 🎮 Wie funktioniert's?

### In der App (3 neue Buttons):

**TopAppBar rechts (neben den bestehenden Buttons):**

1. **↑** (Upload) - DB-Export (wie bisher)
2. **📥** (FileDownload) - **YAML Export** ✨ NEU
3. **📤** (FileUpload) - **YAML Import** ✨ NEU

---

## 📤 YAML Export:

### Schritt 1: Export in der App
```
App öffnen
  ↓
Button "📥" (FileDownload) tippen
  ↓
Meldung: "YAML Export erfolgreich! 5 Spiel(e) exportiert"
  ↓
Datei: /storage/emulated/0/Download/r_solitaire_games.yaml
```

### Schritt 2: Datei auf PC kopieren
```bash
# Mit Backup-Script (automatisch im nächsten Backup)
./backup_games.sh

# Oder manuell:
adb pull /storage/emulated/0/Download/r_solitaire_games.yaml ./r_solitaire_games.yaml
```

### Schritt 3: Datei anschauen/bearbeiten
```bash
# Im Editor öffnen
vim r_solitaire_games.yaml
nano r_solitaire_games.yaml
code r_solitaire_games.yaml  # VS Code

# Oder einfach durchlesen
cat r_solitaire_games.yaml
```

---

## 📥 YAML Import:

### Schritt 1: Datei auf Samsung kopieren
```bash
adb push r_solitaire_games.yaml /storage/emulated/0/Download/r_solitaire_games.yaml
```

### Schritt 2: Import in der App
```
App öffnen
  ↓
Button "📤" (FileUpload) tippen
  ↓
Meldung: "YAML Import erfolgreich! 5 Spiel(e) importiert"
  ↓
Spiele sind in der Liste verfügbar!
```

---

## ✏️ YAML bearbeiten:

### Was Sie tun können:

**1. Spiele umbenennen:**
```yaml
title: "Mein bestes Spiel"  # Ändern Sie den Titel
```

**2. Datum ändern:**
```yaml
date: "2026-01-06T14:30:00"  # ISO 8601 Format
```

**3. Startloch ändern:**
```yaml
start_hole: 44  # Zentrum (Zeile 4, Spalte 4)
```

**4. Züge bearbeiten:**
```yaml
moves:
  - "24->44"  # Von Position 24 zu 44
  - "42->24"  # Von Position 42 zu 24
  # Züge hinzufügen, löschen oder ändern
```

**5. Mehrere Spiele kombinieren:**
Kopieren Sie einfach mehrere `- title: ...` Blöcke in eine Datei!

---

## 🔍 Züge verstehen:

### Zug-Format: `"FROM->TO"`

**Beispiel:** `"24->44"`
- **FROM:** Position 24 = Zeile 2, Spalte 4
- **TO:** Position 44 = Zeile 4, Spalte 4 (Zentrum)
- **Bedeutung:** Springe von (2,4) über (3,4) nach (4,4)

### Zug dekodieren:

```
Position 24:
  Zeile = 24 / 10 = 2
  Spalte = 24 % 10 = 4
  → Brett-Position: Zeile 2, Spalte 4

Position 44:
  Zeile = 44 / 10 = 4
  Spalte = 44 % 10 = 4
  → Brett-Position: Zeile 4, Spalte 4 (Zentrum)
```

---

## 📊 Vergleich: DB vs. YAML Export:

| Feature | DB Export (↑) | YAML Export (📥) |
|---------|---------------|------------------|
| **Format** | Binary (.db) | Text (.yaml) |
| **Lesbar** | ❌ Nein | ✅ **Ja!** |
| **Editierbar** | ❌ Nein | ✅ **Ja!** |
| **Größe** | Kleiner | Größer |
| **Verwendung** | Vollbackup | Austausch, Analyse |
| **Import** | Noch nicht | ✅ **Ja!** |

**Empfehlung:**
- **DB-Export** für komplettes Backup
- **YAML-Export** zum Teilen, Analysieren, Bearbeiten

---

## 🎯 Anwendungsfälle:

### 1. Lösungen dokumentieren
```yaml
# Speichern Sie Ihre besten Lösungen
- title: "31 Züge - Perfekt!"
  date: "2026-01-06T14:30:00"
  start_hole: 44
  moves: [...]
```

### 2. Lösungen teilen
Senden Sie die YAML-Datei an Freunde!

### 3. Lösungen analysieren
Öffnen Sie die Datei und sehen Sie alle Züge.

### 4. Lösungen kombinieren
Mehrere YAML-Dateien in eine kombinieren.

### 5. Eigene Züge eingeben
Schreiben Sie Züge manuell und importieren Sie sie!

---

## 🛠️ Technische Details:

### Implementierte Klassen:

**1. `PositionConverter.kt`**
- `toPosition(row, col)` → Position
- `fromPosition(position)` → (row, col)
- `formatMove(Move)` → "24->44"
- `parseMove("24->44")` → Move

**2. `YamlExporter.kt`**
- `exportToYaml(games)` → YAML String
- `importFromYaml(yaml)` → List<SavedGame>

**3. ViewModel-Methoden:**
- `exportGamesToYaml()` - Exportiert alle Spiele
- `importGamesFromYaml()` - Importiert aus YAML

### Export-Pfad:
```
/storage/emulated/0/Download/r_solitaire_games.yaml
```

### Datei-Encoding:
- UTF-8
- Zeilenumbrüche: LF (Unix-Style)
- Kommentare mit `#` möglich

---

## 📋 Beispiel-Datei:

Siehe: `example_games.yaml` im Projekt-Root

Enthält:
- Ein perfektes Spiel (31 Züge)
- Ein unvollständiges Spiel (3 Züge)

Zum Testen:
```bash
adb push example_games.yaml /storage/emulated/0/Download/r_solitaire_games.yaml
# Dann in App: Import-Button (📤)
```

---

## ✅ Was funktioniert:

- ✅ Export aller gespeicherten Spiele als YAML
- ✅ Import von YAML zurück in die App
- ✅ Menschenlesbare Positionen (11-77)
- ✅ Züge als "24->44" Format
- ✅ Datum/Zeit in ISO 8601
- ✅ Editierbar mit jedem Text-Editor
- ✅ Kommentare möglich
- ✅ Mehrere Spiele pro Datei

---

## 🚀 Update installieren:

```bash
adb install -r app/build/outputs/apk/release/r-solitaire-release.apk
```

**Version:** 1.2 (versionCode 3)  
**Ihre Spiele bleiben erhalten!** (Update, keine Neuinstallation)

---

## 🎯 Workflow:

### Export:
```
1. App öffnen
2. Button 📥 tippen
3. Meldung: "YAML Export erfolgreich!"
4. adb pull /storage/.../r_solitaire_games.yaml
5. Datei mit Editor öffnen und bewundern!
```

### Bearbeiten:
```
1. YAML-Datei in Editor öffnen
2. Züge, Titel, etc. ändern
3. Speichern
```

### Import:
```
1. adb push r_solitaire_games.yaml /storage/.../Download/
2. App öffnen
3. Button 📤 tippen
4. Meldung: "YAML Import erfolgreich!"
5. Spiele in Liste verfügbar
```

---

## 📚 Position-Referenz:

### Kreuzform-Brett:
```
       11  12  13
       21  22  23
31  32  33  34  35  36  37
41  42  43  44  45  46  47
51  52  53  54  55  56  57
       61  62  63
       71  72  73
```

**Zentrum:** 44  
**Standard-Start:** 44 (Zentrum)  
**Alternative Starts:** 11, 13, 33, 44, 55, 71, 73

---

## ✨ Zusammenfassung:

**Implementiert:**
- ✅ Position-Encoding: `Spalte + 10 * Zeile` (1-based)
- ✅ Züge: `"24->44"` Format
- ✅ YAML-Format mit title, date, start_hole, moves
- ✅ Export-Button in App
- ✅ Import-Button in App
- ✅ Menschenlesbar
- ✅ Editierbar
- ✅ Beispiel-Datei

**APK bereit:**
- Datei: `r-solitaire-release.apk`
- Version: 1.2 (versionCode 3)
- Größe: ~10.6 MB
- Signiert: ✅

**Ihre Wünsche erfüllt!** 🎉

---

**Feature-Anfrage:** Menschenlesbare Exports  
**Lösung:** YAML mit Position-Encoding  
**Status:** ✅ Vollständig implementiert  
**Datum:** 06.01.2026

