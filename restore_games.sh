#!/bin/bash
# Wiederherstellung der R-Solitaire Spielstände aufs Samsung (Download-Ordner)
# Datum: 06.01.2026
# Kompatibel mit: MSYS2, WSL, Linux, macOS

# ========== Umgebungs-Erkennung ==========

if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    # MSYS2 auf Windows
    echo "Umgebung: MSYS2 (Windows)"
    export MSYS_NO_PATHCONV=1
    backupBaseDir="/d/backup"
elif [[ -d "/mnt/c" ]]; then
    # WSL
    echo "Umgebung: WSL"
    backupBaseDir="/mnt/d/backup"
else
    # Unix/Linux/macOS
    echo "Umgebung: Unix/Linux/macOS"
    backupBaseDir="$HOME/backup"
fi

echo "=== R-Solitaire Spielstände wiederherstellen ==="
echo ""

# ========== Parameter-Prüfung ==========

if [ $# -eq 0 ]; then
    echo "Verwendung: ./restore_samsung.sh <Backup-Verzeichnis>"
    echo ""
    echo "Beispiel:"
    echo "  ./restore_samsung.sh ${backupBaseDir}/r_solitaire_saves_20260106_120000"
    echo ""
    echo "Verfügbare Backups:"
    if [ -d "$backupBaseDir" ]; then
        ls -dt ${backupBaseDir}/r_solitaire_saves_* 2>/dev/null | head -5
    else
        echo "  (Kein Backup-Verzeichnis gefunden)"
    fi
    exit 1
fi

backupDir="$1"

# Für Windows-Executables (wie adb.exe) brauchen wir Windows-Pfade
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    backupDirOs=$(cygpath -w "$backupDir")
else
    backupDirOs="$backupDir"
fi

# Prüfe ob Backup-Verzeichnis existiert
if [ ! -d "$backupDir" ]; then
    echo "✗ Backup-Verzeichnis nicht gefunden: $backupDir"
    exit 1
fi

# Prüfe ob Datenbank-Datei existiert
if [ ! -f "$backupDir/saved_games.db" ]; then
    echo "✗ Datenbank-Datei 'saved_games.db' nicht gefunden in: $backupDir"
    echo ""
    echo "Hinweis: Neue Backups verwenden 'saved_games.db'"
    echo "         Alte Backups verwendeten 'saved_games_database'"
    exit 1
fi

echo "Backup-Verzeichnis: $backupDir"
echo ""

# ========== ADB Prüfung ==========

if ! command -v adb &> /dev/null; then
    echo "✗ ADB nicht gefunden!"
    exit 1
fi

echo "✓ ADB gefunden"
echo ""

# ========== Geräte-Prüfung ==========

devices=$(adb devices | grep -E "device$")
if [ -z "$devices" ]; then
    echo "✗ Kein Gerät gefunden!"
    echo "Bitte Samsung per USB verbinden und USB-Debugging aktivieren."
    exit 1
fi

deviceId=$(echo "$devices" | awk '{print $1}')
echo "✓ Samsung-Gerät gefunden: $deviceId"
echo ""

# ========== Warnung ==========

echo "⚠️  WARNUNG ⚠️"
echo "Dies wird die aktuellen Spielstände auf dem Gerät überschreiben!"
echo ""
echo "Backup-Info:"
dbSize=$(du -h "$backupDir/saved_games.db" | cut -f1)
echo "  Datei: saved_games.db"
echo "  Größe: $dbSize"
echo ""
read -p "Möchten Sie fortfahren? (j/N): " confirm

if [[ ! "$confirm" =~ ^[jJyY]$ ]]; then
    echo "Abgebrochen."
    exit 0
fi

# ========== Wiederherstellung ==========

echo ""
echo "Kopiere Datenbank aufs Samsung (Download-Ordner)..."

# Ziel: Download-Ordner auf Samsung
restorePath="/storage/emulated/0/Download/r_solitaire_backup.db"

# Kopiere Datenbank in Download-Ordner (verwende backupDirOs für adb.exe)
if adb push "$backupDirOs/saved_games.db" "$restorePath" 2>&1 | grep -q "pushed"; then
    echo "  ✓ Datenbank in Download-Ordner kopiert"
else
    echo "  ✗ Fehler beim Kopieren der Datenbank"
    exit 1
fi

echo ""
echo "=== Datenbank auf Samsung bereitgestellt! ==="
echo ""
echo "Nächster Schritt:"
echo ""
echo "WICHTIG: Die Import-Funktion in der App ist noch nicht implementiert!"
echo ""
echo "Manuelle Wiederherstellung (erfordert Root oder adb root):"
echo "  adb shell"
echo "  su  # (falls Root verfügbar)"
echo "  cp /storage/emulated/0/Download/r_solitaire_backup.db \\"
echo "     /data/data/com.rsinkwitz.r_solitaire/databases/saved_games_database"
echo ""
echo "ODER:"
echo "Warten Sie auf das nächste App-Update mit Import-Button (↓)"
echo "Dann:"
echo "  1. Download-Button (↓) in der App tippen"
echo "  2. App importiert automatisch aus Download-Ordner"
echo ""
echo "Die Datei liegt bereit unter:"
echo "  $restorePath"
echo ""

