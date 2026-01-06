# 📱 R-Solitaire - Backup, Export & Import Guide

**Vollständige Anleitung für Datensicherung und Datenaustausch**

---

## 📋 Inhaltsverzeichnis

1. [Überblick](#überblick)
2. [Backup-Scripts](#backup-scripts)
3. [In-App Export/Import](#in-app-exportimport)
4. [YAML Export/Import](#yaml-exportimport)
5. [Plattform-Kompatibilität](#plattform-kompatibilität)
6. [Troubleshooting](#troubleshooting)

---

## 🎯 Überblick

### Drei Methoden der Datensicherung:

| Methode | Format | Verwendung | Menschenlesbar |
|---------|--------|------------|----------------|
| **DB Export (App)** | Binary (.db) | Vollbackup | ❌ Nein |
| **YAML Export (App)** | Text (.yaml) | Austausch, Analyse | ✅ Ja |
| **Backup-Scripts** | Binary (.db) | PC-Backup via ADB | ❌ Nein |

---

## 📦 Backup-Scripts

### Verfügbare Scripts:

- **`backup_games.sh`** - Sichert Spiele via ADB vom Samsung auf PC
- **`restore_games.sh`** - Stellt Spiele vom PC auf Samsung wieder her

### Plattform-Unterstützung:

✅ **MSYS2** (Windows mit Bash)  
✅ **WSL** (Windows Subsystem for Linux)  
✅ **Linux** (alle Distributionen)  
✅ **macOS**

❌ **PowerShell** (nicht mehr unterstützt - verwenden Sie MSYS2 oder WSL unter Windows)

---

### Backup erstellen (backup_games.sh)

#### Voraussetzungen:

1. **ADB installiert:**
   - MSYS2: `pacman -S android-tools`
   - Linux: `sudo apt install android-tools-adb`
   - macOS: `brew install android-platform-tools`
   - Windows: Android Studio SDK Platform-Tools

2. **Samsung verbunden:**
   - USB-Debugging aktiviert
   - USB-Kabel angeschlossen

#### Verwendung:

```bash
# Script ausführbar machen (einmalig)
chmod +x backup_games.sh

# Backup durchführen
./backup_games.sh
```

#### Ablauf:

1. Script erkennt automatisch Ihre Umgebung
2. Prüft ob ADB verfügbar ist
3. Prüft ob Samsung verbunden ist
4. Sie führen DB-Export in der App durch (Button "DB Export" im Load-Dialog)
5. Script kopiert Datei vom Samsung Download-Ordner auf PC
6. Backup wird gespeichert mit Zeitstempel

#### Backup-Pfade:

- **MSYS2:** `D:\backup\r_solitaire_saves_YYYYMMDD_HHMMSS\`
- **WSL:** `/mnt/d/backup/r_solitaire_saves_YYYYMMDD_HHMMSS/`
- **Linux/Mac:** `~/backup/r_solitaire_saves_YYYYMMDD_HHMMSS/`

#### Backup-Inhalt:

```
r_solitaire_saves_20260106_153000/
├── saved_games.db          # Datenbank mit allen Spielen
└── BACKUP_INFO.txt         # Backup-Informationen
```

---

### Backup wiederherstellen (restore_games.sh)

#### Verwendung:

```bash
# Script ausführbar machen (einmalig)
chmod +x restore_games.sh

# Backup wiederherstellen
./restore_games.sh /pfad/zum/backup/r_solitaire_saves_20260106_153000
```

#### Ablauf:

1. Script prüft ob Backup-Ordner existiert
2. Kopiert `saved_games.db` in Samsung Download-Ordner
3. Sie führen DB-Import in der App durch (Button "DB Import" im Load-Dialog)
4. App lädt Spiele aus Datenbank

#### ⚠️ Warnung:

DB-Import **überschreibt** alle aktuellen Spiele! Erstellen Sie vorher ein Backup.

---

## 📲 In-App Export/Import

### Zugriff über Load-Dialog:

1. App öffnen
2. Listen-Button (📋) oben rechts tippen
3. **5 Buttons** im Dialog:

#### Export-Buttons:

**YAML Export (📥)**
- Exportiert Spiele als menschenlesbare YAML-Datei
- Datei: `r_solitaire_games_YYYYMMDD_HHMMSS.yaml`
- Ort: Samsung Download-Ordner
- **Dialog bleibt offen**, Toast zeigt Erfolg

**DB Export (↓)**
- Exportiert komplette Datenbank als Binary
- Datei: `r_solitaire_backup_YYYYMMDD_HHMMSS.db`
- Ort: Samsung Download-Ordner
- **Dialog bleibt offen**, Toast zeigt Erfolg

#### Import-Buttons:

**YAML Import (📤)**
- Öffnet File-Picker mit verfügbaren YAML-Dateien
- **Fügt Spiele hinzu** (ersetzt nicht!)
- Bei Titel-Konflikt: Automatisches Suffix "-01", "-02"
- Liste aktualisiert sich automatisch

**DB Import (↑)**
- Öffnet File-Picker mit verfügbaren DB-Dateien
- **⚠️ ERSETZT alle aktuellen Spiele!**
- Warnung wird angezeigt
- Liste aktualisiert sich automatisch

#### Weitere Funktionen:

**Alle löschen (🗑️)**
- Löscht alle gespeicherten Spiele
- Bestätigungsdialog
- Liste aktualisiert sich automatisch

---

## 📄 YAML Export/Import

### YAML-Format

Menschenlesbar und editierbar!

#### Position-Encoding:

```
Position = Spalte (1-based) + 10 * Zeile (1-based)

Brett-Layout:
       13  14  15      Zeile 1
       23  24  25      Zeile 2
31  32  33  34  35  36  37   Zeile 3
41  42  43  44  45  46  47   Zeile 4 (44 = Zentrum)
51  52  53  54  55  56  57   Zeile 5
       63  64  65      Zeile 6
       73  74  75      Zeile 7
```

**Vorteil:** Spalten mit gleicher Nummer stehen übereinander!

#### Beispiel YAML-Datei:

```yaml
# R-Solitaire Spielstände Export
games:
  - title: "Perfektes Spiel vom Zentrum"
    date: "2026-01-06T15:30:00"
    start_hole: 44
    moves:
      - "24->44"
      - "42->24"
      - "44->42"
      # ... weitere Züge
    move_count: 31
    result: "complete"
    
  - title: "Test Spiel"
    date: "2026-01-06T16:00:00"
    start_hole: 33
    moves:
      - "13->33"
      - "31->33"
    move_count: 2
    result: "incomplete"
```

### YAML bearbeiten:

Sie können YAML-Dateien mit jedem Text-Editor öffnen und bearbeiten:

- **Titel ändern:** `title: "Neuer Name"`
- **Datum ändern:** `date: "2026-01-07T10:00:00"`
- **Züge hinzufügen/löschen:** In `moves:` Liste
- **Mehrere Dateien kombinieren:** Spiele in eine Datei kopieren

### YAML-Anwendungsfälle:

1. **Lösungen dokumentieren** - Lesbare Aufzeichnung Ihrer besten Spiele
2. **Lösungen teilen** - YAML-Datei an Freunde senden
3. **Lösungen analysieren** - Züge im Editor durchgehen
4. **Lösungen kombinieren** - Mehrere YAML-Dateien zusammenführen
5. **Eigene Züge eingeben** - Manuell Spiele erstellen

---

## 🌍 Plattform-Kompatibilität

### Pfad-Handling:

Die Scripts und die App handhaben Pfade automatisch korrekt:

#### MSYS2 (Windows):

**Problem:** `adb.exe` ist Windows-Binary, braucht Windows-Pfade

**Lösung:** Scripts verwenden `cygpath -w` für adb-Befehle:

```bash
backupDir="/d/backup/saves_123"              # Bash-Pfad
backupDirOs=$(cygpath -w "$backupDir")       # Windows-Pfad für adb.exe
adb pull ... "$backupDirOs/file.db"          # Funktioniert!
```

#### WSL:

Pfade wie `/mnt/d/backup/` funktionieren direkt.

#### Linux/macOS:

Standard-Pfade wie `~/backup/` funktionieren direkt.

### Download-Ordner auf Samsung:

Die App versucht automatisch **3 verschiedene Pfade**:

1. `Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)`
2. `Environment.getExternalStorageDirectory() + "/Download"`
3. `Context.getExternalFilesDir(DIRECTORY_DOWNLOADS)`

**Funktioniert auf allen Android-Versionen und Geräten!**

---

## 🔧 Troubleshooting

### Problem: "ADB not found"

**Lösung:**
```bash
# MSYS2:
pacman -S android-tools

# Linux (Debian/Ubuntu):
sudo apt install android-tools-adb

# macOS:
brew install android-platform-tools
```

Oder: Android Studio installieren, dann `platform-tools` zum PATH hinzufügen.

---

### Problem: "Kein Gerät gefunden"

**Checkliste:**

1. ✅ USB-Kabel angeschlossen?
2. ✅ USB-Debugging aktiviert?
   - Einstellungen → Über das Telefon
   - 7x auf Build-Nummer tippen
   - Entwickleroptionen → USB-Debugging AN
3. ✅ Gerät entsperrt?
4. ✅ USB-Debugging-Dialog auf Samsung bestätigt?

**Test:**
```bash
adb devices
```
Sollte Ihr Gerät listen.

---

### Problem: "Export nicht gefunden"

**Mögliche Ursachen:**

1. **Export in App nicht durchgeführt**
   - Lösung: Listen-Button → "DB Export" oder "YAML Export"

2. **Permission denied**
   - Samsung: Einstellungen → Apps → R-Solitaire → Berechtigungen → "Speicher" erlauben
   - App neu starten und Export wiederholen

3. **Falscher Pfad**
   - Export-Pfad im Toast notieren
   - Script ggf. manuell mit korrektem Pfad aufrufen

**Debug:**
```bash
# Suche nach allen .db Dateien im Download-Ordner:
adb shell "ls -la /storage/emulated/0/Download/*.db"

# Suche nach allen .yaml Dateien:
adb shell "ls -la /storage/emulated/0/Download/*.yaml"
```

---

### Problem: "YAML Import lädt nichts"

**Wurde behoben in v1.6!**

**Ursache war:** Parser erkannte Spiele mit führenden Spaces nicht korrekt.

**Lösung:** Update auf v1.6 oder neuer installieren.

**Test:**
```bash
# Prüfe ob YAML-Datei existiert:
adb shell "ls -la /storage/emulated/0/Download/r_solitaire_games*.yaml"

# Zeige Inhalt (erste 20 Zeilen):
adb shell "head -n 20 /storage/emulated/0/Download/r_solitaire_games*.yaml"
```

---

### Problem: "DB Import crashed"

**Wurde behoben in v1.6!**

**Ursache war:** Room's InvalidationTracker lief noch nach `db.close()`

**Lösung:** Update auf v1.6 oder neuer installieren.

**Aktuelle Methode:** Record-by-Record Import statt File-Replace (kein `db.close()` mehr).

---

### Problem: "Liste aktualisiert sich nicht nach Import"

**Wurde behoben in v1.6!**

**Lösung:** Update auf v1.6 oder neuer installieren.

**Wie es funktioniert:**
- DB-Import: Alte Spiele werden gelöscht, neue eingefügt (Room-aware)
- YAML-Import: Spiele werden eingefügt (Room-aware)
- Flow aktualisiert automatisch die UI

---

## 📊 Vergleich der Methoden

| Merkmal | Backup-Scripts | DB Export/Import | YAML Export/Import |
|---------|----------------|------------------|-------------------|
| **Benötigt ADB** | ✅ Ja | ❌ Nein | ❌ Nein |
| **PC-Verbindung** | ✅ Ja | ❌ Nein | ❌ Nein |
| **Menschenlesbar** | ❌ Nein | ❌ Nein | ✅ Ja |
| **Editierbar** | ❌ Nein | ❌ Nein | ✅ Ja |
| **Automatisch** | ✅ Ja | ✅ Ja | ✅ Ja |
| **Datei-Wahl** | ❌ Nein | ✅ Ja | ✅ Ja |
| **Import-Modus** | Ersetzt | Ersetzt | Anhängen |
| **Verwendung** | PC-Backup | Vollbackup | Austausch |

### Empfehlung:

- **Regelmäßiges Backup:** Backup-Scripts (automatisierbar)
- **Vollbackup auf Gerät:** DB Export (schnell, kompakt)
- **Spiele teilen/analysieren:** YAML Export (lesbar, editierbar)
- **Mehrere Geräte:** YAML Export/Import (fügt Spiele hinzu)

---

## 🎯 Best Practices

### 1. Regelmäßige Backups

```bash
# Cron-Job (Linux/macOS) - täglich um 2 Uhr:
0 2 * * * /pfad/zu/backup_games.sh

# Task Scheduler (Windows mit MSYS2):
# Aufgabe erstellen, die backup_games.sh über bash.exe ausführt
```

### 2. Mehrere Backup-Generationen

Die Scripts erstellen automatisch Zeitstempel:
```
/d/backup/
├── r_solitaire_saves_20260106_140000/
├── r_solitaire_saves_20260106_150000/
└── r_solitaire_saves_20260106_160000/
```

**Empfehlung:** Behalten Sie mindestens die letzten 7 Backups.

### 3. Vor großen Änderungen

**Immer zuerst Backup erstellen:**
- Vor App-Update
- Vor DB-Import
- Vor "Alle löschen"

### 4. YAML für Archivierung

Exportieren Sie Ihre besten Lösungen als YAML:
- Langfristig lesbar (auch ohne App)
- Versionskontrolle möglich (Git)
- Dokumentation möglich

---

## 📝 Changelog relevanter Versionen

### v1.7 - UI-Verbesserungen
- Export-Dialog bleibt offen
- Toast einzeilig und kompakt
- Start-Loch mit grünem Rand

### v1.6 - Kritische Fixes
- DB-Import ohne InvalidationTracker-Fehler
- YAML-Import erkennt alle Spiele
- Liste aktualisiert sich automatisch
- Alle DB-Operationen auf IO-Thread

### v1.3 - YAML Export/Import
- Menschenlesbare YAML-Dateien
- Position-Encoding mit übereinander liegenden Spalten
- File-Picker für Import
- Dateinamen mit Zeitstempel

---

## 🔗 Weitere Ressourcen

### Dokumentation:
- `VERSION_1.7_UI_IMPROVEMENTS.md` - Aktuelle Version
- `VERSION_1.6_DB_IMPORT_FIX.md` - DB-Import Fixes
- `YAML_EXPORT_FEATURE.md` - YAML Details

### Tools:
- **DB Browser for SQLite:** https://sqlitebrowser.org/
  - Zum Anschauen der .db-Dateien
- **Text-Editor:** Beliebiger Editor für .yaml-Dateien
  - VS Code, Notepad++, vim, nano, etc.

### Scripts:
- `backup_games.sh` - Backup erstellen
- `restore_games.sh` - Backup wiederherstellen
- `test_paths.sh` - Pfad-Konvertierung testen (falls vorhanden)

---

**Stand:** 06.01.2026  
**App-Version:** 1.7 (versionCode 8)  
**Script-Version:** Bash (plattformübergreifend)

**Alle Methoden funktionieren zuverlässig!** 🎉

