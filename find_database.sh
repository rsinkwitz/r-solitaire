#!/bin/bash
# Debug-Script: Finde die R-Solitaire Datenbank auf dem Samsung
# Datum: 06.01.2026

echo "=== R-Solitaire Datenbank-Suche ==="
echo ""

# Prüfe ob ADB verfügbar ist
if ! command -v adb &> /dev/null; then
    echo "✗ ADB nicht gefunden!"
    exit 1
fi

echo "✓ ADB gefunden"
echo ""

# Prüfe ob Gerät verbunden ist
devices=$(adb devices | grep -E "device$")
if [ -z "$devices" ]; then
    echo "✗ Kein Gerät gefunden!"
    echo "Bitte Samsung per USB verbinden und USB-Debugging aktivieren."
    exit 1
fi

deviceId=$(echo "$devices" | awk '{print $1}')
echo "✓ Samsung-Gerät gefunden: $deviceId"
echo ""

echo "=== Suche nach R-Solitaire Daten ==="
echo ""

# 1. Prüfe bekannte Pfade
echo "1. Prüfe Standard-Pfade:"
echo ""

paths=(
    "/data/data/com.rsinkwitz.r_solitaire"
    "/data/user/0/com.rsinkwitz.r_solitaire"
    "/storage/emulated/0/Android/data/com.rsinkwitz.r_solitaire"
    "/sdcard/Android/data/com.rsinkwitz.r_solitaire"
)

for path in "${paths[@]}"; do
    if adb shell "test -d $path && echo exists" 2>/dev/null | grep -q "exists"; then
        echo "  ✓ Verzeichnis gefunden: $path"

        # Liste Inhalt
        echo "    Inhalt:"
        adb shell "ls -la $path 2>/dev/null" | sed 's/^/      /'
        echo ""
    else
        echo "  ✗ Nicht gefunden: $path"
    fi
done

echo ""
echo "2. Suche nach 'saved_games_database' Dateien:"
echo ""

# Suche in verschiedenen Bereichen
searchPaths=(
    "/storage/emulated/0"
    "/sdcard"
)

for searchPath in "${searchPaths[@]}"; do
    echo "   Suche in: $searchPath"
    results=$(adb shell "find $searchPath -name 'saved_games_database' 2>/dev/null" 2>/dev/null)
    if [ -n "$results" ]; then
        echo "$results" | while read -r file; do
            file=$(echo "$file" | tr -d '\r')
            size=$(adb shell "ls -lh '$file' 2>/dev/null" | awk '{print $5}')
            echo "     ✓ Gefunden: $file (Größe: $size)"
        done
    fi
done

echo ""
echo "3. Suche nach R-Solitaire App-Verzeichnissen:"
echo ""

# Suche nach allen Verzeichnissen mit r_solitaire im Namen
results=$(adb shell "find /storage/emulated/0 -type d -iname '*solitaire*' 2>/dev/null" 2>/dev/null)
if [ -n "$results" ]; then
    echo "$results" | while read -r dir; do
        dir=$(echo "$dir" | tr -d '\r')
        echo "  ✓ $dir"
        # Zeige Inhalt
        adb shell "ls -la '$dir' 2>/dev/null" | head -10 | sed 's/^/    /'
        echo ""
    done
else
    echo "  Keine Verzeichnisse gefunden"
fi

echo ""
echo "4. Prüfe App-spezifischen Storage:"
echo ""

# Prüfe files dir
filesDir="/storage/emulated/0/Android/data/com.rsinkwitz.r_solitaire/files"
if adb shell "test -d $filesDir && echo exists" 2>/dev/null | grep -q "exists"; then
    echo "  ✓ Files-Verzeichnis gefunden: $filesDir"
    echo "    Inhalt:"
    adb shell "find $filesDir -type f 2>/dev/null" | sed 's/^/      /' | head -20
else
    echo "  ✗ Kein Files-Verzeichnis in Android/data"
fi

echo ""
echo "5. Prüfe interne Datenbank-Verzeichnisse:"
echo ""

# Versuche verschiedene database Pfade
dbPaths=(
    "/data/user/0/com.rsinkwitz.r_solitaire/databases"
    "/data/data/com.rsinkwitz.r_solitaire/databases"
)

for dbPath in "${dbPaths[@]}"; do
    if adb shell "test -d $dbPath && echo exists" 2>/dev/null | grep -q "exists"; then
        echo "  ✓ Datenbank-Verzeichnis gefunden: $dbPath"
        echo "    Inhalt:"
        adb shell "ls -la $dbPath 2>/dev/null" | sed 's/^/      /'
        echo ""
    fi
done

echo ""
echo "=== Zusammenfassung ==="
echo ""
echo "Wenn eine 'saved_games_database' Datei gefunden wurde,"
echo "verwenden Sie deren Pfad im backup_samsung_saves.sh Script."
echo ""
echo "Falls nichts gefunden wurde:"
echo "1. Stellen Sie sicher, dass die App installiert ist"
echo "2. Öffnen Sie die App und speichern Sie ein Spiel"
echo "3. Führen Sie dieses Script erneut aus"
echo ""

