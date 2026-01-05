package com.rsinkwitz.r_solitaire.model

data class SavedGame(
    val id: String,
    val title: String,
    val timestamp: Long,
    val initialHoleRow: Int,
    val initialHoleCol: Int,
    val moves: List<Move>
)

