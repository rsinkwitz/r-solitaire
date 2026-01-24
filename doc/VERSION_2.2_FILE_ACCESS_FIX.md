# Version 2.2 - File Access Fix (SAF)

## Datum: 08.01.2026

## Problem

Die App konnte YAML- und DB-Dateien nicht finden, die manuell in den Download-Ordner kopiert wurden. Das Problem lag daran, dass:
1. Ab Android 10+ Scoped Storage verwendet wird
2. Apps keinen direkten Zugriff auf `/storage/emulated/0/Download` haben, ohne spezielle Berechtigungen
3. Die Berechtigung `MANAGE_EXTERNAL_STORAGE` zu weitreichend ist und von Google Play abgelehnt werden könnte

## Lösung

**Storage Access Framework (SAF) File Picker:**
- Statt Dateien direkt im Download-Ordner zu suchen, öffnet die App jetzt einen System-File-Picker
- Der Benutzer wählt die zu importierende Datei aus
- Android gewährt automatisch Zugriff auf die ausgewählte Datei über einen `content://` URI
- Die App liest die Datei über `ContentResolver` statt direktem File-Zugriff

## Änderungen

### LoadGameDialog.kt
- **File Picker Dialoge entfernt:** Die alten Dialoge, die Dateien auflisten, wurden entfernt
- **SAF Launcher hinzugefügt:** `rememberLauncherForActivityResult` mit `ActivityResultContracts.GetContent()`
- **Import Buttons:** Öffnen jetzt direkt den System-File-Picker

### SolitaireViewModel.kt
- **Neue Funktionen hinzugefügt:**
  - `importGamesFromYamlUri(uri: Uri)` - Importiert YAML über ContentResolver
  - `importDatabaseFromUri(uri: Uri)` - Importiert DB über ContentResolver
- **Alte Funktionen bleiben:** Die alten Funktionen für direkten Dateizugriff bleiben für mögliche zukünftige Verwendung

### SolitaireScreen.kt
- **Callbacks aktualisiert:** `onImportYaml` und `onImportDb` verwenden jetzt Uri statt String

## Vorteile

1. **Funktioniert auf allen Android-Versionen:** SAF ist seit Android 4.4 verfügbar
2. **Keine speziellen Berechtigungen nötig:** SAF nutzt den System-File-Picker
3. **Benutzerfreundlich:** Der Benutzer sieht alle Dateien, nicht nur im Download-Ordner
4. **Google Play konform:** Keine problematischen Berechtigungen wie `MANAGE_EXTERNAL_STORAGE`
5. **Sicher:** Die App bekommt nur Zugriff auf explizit vom Benutzer ausgewählte Dateien

## Verwendung

1. Tippen Sie auf das "Replay"-Icon
2. Tippen Sie auf "YAML Import" oder "DB Import"
3. Wählen Sie im System-File-Picker die gewünschte Datei aus
4. Die Datei wird importiert

Der File-Picker zeigt standardmäßig den Download-Ordner, aber der Benutzer kann zu jedem Ordner navigieren.

## Technische Details

- **File Picker Typ:** `ActivityResultContracts.GetContent()`
- **MIME-Type:** `"*/*"` (alle Dateien)
- **URI-Handling:** `ContentResolver.openInputStream(uri)`
- **Temporäre Dateien:** DB-Import erstellt temporäre Kopie im Cache-Ordner

## Build

```bash
./gradlew assembleRelease
```

Release APK: `r-solitaire-release.apk` (ca. 11 MB)



