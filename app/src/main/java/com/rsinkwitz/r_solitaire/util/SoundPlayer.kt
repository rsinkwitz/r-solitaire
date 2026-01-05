package com.rsinkwitz.r_solitaire.util

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SoundPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val wavFile = File(context.filesDir, "congratulations.wav")

    init {
        // Generiere WAV-Datei beim ersten Start, falls nicht vorhanden
        if (!wavFile.exists()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    WavGenerator.generateCongratulationsWav(wavFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun playCongratulationsSound() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Warte bis WAV-Datei existiert
                var attempts = 0
                while (!wavFile.exists() && attempts < 50) {
                    Thread.sleep(100)
                    attempts++
                }

                if (!wavFile.exists()) {
                    // Fallback: Generiere jetzt
                    WavGenerator.generateCongratulationsWav(wavFile)
                }

                // Spiele Sound mit MediaPlayer ab
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(wavFile.absolutePath)
                    prepare()
                    setOnCompletionListener { mp ->
                        mp.release()
                    }
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

