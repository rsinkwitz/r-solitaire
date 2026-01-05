package com.rsinkwitz.r_solitaire.util

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generiert eine WAV-Datei mit der C-E-G-C Melodie
 */
object WavGenerator {

    private const val SAMPLE_RATE = 44100

    fun generateCongratulationsWav(outputFile: File) {
        // Töne: C4 (200ms), E4 (200ms), G4 (200ms), C5 (500ms) mit Pausen
        val tones = listOf(
            Tone(261.63, 200),  // C4
            Tone(0.0, 50),      // Pause
            Tone(329.63, 200),  // E4
            Tone(0.0, 50),      // Pause
            Tone(392.00, 200),  // G4
            Tone(0.0, 50),      // Pause
            Tone(523.25, 500)   // C5
        )

        // Generiere alle Samples
        val allSamples = mutableListOf<Short>()
        for (tone in tones) {
            allSamples.addAll(generateTone(tone.frequency, tone.durationMs))
        }

        // Schreibe WAV-Datei
        writeWavFile(outputFile, allSamples.toShortArray())
    }

    private fun generateTone(frequencyHz: Double, durationMs: Int): List<Short> {
        val numSamples = (durationMs * SAMPLE_RATE / 1000)
        val samples = mutableListOf<Short>()

        if (frequencyHz == 0.0) {
            // Pause - Stille
            repeat(numSamples) { samples.add(0) }
            return samples
        }

        // Fade-In/Out Zeiten
        val fadeInSamples = (SAMPLE_RATE * 5 / 1000).coerceAtMost(numSamples / 4)
        val fadeOutSamples = (SAMPLE_RATE * 10 / 1000).coerceAtMost(numSamples / 4)

        // Generiere Sinuswelle mit sanften Übergängen
        for (i in 0 until numSamples) {
            val phase = 2.0 * PI * i / (SAMPLE_RATE / frequencyHz)
            var sample = sin(phase) * Short.MAX_VALUE * 0.35  // 35% Lautstärke

            // Fade-In (sanfter Start)
            if (i < fadeInSamples) {
                val fadeFactor = i.toDouble() / fadeInSamples
                // Quadratisches Fade-In für noch sanfteren Start
                sample *= fadeFactor * fadeFactor
            }

            // Fade-Out (sanftes Ende)
            if (i >= numSamples - fadeOutSamples) {
                val fadeIndex = i - (numSamples - fadeOutSamples)
                val fadeFactor = 1.0 - (fadeIndex.toDouble() / fadeOutSamples)
                // Quadratisches Fade-Out für noch sanfteres Ende
                sample *= fadeFactor * fadeFactor
            }

            samples.add(sample.toInt().toShort())
        }

        return samples
    }

    private fun writeWavFile(file: File, samples: ShortArray) {
        FileOutputStream(file).use { fos ->
            val dataSize = samples.size * 2
            val fileSize = dataSize + 36

            // WAV Header schreiben
            fos.write("RIFF".toByteArray())
            fos.write(intToBytes(fileSize))
            fos.write("WAVE".toByteArray())

            // fmt Chunk
            fos.write("fmt ".toByteArray())
            fos.write(intToBytes(16))  // Chunk size
            fos.write(shortToBytes(1))  // Audio format (PCM)
            fos.write(shortToBytes(1))  // Channels (Mono)
            fos.write(intToBytes(SAMPLE_RATE))  // Sample rate
            fos.write(intToBytes(SAMPLE_RATE * 2))  // Byte rate
            fos.write(shortToBytes(2))  // Block align
            fos.write(shortToBytes(16))  // Bits per sample

            // data Chunk
            fos.write("data".toByteArray())
            fos.write(intToBytes(dataSize))

            // Audio data
            for (sample in samples) {
                fos.write(shortToBytes(sample.toInt()))
            }
        }
    }

    private fun intToBytes(value: Int): ByteArray {
        return ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(value)
            .array()
    }

    private fun shortToBytes(value: Int): ByteArray {
        return ByteBuffer.allocate(2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(value.toShort())
            .array()
    }

    private data class Tone(val frequency: Double, val durationMs: Int)
}

