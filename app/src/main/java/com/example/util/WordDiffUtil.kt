package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

enum class DiffKind {
    UNCHANGED,
    WRONG_REMOVED,
    CORRECTED_ADDED
}

data class DiffSegment(
    val text: String,
    val kind: DiffKind
)

object WordDiffUtil {

    private val wrongColor = Color(0xFFEF4444)
    private val correctedColor = Color(0xFF10B981)

    fun computeWordDiff(original: String, corrected: String): List<DiffSegment> {
        val origTokens = tokenizeWords(original)
        val corrTokens = tokenizeWords(corrected)

        val n = origTokens.size
        val m = corrTokens.size
        val dp = Array(n + 1) { IntArray(m + 1) }

        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                dp[i][j] = if (origTokens[i] == corrTokens[j]) {
                    1 + dp[i + 1][j + 1]
                } else {
                    maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        val raw = mutableListOf<DiffSegment>()
        var i = 0
        var j = 0
        while (i < n && j < m) {
            when {
                origTokens[i] == corrTokens[j] -> {
                    raw.add(DiffSegment(origTokens[i], DiffKind.UNCHANGED))
                    i++
                    j++
                }
                dp[i + 1][j] >= dp[i][j + 1] -> {
                    raw.add(DiffSegment(origTokens[i], DiffKind.WRONG_REMOVED))
                    i++
                }
                else -> {
                    raw.add(DiffSegment(corrTokens[j], DiffKind.CORRECTED_ADDED))
                    j++
                }
            }
        }
        while (i < n) {
            raw.add(DiffSegment(origTokens[i], DiffKind.WRONG_REMOVED))
            i++
        }
        while (j < m) {
            raw.add(DiffSegment(corrTokens[j], DiffKind.CORRECTED_ADDED))
            j++
        }

        return raw
    }

    fun buildDiffAnnotatedString(
        original: String,
        corrected: String,
        unchangedColor: Color
    ): AnnotatedString {
        val segments = computeWordDiff(original, corrected)
        return buildAnnotatedString {
            segments.forEachIndexed { idx, seg ->
                if (seg.text.isBlank()) {
                    append(seg.text)
                } else {
                    when (seg.kind) {
                        DiffKind.UNCHANGED -> {
                            withStyle(SpanStyle(color = unchangedColor)) {
                                append(seg.text)
                            }
                        }
                        DiffKind.WRONG_REMOVED -> {
                            withStyle(
                                SpanStyle(
                                    color = wrongColor,
                                    background = wrongColor.copy(alpha = 0.16f),
                                    textDecoration = TextDecoration.LineThrough,
                                    fontWeight = FontWeight.Medium
                                )
                            ) {
                                append(" ${seg.text} ")
                            }
                        }
                        DiffKind.CORRECTED_ADDED -> {
                            withStyle(
                                SpanStyle(
                                    color = correctedColor,
                                    background = correctedColor.copy(alpha = 0.16f),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(" ${seg.text} ")
                            }
                        }
                    }
                    // Ensure spacing between adjacent non-whitespace tokens if needed
                    val next = segments.getOrNull(idx + 1)
                    if (next != null && next.text.isNotBlank() && seg.kind == DiffKind.UNCHANGED && next.kind == DiffKind.UNCHANGED) {
                        append(" ")
                    }
                }
            }
        }
    }

    private fun tokenizeWords(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        val regex = Regex("\\S+|\\s+")
        return regex.findAll(text).map { it.value }.toList()
    }
}
