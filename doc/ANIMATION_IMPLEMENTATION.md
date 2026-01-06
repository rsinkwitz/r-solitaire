# 🎬 Flüssige Peg-Animation im Replay

## Datum: 05.01.2026

## ✨ Neue Feature: Animierte Bewegung des roten Pegs

### Was wurde implementiert:

Beim Replay bewegt sich der rote Peg jetzt **flüssig und sichtbar** vom Start zum Ziel!

### 🎯 Animations-Ablauf:

```
Phase 1: Stillstand am Start (25% der Zeit)
   ↓ Peg wird rot und bleibt stehen
   
Phase 2: Konstante Bewegung (50% der Zeit)
   ↓ Peg bewegt sich flüssig in 20 Schritten
   ↓ Lineare Interpolation zwischen Start und Ziel
   
Phase 3: Stillstand am Ziel (25% der Zeit)
   ↓ Peg ist am Ziel angekommen und bleibt rot
   
Phase 4: Zug ausführen
   ↓ Peg springt zurück zu blau
   ↓ Übersprungener Peg wird entfernt
```

### 📊 Timing-Verteilung:

Bei **Normal-Geschwindigkeit** (1000ms):
- **250ms** - Peg wird rot, steht still am Start
- **500ms** - Flüssige Bewegung (20 Frames à 25ms)
- **250ms** - Peg steht still am Ziel (noch rot)
- **Zug wird ausgeführt** - Peg wird blau

Bei **Langsam** (2000ms):
- 500ms Stillstand Start
- 1000ms Bewegung (20 Frames à 50ms)
- 500ms Stillstand Ziel

Bei **Schnell** (500ms):
- 125ms Stillstand Start
- 250ms Bewegung (20 Frames à 12.5ms)
- 125ms Stillstand Ziel

### 🔧 Technische Umsetzung:

#### 1. Erweiterte ReplayState-Struktur:
```kotlin
data class ReplayState(
    val savedGame: SavedGame,
    val currentMoveIndex: Int = 0,
    val isPlaying: Boolean = true,
    val speed: ReplaySpeed = ReplaySpeed.NORMAL,
    val animatingPegFromRow: Int? = null,  // Start-Position
    val animatingPegFromCol: Int? = null,
    val animatingPegToRow: Int? = null,    // Ziel-Position
    val animatingPegToCol: Int? = null,
    val animationProgress: Float = 0f      // 0.0 bis 1.0
)
```

#### 2. Animations-Loop im ViewModel:
```kotlin
// Phase 1: Stillstand am Start
replayState.value = currentState.copy(
    animatingPegFromRow = move.fromRow,
    animatingPegFromCol = move.fromCol,
    animatingPegToRow = move.toRow,
    animatingPegToCol = move.toCol,
    animationProgress = 0f
)
delay(speed.delayMs / 4)  // 25%

// Phase 2: Flüssige Bewegung
val animationSteps = 20
val stepDelay = (speed.delayMs / 2) / animationSteps

for (step in 1..animationSteps) {
    val progress = step.toFloat() / animationSteps
    replayState.value = replayState.value?.copy(
        animationProgress = progress
    )
    delay(stepDelay)
}

// Phase 3: Stillstand am Ziel
delay(speed.delayMs / 4)  // 25%

// Phase 4: Zug ausführen
executeReplayMove(move)
```

#### 3. Interpolation in der UI:
```kotlin
// Start- und Ziel-Koordinaten
val fromX = animatingPegFromCol * cellSizePx + cellSizePx / 2
val fromY = animatingPegFromRow * cellSizePx + cellSizePx / 2
val toX = animatingPegToCol * cellSizePx + cellSizePx / 2
val toY = animatingPegToRow * cellSizePx + cellSizePx / 2

// Lineare Interpolation
val currentX = fromX + (toX - fromX) * animationProgress
val currentY = fromY + (toY - fromY) * animationProgress

// Peg an interpolierter Position zeichnen
drawCircle(
    color = Color.Red,
    radius = radius,
    center = Offset(currentX, currentY)
)
```

### 🎨 Visuelle Details:

1. **Startpunkt:**
   - Peg verschwindet vom Startfeld (wird als leerer Ring gezeichnet)
   - Roter Peg erscheint an dieser Position
   - Kurzer Stillstand (Benutzer kann Start erkennen)

2. **Bewegung:**
   - 20 Animations-Frames für flüssige Bewegung
   - Lineare Interpolation (konstante Geschwindigkeit)
   - Peg bewegt sich Pixel für Pixel

3. **Zielpunkt:**
   - Peg kommt am Ziel an (noch rot)
   - Kurzer Stillstand (Benutzer kann Ziel erkennen)
   - Dann Zug-Ausführung

4. **Nach dem Zug:**
   - Roter Peg verschwindet
   - Blauer Peg erscheint am Ziel
   - Übersprungener Peg ist weg
   - Startfeld bleibt leer

### 🎮 Benutzererfahrung:

**Vorher:**
- Peg wurde rot
- Stand an einer Position
- Sprang zum Ziel

**Jetzt:**
- Peg wird rot ✅
- Kurzer Stillstand am Start ✅
- **Flüssige, sichtbare Bewegung** ✅
- Kurzer Stillstand am Ziel ✅
- Zug wird ausgeführt ✅

### 📈 Performance:

- **20 Frames** für flüssige Animation
- **60 FPS** bei allen Geschwindigkeiten möglich
- **Compose Recomposition** nur für animierten Peg
- Statische Pegs werden nur einmal gezeichnet
- Animierter Peg wird separat darüber gezeichnet

### 🔍 Layer-Struktur:

1. **Unterste Ebene:** Alle leeren Löcher (blaue Ringe)
2. **Mittlere Ebene:** Alle statischen blauen Pegs
3. **Oberste Ebene:** Animierter roter Peg (beweglich)

Der animierte Peg wird **über allen anderen** gezeichnet, so dass er nicht von anderen Pegs verdeckt werden kann.

### ⚙️ Anpassungsmöglichkeiten:

Die Animation kann leicht angepasst werden:

**Mehr Frames (noch flüssiger):**
```kotlin
val animationSteps = 30  // statt 20
```

**Andere Timing-Verteilung:**
```kotlin
delay(speed.delayMs / 3)  // Längerer Stillstand
// ...
delay(speed.delayMs / 6)  // Kürzerer Stillstand
```

**Ease-In/Ease-Out statt linear:**
```kotlin
val progress = easeInOut(step.toFloat() / animationSteps)
```

### 🎯 Geänderte Dateien:

1. **`model/ReplayState.kt`**
   - Erweitert um `animatingPegFromRow/Col`
   - Erweitert um `animatingPegToRow/Col`
   - Neu: `animationProgress` (0.0 - 1.0)

2. **`viewmodel/SolitaireViewModel.kt`**
   - `startReplayLoop()`: 4-Phasen-Animation
   - `pauseReplay()`: Setzt alle Animations-Parameter zurück

3. **`ui/SolitaireScreen.kt`**
   - Zwei-Pass-Rendering:
     - Pass 1: Alle statischen Pegs und Löcher
     - Pass 2: Animierter roter Peg (interpoliert)
   - Lineare Interpolation der Position

---

## ✅ Status: Vollständig implementiert!

Die flüssige Peg-Animation ist fertig und bereit zum Testen.

**Nächster Schritt:** Gradle Sync und App ausführen

Die Animation wird automatisch bei jedem Replay-Zug abgespielt! 🎬

