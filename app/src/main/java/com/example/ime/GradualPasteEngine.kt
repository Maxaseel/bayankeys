package com.example.ime

import com.example.data.model.GradualPasteConfig
import com.example.data.model.GradualPasteMode
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Shared gradual paste sequencer used both by [BayanInputMethodService]
 * and the in-app playground so saved speed, rhythm, and breathing pause
 * parameters behave identically everywhere.
 */
object GradualPasteEngine {

    suspend fun stream(
        rawText: String,
        config: GradualPasteConfig,
        onChunk: suspend (String) -> Boolean
    ): Boolean {
        delay(config.startDelayMs().toLong())
        val pulseFraction = (config.naturalRhythmPulse.coerceIn(1, 50) / 50.0) * 0.45
        val breathFactor = 0.4 + (config.breathIntensity.coerceIn(1, 50) / 50.0) * 0.9
        var unitsSinceBreath = 0

        if (config.mode == GradualPasteMode.CHAR_BY_CHAR) {
            var offset = 0
            while (offset < rawText.length) {
                val cp = rawText.codePointAt(offset)
                val count = Character.charCount(cp)
                val chunk = rawText.substring(offset, offset + count)
                offset += count

                if (!onChunk(chunk)) return false
                unitsSinceBreath++

                val isWhitespace = chunk.length == 1 && chunk[0].isWhitespace()
                val baseDelay = if (isWhitespace) config.spaceDelayMs() else config.charDelayMs()
                val jitter = if (pulseFraction > 0.0) {
                    1.0 + Random.nextDouble(-pulseFraction, pulseFraction)
                } else {
                    1.0
                }
                delay((baseDelay * jitter).toLong().coerceAtLeast(8L))

                if (unitsSinceBreath >= config.charsBeforeBreath.coerceIn(1, 50) && offset < rawText.length) {
                    unitsSinceBreath = 0
                    val breathDelay = (config.breathPauseMs() * breathFactor).toLong().coerceAtLeast(30L)
                    delay(breathDelay)
                }
            }
        } else {
            val tokens = Regex("\\S+|\\s+").findAll(rawText).map { it.value }.toList()
            for (idx in tokens.indices) {
                val token = tokens[idx]
                if (!onChunk(token)) return false
                unitsSinceBreath += token.length

                val isWhitespace = token.isNotEmpty() && token.all { it.isWhitespace() }
                val baseDelay = if (isWhitespace) config.spaceDelayMs() else config.wordDelayMs()
                val jitter = if (pulseFraction > 0.0) {
                    1.0 + Random.nextDouble(-pulseFraction, pulseFraction)
                } else {
                    1.0
                }
                delay((baseDelay * jitter).toLong().coerceAtLeast(10L))

                if (unitsSinceBreath >= config.charsBeforeBreath.coerceIn(1, 50) && idx < tokens.lastIndex) {
                    unitsSinceBreath = 0
                    val breathDelay = (config.breathPauseMs() * breathFactor).toLong().coerceAtLeast(30L)
                    delay(breathDelay)
                }
            }
        }
        return true
    }
}
