package com.rsinkwitz.r_solitaire.model

enum class ReplaySpeed(val delayMs: Long, val displayName: String) {
    SLOW(2000, "Langsam"),
    NORMAL(1000, "Normal"),
    FAST(500, "Schnell");

    fun next(): ReplaySpeed = when (this) {
        SLOW -> NORMAL
        NORMAL -> FAST
        FAST -> SLOW
    }
}

