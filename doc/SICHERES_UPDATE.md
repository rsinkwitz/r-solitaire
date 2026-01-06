# ✅ Sicheres Update ohne Datenverlust

## ⚠️ WICHTIG: Ihre Spiele bleiben erhalten!

### Version erhöht:
- **Alt:** Version 1.0 (versionCode 1)
- **Neu:** Version 1.1 (versionCode 2)

**Das bedeutet:** Android erkennt es als **UPDATE**, nicht als Neuinstallation!

---

## 📱 Sicheres Update durchführen:

### Option 1: Mit ADB (empfohlen)
```bash
# Update statt Neuinstallation (-r Flag)
adb install -r D:\Users\Rainer\Documents\0-Rainer\dev\mobile\r_solitaire\app\build\outputs\apk\release\r-solitaire-release.apk
```

**Das `-r` Flag bedeutet:**
- Ersetzt die bestehende App
- **Behält alle Daten** (Datenbank bleibt erhalten!)
- Nur der Code wird aktualisiert

### Option 2: Manuell auf Samsung
1. APK auf Samsung kopieren
2. Datei antippen
3. **"Update"** oder **"Ersetzen"** wählen (NICHT "Deinstallieren"!)
4. Installieren bestätigen

---

## ✅ Ihre Daten sind sicher weil:

1. **Gleiche applicationId:** `com.rsinkwitz.r_solitaire`
   - Android erkennt es als dieselbe App
   
2. **Gleicher Signing Key:** `rsinkwitz-release-key.jks`
   - Nur Updates mit demselben Key sind erlaubt
   
3. **Höhere versionCode:** 1 → 2
   - Android erkennt: Das ist ein Update!

4. **Datenbank-Pfad unverändert:**
   - `/data/data/com.rsinkwitz.r_solitaire/databases/saved_games_database`
   - Wird bei Update NICHT gelöscht

---

## 🎯 Nach dem Update:

### Die App hat jetzt:
- ✅ Alle Ihre gespeicherten Spiele (bleiben erhalten!)
- ✅ Neuer Upload-Button (↑) für Export
- ✅ Alle bisherigen Features
- ✅ Neustart-Bestätigungsdialog
- ✅ RestartAlt-Icon

### Testen Sie den Export:
1. App öffnen
2. Prüfen: Sind Ihre gespeicherten Spiele noch da? ✅
3. Upload-Button (↑) tippen (oben rechts)
4. Meldung erscheint: "Backup erstellt: /storage/..."
5. Jetzt Backup mit Script erstellen:
   ```bash
   ./backup_with_export.sh
   ```

---

## 📊 Versionsübersicht:

| Version | versionCode | Features | APK-Datum |
|---------|-------------|----------|-----------|
| 1.0 | 1 | Basis-Features | 05.01.2026 |
| 1.1 | 2 | + Export-Button | 06.01.2026 |

---

## ⚠️ Nur bei Debug ↔ Release Wechsel:

**Das Problem beim letzten Mal:**
- Debug-APK: Signing mit Debug-Key
- Release-APK: Signing mit Release-Key
- **Unterschiedliche Keys** → Android sieht es als andere App
- → Neuinstallation erforderlich → Daten verloren

**Jetzt:**
- Beide sind Release-APKs
- Beide mit demselben Key signiert
- → Sicheres Update möglich! ✅

---

## 🚀 Installation durchführen:

```bash
# Samsung per USB verbinden
# Dann:
adb install -r app/build/outputs/apk/release/r-solitaire-release.apk
```

**Output sollte sein:**
```
Performing Streamed Install
Success
```

**NICHT:**
```
INSTALL_FAILED_UPDATE_INCOMPATIBLE
```

Wenn doch "INCOMPATIBLE" erscheint:
→ Unterschiedliche Signing Keys
→ Dann müsste erst deinstalliert werden (mit Datenverlust)

---

## 🛡️ Sicherheitscheck vor Update:

### Falls Sie ganz sicher gehen wollen:

1. **Prüfe installierte Version:**
   ```bash
   adb shell dumpsys package com.rsinkwitz.r_solitaire | grep versionCode
   ```
   Sollte zeigen: `versionCode=1`

2. **Prüfe Signing:**
   ```bash
   adb shell dumpsys package com.rsinkwitz.r_solitaire | grep signatures
   ```
   Notieren Sie die Signatur

3. **Nach Update prüfen:**
   ```bash
   adb shell dumpsys package com.rsinkwitz.r_solitaire | grep versionCode
   ```
   Sollte zeigen: `versionCode=2`

4. **Spiele prüfen:**
   - App öffnen
   - Liste öffnen
   - Sind Ihre Spiele noch da? ✅

---

## ✅ Zusammenfassung:

- **Version erhöht:** 1.0 → 1.1 ✅
- **Neues APK erstellt:** 12:06 Uhr ✅
- **Gleicher Signing Key:** ✅
- **Update-fähig:** ✅
- **Daten bleiben erhalten:** ✅

**Sie können das Update sicher installieren!** 🎉

---

**APK bereit:**
```
D:\Users\Rainer\Documents\0-Rainer\dev\mobile\r_solitaire\app\build\outputs\apk\release\r-solitaire-release.apk
```

**Größe:** 10.6 MB  
**Version:** 1.1 (versionCode 2)  
**Erstellt:** 06.01.2026, 12:06 Uhr

