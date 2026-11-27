package com.example.ime

import android.content.Context

/** A Unicode emoji plus its official searchable English name. */
data class KeyboardEmojiEntry(
    val value: String,
    val name: String
)

/** A browsable emoji group shown by the keyboard category strip. */
data class KeyboardEmojiCategory(
    val id: String,
    val unicodeGroup: String,
    val icon: String,
    val nameAr: String,
    val nameEn: String,
    val emojis: List<KeyboardEmojiEntry>
)

/**
 * Loads the official Unicode emoji-test.txt catalog bundled in app assets.
 * The asset is Unicode Emoji 17.0 and retains its upstream copyright and terms header.
 */
object EmojiCatalog {
    private data class Definition(
        val id: String,
        val unicodeGroup: String,
        val icon: String,
        val nameAr: String,
        val nameEn: String
    )

    private val definitions = listOf(
        Definition("smileys", "Smileys & Emotion", "😀", "الوجوه", "Smileys"),
        Definition("people", "People & Body", "👤", "الأشخاص", "People"),
        Definition("animals", "Animals & Nature", "🐻", "الحيوانات", "Animals"),
        Definition("food", "Food & Drink", "🍎", "الطعام", "Food"),
        Definition("travel", "Travel & Places", "🚗", "السفر", "Travel"),
        Definition("activities", "Activities", "⚽", "الأنشطة", "Activities"),
        Definition("objects", "Objects", "💡", "الأشياء", "Objects"),
        Definition("symbols", "Symbols", "🔣", "الرموز", "Symbols"),
        Definition("flags", "Flags", "🏳️", "الأعلام", "Flags")
    )

    @Volatile
    private var cached: List<KeyboardEmojiCategory>? = null

    fun load(context: Context): List<KeyboardEmojiCategory> {
        cached?.let { return it }
        return synchronized(this) {
            cached ?: parse(context.applicationContext).also { cached = it }
        }
    }

    private fun parse(context: Context): List<KeyboardEmojiCategory> {
        val entriesByGroup = definitions.associate { it.unicodeGroup to mutableListOf<KeyboardEmojiEntry>() }
            .toMutableMap()
        var currentGroup = ""

        context.assets.open("emoji-test.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach lineLoop@{ line ->
                if (line.startsWith("# group:")) {
                    currentGroup = line.substringAfter(':').trim()
                    return@lineLoop
                }
                if (line.isBlank() || line.startsWith('#') || ';' !in line) return@lineLoop

                val status = line.substringAfter(';').substringBefore('#').trim()
                if (status != "fully-qualified" && status != "component") return@lineLoop

                val mappedGroup = if (currentGroup == "Component") "People & Body" else currentGroup
                val destination = entriesByGroup[mappedGroup] ?: return@lineLoop
                val codePoints = line.substringBefore(';')
                    .trim()
                    .split(Regex("\\s+"))
                    .mapNotNull { it.toIntOrNull(16) }
                    .toIntArray()
                if (codePoints.isEmpty()) return@lineLoop

                val commentParts = line.substringAfter('#', "")
                    .trim()
                    .split(Regex("\\s+"), limit = 3)
                val name = commentParts.getOrNull(2).orEmpty()
                destination += KeyboardEmojiEntry(
                    value = String(codePoints, 0, codePoints.size),
                    name = name
                )
            }
        }

        return definitions.map { definition ->
            KeyboardEmojiCategory(
                id = definition.id,
                unicodeGroup = definition.unicodeGroup,
                icon = definition.icon,
                nameAr = definition.nameAr,
                nameEn = definition.nameEn,
                emojis = entriesByGroup[definition.unicodeGroup].orEmpty()
            )
        }
    }
}
