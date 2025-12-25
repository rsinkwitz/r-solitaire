package com.rsinkwitz.r_solitaire.model

data class Hole(
    val row: Int,
    val col: Int,
    var hasPeg: Boolean = true,
    var isSelected: Boolean = false
)
