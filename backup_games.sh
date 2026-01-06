#!/bin/bash
# Backup der R-Solitaire Spielstände vom Samsung (Download-Ordner)
# Datum: 06.01.2026
# Kompatibel mit: MSYS2, WSL, Linux, macOS

# ========== Umgebungs-Erkennung ==========

# Erkenne Betriebssystem und setze Backup-Pfad
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    # MSYS2 auf Windows
    echo "Umgebung: MSYS2 (Windows)"
    export MSYS_NO_PATHCONV=1
    backupBaseDir="/d/backup"
elif [[ -d "/mnt/c" ]]; then
    # WSL (Windows Subsystem for Linux)
    echo "Umgebung: WSL"
    backupBaseDir="/mnt/d/backup"
else
    # Unix/Linux/macOS
    echo "Umgebung: Unix/Linux/macOS"
    backupBaseDir="$HOME/backup"
fi

timestamp=$(date +%Y%m%d_%H%M%S)
backupDir="${backupBaseDir}/r_solitaire_saves_${timestamp}"

# Für Windows-Executables (wie adb.exe) brauchen wir Windows-Pfade
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    backupDirOs=$(cygpath -w "$backupDir")
else
    backupDirOs="$backupDir"
fi

echo "=== R-Solitaire Spielstände sichern (Download-Ordner) ==="
echo ""

# Backup-Verzeichnis erstellen
mkdir -p "$backupDir"

# ========== ADB Prüfung ==========

if ! command -v adb &> /dev/null; then
    echo "✗ ADB nicht gefunden!"
    echo ""
    echo "Installation:"
    echo "  - MSYS2: pacman -S android-tools"
    echo "  - Linux: sudo apt install android-tools-adb"
    echo "  - macOS: brew install android-platform-tools"
    echo "  - Oder: Android Studio SDK Platform-Tools"
    exit 1
fi

echo "✓ ADB gefunden"
echo ""

# ========== Geräte-Prüfung ==========

devices=$(adb devices | grep -E "device$")
if [ -z "$devices" ]; then
    echo "✗ Kein Gerät gefunden!"
    echo ""
    echo "Bitte:"
    echo "1. Samsung per USB verbinden"
    echo "2. USB-Debugging aktivieren:"
    echo "   - Einstellungen → Über das Telefon"
    echo "   - 7x auf Build-Nummer tippen"
    echo "   - Entwickleroptionen → USB-Debugging AN"
    exit 1
fi

deviceId=$(echo "$devices" | awk '{print $1}')
echo "✓ Samsung-Gerät gefunden: $deviceId"
echo ""

# ========== Export-Anleitung ==========

echo "=== Export via App-Button ==="
echo ""
echo "Die App exportiert die Datenbank in den Download-Ordner:"
echo ""
echo "1. Öffnen Sie R-Solitaire auf dem Samsung"
echo "2. Tippen Sie den UPLOAD-Button (↑) oben rechts"
echo "3. Falls nach Berechtigung gefragt: 'Speicher' erlauben"
echo "4. Die App kopiert nach:"
echo "   /storage/emulated/0/Download/r_solitaire_backup.db"
echo ""
read -p "Haben Sie den Export durchgeführt? (j/N): " exported

if [[ ! "$exported" =~ ^[jJyY]$ ]]; then
    echo ""
    echo "Bitte führen Sie den Export in der App durch und starten Sie dieses Script erneut."
    exit 0
fi

# ========== Suche exportierte Datenbank ==========

echo ""
echo "Suche exportierte Datenbank im Download-Ordner..."

# Der öffentliche Download-Ordner - IMMER zugänglich!
exportPath="/storage/emulated/0/Download/r_solitaire_backup.db"

echo "Prüfe: $exportPath"

if adb shell "test -f $exportPath && echo exists" 2>/dev/null | grep -q "exists"; then
    echo "  ✓ Export gefunden!"

    # Zeige Dateigröße
    size=$(adb shell "ls -lh $exportPath 2>/dev/null" | awk '{print $5}')
    echo "  Größe: $size"
else
    echo "  ✗ Export nicht gefunden!"
    echo ""
    echo "Bitte prüfen Sie:"
    echo ""
    echo "1. Haben Sie den Upload-Button (↑) in der App getippt?"
    echo ""
    echo "2. Welche Meldung hat die App gezeigt?"
    echo "   - 'Export erfolgreich' → Notieren Sie den Pfad"
    echo "   - 'Permission denied' → Storage-Berechtigung fehlt"
    echo ""
    echo "3. Falls Permission-Problem:"
    echo "   Samsung: Einstellungen → Apps → R-Solitaire → Berechtigungen"
    echo "   → 'Speicher' erlauben"
    echo ""
    echo "4. Dann erneut Upload-Button tippen und Script ausführen"
    echo ""

    # Alternative: Manuell im Download-Ordner suchen
    echo "Suche nach beliebigen .db Dateien im Download-Ordner..."
    adb shell "ls -la /storage/emulated/0/Download/*.db 2>/dev/null"

    exit 1
fi

# ========== Backup erstellen ==========

echo ""
echo "Kopiere exportierte Datenbank..."

if adb pull "$exportPath" "$backupDirOs/saved_games.db" 2>&1 | grep -q "pulled"; then
    echo "  ✓ saved_games.db"
else
    echo "  ✗ Fehler beim Kopieren"
    exit 1
fi

# ========== Backup-Info erstellen ==========

# Konvertiere Pfad für Anzeige
displayPath="$backupDir"
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    # Zeige Windows-Pfad in MSYS2
    displayPath=$(cygpath -w "$backupDir" 2>/dev/null || echo "$backupDir")
fi

echo ""
echo "=== Backup erfolgreich! ==="
echo ""
echo "Gespeichert in:"
echo "  $displayPath"
echo ""

if [ -f "$backupDir/saved_games.db" ]; then
    sizeKB=$(du -k "$backupDir/saved_games.db" | cut -f1)
    echo "Datenbankgröße: ${sizeKB} KB"
fi

# Info-Datei erstellen
cat > "$backupDir/BACKUP_INFO.txt" << EOF
# R-Solitaire Spielstände Backup

**Backup erstellt:** $(date '+%d.%m.%Y %H:%M:%S')
**Gerät:** $deviceId
**Methode:** App-Export-Funktion (Download-Ordner)
**Umgebung:** $OSTYPE

## Wie das Backup erstellt wurde:

1. Upload-Button (↑) in der App getippt
2. App kopiert Datenbank nach:
   /storage/emulated/0/Download/r_solitaire_backup.db
3. Dieses Script kopiert die Datei auf den PC via 'adb pull'

## Wiederherstellung:

### Methode 1: Via Script (empfohlen)
./restore_samsung_saves.sh "$backupDir"

### Methode 2: Manuell via ADB
adb push "$backupDir/saved_games.db" /storage/emulated/0/Download/r_solitaire_backup.db
# Dann in der App: Download-Button (↓) tippen (wenn implementiert)

### Datenbank anschauen:
DB Browser for SQLite: https://sqlitebrowser.org/
Datei: saved_games.db
Tabelle: saved_games

---
**Backup-Pfad:** $backupDir
**Plattform:** $OSTYPE
EOF

echo ""
echo "Info-Datei erstellt: BACKUP_INFO.txt"
echo ""

