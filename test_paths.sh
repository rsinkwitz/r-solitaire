#!/bin/bash
# Test-Script für Pfad-Konvertierung
# Zeigt wie backupDir und backupDirOs gesetzt werden

echo "=== Pfad-Konvertierung Test ==="
echo ""

# Erkenne Betriebssystem
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    echo "Umgebung: MSYS2 (Windows)"
    export MSYS_NO_PATHCONV=1
    backupBaseDir="/d/backup"
elif [[ -d "/mnt/c" ]]; then
    echo "Umgebung: WSL"
    backupBaseDir="/mnt/d/backup"
else
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

echo ""
echo "Ergebnis:"
echo "  OSTYPE: $OSTYPE"
echo "  backupDir (für Bash): $backupDir"
echo "  backupDirOs (für Executables): $backupDirOs"
echo ""

echo "Verwendung:"
echo "  mkdir -p \"\$backupDir\"          # Unix-Pfad für Bash"
echo "  adb pull ... \"\$backupDirOs/...\" # OS-nativer Pfad für adb.exe"
echo ""

# Teste ob cygpath verfügbar ist (MSYS2)
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    if command -v cygpath &> /dev/null; then
        echo "✓ cygpath verfügbar"
        echo ""
        echo "Beispiel-Konvertierungen:"
        echo "  /d/backup → $(cygpath -w /d/backup)"
        echo "  /c/Users → $(cygpath -w /c/Users 2>/dev/null || echo 'N/A')"
    else
        echo "✗ cygpath nicht gefunden (sollte in MSYS2 vorhanden sein)"
    fi
fi

echo ""
echo "Test abgeschlossen."

