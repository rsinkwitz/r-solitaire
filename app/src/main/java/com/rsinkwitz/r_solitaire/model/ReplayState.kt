package com.rsinkwitz.r_solitaire.model

data class ReplayState(
    val savedGame: SavedGame,
    val currentMoveIndex: Int = 0,
    val isPlaying: Boolean = true,
    val speed: ReplaySpeed = ReplaySpeed.NORMAL,
    val animatingPegFromRow: Int? = null,  // Start-Position des animierten Pegs
    val animatingPegFromCol: Int? = null,
    val animatingPegToRow: Int? = null,    // Ziel-Position des animierten Pegs
    val animatingPegToCol: Int? = null,
    val animationProgress: Float = 0f      // 0.0 bis 1.0 für Interpolation
)

