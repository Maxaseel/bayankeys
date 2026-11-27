package com.example.ime

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** A Unicode emoji with searchable English and Arabic names and keywords. */
data class KeyboardEmojiEntry(
    val value: String,
    val name: String,
    val nameAr: String = "",
    val searchTermsAr: String = ""
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
 * Loads the Unicode Emoji 17.0 catalog and Arabic CLDR annotations bundled in app assets.
 * CLDR annotations provide the Arabic display names and searchable keywords.
 * CLDR data is Copyright © 2015-2024 Unicode, Inc.; see CLDR-UNICODE-LICENSE.txt.
 */
object EmojiCatalog {
    private data class Definition(
        val id: String,
        val unicodeGroup: String,
        val icon: String,
        val nameAr: String,
        val nameEn: String
    )

    private data class ArabicAnnotation(
        val name: String,
        val searchTerms: String
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
        val arabicAnnotations = loadArabicAnnotations(context)
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
                val value = String(codePoints, 0, codePoints.size)
                val annotation = findArabicAnnotation(value, arabicAnnotations)
                val categoryNameAr = definitions.firstOrNull { it.unicodeGroup == mappedGroup }?.nameAr.orEmpty()
                destination += KeyboardEmojiEntry(
                    value = value,
                    name = name,
                    nameAr = annotation?.name.orEmpty().ifBlank { categoryNameAr },
                    searchTermsAr = listOf(annotation?.searchTerms.orEmpty(), categoryNameAr)
                        .filter { it.isNotBlank() }
                        .distinct()
                        .joinToString(" ")
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

    private fun loadArabicAnnotations(context: Context): Map<String, ArabicAnnotation> {
        val root = context.assets.open("cldr-annotations-ar.json")
            .bufferedReader(Charsets.UTF_8)
            .use { JSONObject(it.readText()) }
        val annotations = root.getJSONObject("annotations").getJSONObject("annotations")
        return buildMap {
            val keys = annotations.keys()
            while (keys.hasNext()) {
                val sequence = keys.next()
                val entry = annotations.optJSONObject(sequence) ?: continue
                val names = entry.optJSONArray("tts").toStringValues()
                val terms = entry.optJSONArray("default").toStringValues()
                val name = names.firstOrNull().orEmpty()
                val searchableTerms = (terms + names).distinct().joinToString(" ")
                put(sequence, ArabicAnnotation(name, searchableTerms))
            }
        }
    }

    private fun JSONArray?.toStringValues(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optString(index).takeIf { it.isNotBlank() } }
    }

    private fun findArabicAnnotation(
        emoji: String,
        annotations: Map<String, ArabicAnnotation>
    ): ArabicAnnotation? {
        annotations[emoji]?.let { return it }
        if (emoji.contains("🫱") && emoji.contains("🫲")) {
            annotations["🤝"]?.let { return it }
        }
        val withoutVariationSelectors = removeCodePoints(emoji) { it == 0xFE0E || it == 0xFE0F }
        annotations[withoutVariationSelectors]?.let { return it }
        val baseSequence = removeCodePoints(withoutVariationSelectors) { it in 0x1F3FB..0x1F3FF }
        annotations[baseSequence]?.let { return it }
        val simplifiedSequence = removeCodePoints(baseSequence) {
            it in 0x1F9B0..0x1F9B3 || it == 0x2640 || it == 0x2642
        }.trim('\u200D')
        return annotations[simplifiedSequence]
    }

    private fun removeCodePoints(value: String, shouldRemove: (Int) -> Boolean): String = buildString {
        var index = 0
        while (index < value.length) {
            val codePoint = value.codePointAt(index)
            if (!shouldRemove(codePoint)) appendCodePoint(codePoint)
            index += Character.charCount(codePoint)
        }
    }
}
