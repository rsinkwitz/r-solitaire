package com.rsinkwitz.r_solitaire.model

data class ReplayState(
    val savedGame: SavedGame,
    val currentMoveIndex: Int = 0,
    val isPlaying: Boolean = true,
    val speed: ReplaySpeed = ReplaySpeed.NORMAL,
    val animatingPegRow: Int? = null,  // Row des animierten Pegs (wird rot)
    val animatingPegCol: Int? = null   // Col des animierten Pegs (wird rot)
)

