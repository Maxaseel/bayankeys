package com.example.ime

import com.example.data.model.KeyboardLanguage
import com.example.data.model.NumberRowStyle

enum class KeyboardLayer {
    LETTERS,
    SYMBOLS_1,
    SYMBOLS_2,
    NUMPAD
}

data class KeySpec(
    val label: String,
    val output: String = label,
    val shiftLabel: String = label.uppercase(),
    val shiftOutput: String = output.uppercase(),
    val longPressChars: List<String> = emptyList(),
    val weight: Float = 1f,
    val kind: KeyKind = KeyKind.CHARACTER
)

enum class KeyKind {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    SYMBOLS_TOGGLE,
    LANGUAGE_SWITCH,
    SPACE,
    ENTER,
    EMOJI
}

object KeyboardLayouts {

    val numberRow: List<KeySpec> = listOf(
        KeySpec("1", "1", "1", "1", listOf("١")),
        KeySpec("2", "2", "2", "2", listOf("٢")),
        KeySpec("3", "3", "3", "3", listOf("٣")),
        KeySpec("4", "4", "4", "4", listOf("٤")),
        KeySpec("5", "5", "5", "5", listOf("٥")),
        KeySpec("6", "6", "6", "6", listOf("٦")),
        KeySpec("7", "7", "7", "7", listOf("٧")),
        KeySpec("8", "8", "8", "8", listOf("٨")),
        KeySpec("9", "9", "9", "9", listOf("٩")),
        KeySpec("0", "0", "0", "0", listOf("٠"))
    )

    val numberRowArabic: List<KeySpec> = listOf(
        KeySpec("١", "١", "١", "١", listOf("1")),
        KeySpec("٢", "٢", "٢", "٢", listOf("2")),
        KeySpec("٣", "٣", "٣", "٣", listOf("3")),
        KeySpec("٤", "٤", "٤", "٤", listOf("4")),
        KeySpec("٥", "٥", "٥", "٥", listOf("5")),
        KeySpec("٦", "٦", "٦", "٦", listOf("6")),
        KeySpec("٧", "٧", "٧", "٧", listOf("7")),
        KeySpec("٨", "٨", "٨", "٨", listOf("8")),
        KeySpec("٩", "٩", "٩", "٩", listOf("9")),
        KeySpec("٠", "٠", "٠", "٠", listOf("0"))
    )

    private val arabicRowsNormal: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("ض", "ض", "َ", "َ"),
            KeySpec("ص", "ص", "ً", "ً"),
            KeySpec("ث", "ث", "ُ", "ُ"),
            KeySpec("ق", "ق", "ٌ", "ٌ", listOf("ڨ")),
            KeySpec("ف", "ف", "ِ", "ِ", listOf("ڤ", "ڢ", "ڥ")),
            KeySpec("غ", "غ", "ٍ", "ٍ"),
            KeySpec("ع", "ع", "ْ", "ْ"),
            KeySpec("ه", "ه", "ّ", "ّ", listOf("ﻫ", "ه‍")),
            KeySpec("خ", "خ", "أ", "أ"),
            KeySpec("ح", "ح", "إ", "إ"),
            KeySpec("ج", "ج", "آ", "آ", listOf("چ"))
        ),
        listOf(
            KeySpec("ش", "ش", "ـ", "ـ", listOf("ڜ")),
            KeySpec("س", "س", "«", "«"),
            KeySpec("ي", "ي", "»", "»", listOf("ئ", "ى")),
            KeySpec("ب", "ب", "(", "(", listOf("پ")),
            KeySpec("ل", "ل", ")", ")", listOf("ﻻ", "لا", "ﻷ", "لأ", "ﻹ", "لإ", "ﻵ", "لآ")),
            KeySpec("ا", "ا", "أ", "أ", listOf("آ", "ء", "أ", "إ", "ٱ")),
            KeySpec("ت", "ت", "إ", "إ", listOf("ة")),
            KeySpec("ن", "ن", "آ", "آ"),
            KeySpec("م", "م", "؛", "؛"),
            KeySpec("ك", "ك", ":", ":", listOf("گ", "ک")),
            KeySpec("ط", "ط", "ظ", "ظ")
        ),
        listOf(
            KeySpec("ذ", "ذ", "ذ", "ذ"),
            KeySpec("ء", "ء", "ء", "ء"),
            KeySpec("ؤ", "ؤ", "ؤ", "ؤ"),
            KeySpec("ر", "ر", "ر", "ر"),
            KeySpec("ى", "ى", "ى", "ى", listOf("ئ")),
            KeySpec("ة", "ة", "ة", "ة"),
            KeySpec("و", "و", "و", "و", listOf("ؤ")),
            KeySpec("ز", "ز", "ز", "ز", listOf("ژ")),
            KeySpec("ظ", "ظ", "ظ", "ظ"),
            KeySpec("د", "د", "د", "د"),
            KeySpec("⌫", "", "⌫", "", weight = 1.35f, kind = KeyKind.BACKSPACE)
        )
    )

    private val englishRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("q", "q", "Q", "Q", listOf("1")),
            KeySpec("w", "w", "W", "W", listOf("2")),
            KeySpec("e", "e", "E", "E", listOf("3", "é", "è")),
            KeySpec("r", "r", "R", "R", listOf("4")),
            KeySpec("t", "t", "T", "T", listOf("5")),
            KeySpec("y", "y", "Y", "Y", listOf("6")),
            KeySpec("u", "u", "U", "U", listOf("7", "ü", "ú")),
            KeySpec("i", "i", "I", "I", listOf("8", "í", "î")),
            KeySpec("o", "o", "O", "O", listOf("9", "ö", "ó")),
            KeySpec("p", "p", "P", "P", listOf("0"))
        ),
        listOf(
            KeySpec("a", "a", "A", "A", listOf("@", "á", "à")),
            KeySpec("s", "s", "S", "S", listOf("ş", "ß")),
            KeySpec("d", "d", "D", "D"),
            KeySpec("f", "f", "F", "F"),
            KeySpec("g", "g", "G", "G", listOf("ğ")),
            KeySpec("h", "h", "H", "H"),
            KeySpec("j", "j", "J", "J"),
            KeySpec("k", "k", "K", "K"),
            KeySpec("l", "l", "L", "L")
        ),
        listOf(
            KeySpec("⇧", "", "⇧", "", weight = 1.45f, kind = KeyKind.SHIFT),
            KeySpec("z", "z", "Z", "Z"),
            KeySpec("x", "x", "X", "X"),
            KeySpec("c", "c", "C", "C", listOf("ç")),
            KeySpec("v", "v", "V", "V"),
            KeySpec("b", "b", "B", "B"),
            KeySpec("n", "n", "N", "N", listOf("ñ")),
            KeySpec("m", "m", "M", "M"),
            KeySpec("⌫", "", "⌫", "", weight = 1.45f, kind = KeyKind.BACKSPACE)
        )
    )

    private val frenchRows: List<List<KeySpec>> = listOf(
        listOf(
            KeySpec("a", "a", "A", "A", listOf("à", "â")),
            KeySpec("z", "z", "Z", "Z"),
            KeySpec("e", "e", "E", "E", listOf("é", "è", "ê")),
            KeySpec("r", "r", "R", "R"),
            KeySpec("t", "t", "T", "T"),
            KeySpec("y", "y", "Y", "Y"),
            KeySpec("u", "u", "U", "U", listOf("ù", "û")),
            KeySpec("i", "i", "I", "I", listOf("î", "ï")),
            KeySpec("o", "o", "O", "O", listOf("ô")),
            KeySpec("p", "p", "P", "P")
        ),
        listOf(
            KeySpec("q", "q", "Q", "Q"),
            KeySpec("s", "s", "S", "S"),
            KeySpec("d", "d", "D", "D"),
            KeySpec("f", "f", "F", "F"),
            KeySpec("g", "g", "G", "G"),
            KeySpec("h", "h", "H", "H"),
            KeySpec("j", "j", "J", "J"),
            KeySpec("k", "k", "K", "K"),
            KeySpec("l", "l", "L", "L"),
            KeySpec("m", "m", "M", "M")
        ),
        listOf(
            KeySpec("⇧", "", "⇧", "", weight = 1.45f, kind = KeyKind.SHIFT),
            KeySpec("w", "w", "W", "W"),
            KeySpec("x", "x", "X", "X"),
            KeySpec("c", "c", "C", "C", listOf("ç")),
            KeySpec("v", "v", "V", "V"),
            KeySpec("b", "b", "B", "B"),
            KeySpec("n", "n", "N", "N"),
            KeySpec("'", "'", "'", "'"),
            KeySpec("⌫", "", "⌫", "", weight = 1.45f, kind = KeyKind.BACKSPACE)
        )
    )

    private val spanishRows: List<List<KeySpec>> = listOf(
        englishRows[0],
        englishRows[1] + listOf(KeySpec("ñ", "ñ", "Ñ", "Ñ")),
        englishRows[2]
    )

    private val turkishRows: List<List<KeySpec>> = listOf(
        englishRows[0] + listOf(KeySpec("ğ", "ğ", "Ğ", "Ğ"), KeySpec("ü", "ü", "Ü", "Ü")),
        englishRows[1] + listOf(KeySpec("ş", "ş", "Ş", "Ş"), KeySpec("i", "i", "İ", "İ")),
        listOf(
            KeySpec("⇧", "", "⇧", "", weight = 1.35f, kind = KeyKind.SHIFT),
            KeySpec("z", "z", "Z", "Z"),
            KeySpec("x", "x", "X", "X"),
            KeySpec("c", "c", "C", "C"),
            KeySpec("v", "v", "V", "V"),
            KeySpec("b", "b", "B", "B"),
            KeySpec("n", "n", "N", "N"),
            KeySpec("m", "m", "M", "M"),
            KeySpec("ö", "ö", "Ö", "Ö"),
            KeySpec("ç", "ç", "Ç", "Ç"),
            KeySpec("⌫", "", "⌫", "", weight = 1.35f, kind = KeyKind.BACKSPACE)
        )
    )

    val symbols1Rows: List<List<KeySpec>> = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map { KeySpec(it) },
        listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/").map { KeySpec(it) },
        listOf(
            KeySpec("=\\<", "", "=\\<", "", weight = 1.4f, kind = KeyKind.SYMBOLS_TOGGLE),
            KeySpec("*"), KeySpec("\""), KeySpec("'"), KeySpec(":"), KeySpec(";"), KeySpec("!"), KeySpec("?"),
            KeySpec("⌫", "", "⌫", "", weight = 1.4f, kind = KeyKind.BACKSPACE)
        )
    )

    val symbols2Rows: List<List<KeySpec>> = listOf(
        listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆").map { KeySpec(it) },
        listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\").map { KeySpec(it) },
        listOf(
            KeySpec("?123", "", "?123", "", weight = 1.4f, kind = KeyKind.SYMBOLS_TOGGLE),
            KeySpec("%"), KeySpec("©"), KeySpec("®"), KeySpec("™"), KeySpec("✓"), KeySpec("["), KeySpec("]"),
            KeySpec("⌫", "", "⌫", "", weight = 1.4f, kind = KeyKind.BACKSPACE)
        )
    )

    fun bottomRow(language: KeyboardLanguage, layer: KeyboardLayer): List<KeySpec> {
        val comma = if (language == KeyboardLanguage.ARABIC) "،" else ","
        val question = if (language == KeyboardLanguage.ARABIC) "؟" else "?"
        val toggleLabel = if (layer == KeyboardLayer.LETTERS) {
            if (language == KeyboardLanguage.ARABIC) "٣٢١؟" else "?123"
        } else if (language == KeyboardLanguage.ARABIC) "أ‌ب‌ج" else "ABC"
        val dotLongPress = if (language == KeyboardLanguage.ARABIC) {
            listOf("ٕ", "ٔ", "ْ", "ٍ", "ٌ", "ً", "ّ", "ٖ", "ٰ", "ٓ", "ِ", "ُ", "َ", "ـ", question)
        } else listOf(question, "!", "…")
        return listOf(
            KeySpec(toggleLabel, "", toggleLabel, "", weight = 1.35f, kind = KeyKind.SYMBOLS_TOGGLE),
            KeySpec(comma, comma, comma, comma, listOf("؛", ":"), weight = 0.9f),
            KeySpec("🌐", "", "🌐", "", weight = 1.0f, kind = KeyKind.LANGUAGE_SWITCH),
            KeySpec(language.spaceLabel, " ", language.spaceLabel, " ", weight = 4.2f, kind = KeyKind.SPACE),
            KeySpec(".", ".", ".", ".", dotLongPress, weight = 0.9f),
            KeySpec("→", "\n", "→", "\n", weight = 1.55f, kind = KeyKind.ENTER)
        )
    }

    private fun numpadRows(language: KeyboardLanguage, numberRowStyle: NumberRowStyle): List<List<KeySpec>> {
        val arabic = numberRowStyle == NumberRowStyle.ARABIC_EASTERN
        fun d(western: String, eastern: String): KeySpec =
            if (arabic) KeySpec(eastern, eastern, eastern, eastern, listOf(western))
            else KeySpec(western, western, western, western, listOf(eastern))

        val lettersLabel = if (language == KeyboardLanguage.ARABIC) "أ ب ج" else "ABC"
        return listOf(
            listOf(d("1", "١"), d("2", "٢"), d("3", "٣"), KeySpec("-", "-", "-", "-")),
            listOf(d("4", "٤"), d("5", "٥"), d("6", "٦"), KeySpec("+", "+", "+", "+", listOf("*", "/"))),
            listOf(d("7", "٧"), d("8", "٨"), d("9", "٩"), KeySpec("⌫", "", "⌫", "", kind = KeyKind.BACKSPACE)),
            listOf(
                KeySpec(lettersLabel, "", lettersLabel, "", weight = 1.15f, kind = KeyKind.SYMBOLS_TOGGLE),
                KeySpec(".", ".", ".", ".", listOf(","), weight = 0.85f),
                d("0", "٠").copy(weight = 1.15f),
                KeySpec("␣", " ", "␣", " ", weight = 0.85f, kind = KeyKind.SPACE),
                KeySpec("↵", "\n", "↵", "\n", weight = 1.0f, kind = KeyKind.ENTER)
            )
        )
    }

    fun getRowsFor(
        language: KeyboardLanguage,
        layer: KeyboardLayer,
        showNumberRow: Boolean,
        numberRowStyle: NumberRowStyle = NumberRowStyle.WESTERN
    ): List<List<KeySpec>> {
        if (layer == KeyboardLayer.NUMPAD) {
            return numpadRows(language, numberRowStyle)
        }
        val core = when (layer) {
            KeyboardLayer.SYMBOLS_1 -> symbols1Rows
            KeyboardLayer.SYMBOLS_2 -> symbols2Rows
            KeyboardLayer.NUMPAD -> emptyList()
            KeyboardLayer.LETTERS -> {
                val base = when (language) {
                    KeyboardLanguage.ARABIC -> arabicRowsNormal
                    KeyboardLanguage.ENGLISH -> englishRows
                    KeyboardLanguage.FRENCH -> frenchRows
                    KeyboardLanguage.SPANISH -> spanishRows
                    KeyboardLanguage.TURKISH -> turkishRows
                }
                if (showNumberRow) {
                    val activeNumbers = when (numberRowStyle) {
                        NumberRowStyle.ARABIC_EASTERN -> numberRowArabic
                        NumberRowStyle.WESTERN -> numberRow
                    }
                    listOf(activeNumbers) + base
                } else {
                    base
                }
            }
        }
        return core + listOf(bottomRow(language, layer))
    }

    val emojiCategories: List<Pair<String, List<String>>> = listOf(
        "😊" to listOf("😀", "😃", "😄", "😁", "😊", "😉", "😍", "🥰", "😘", "😎", "🤩", "🤔", "🫡", "🤗", "🙏", "👍", "👏", "🔥", "✨", "🎉", "💯", "✅", "❤️", "💙"),
        "💼" to listOf("📝", "📌", "📅", "⏰", "📧", "📞", "💬", "🌐", "🚀", "💡", "⚡", "🔍", "📊", "📁", "🔗", "🛡️", "⚙️", "💻", "📱", "🎙️", "📸", "🏆", "⭐", "🌟")
    )
}
