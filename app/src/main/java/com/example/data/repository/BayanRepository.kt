package com.example.data.repository

import com.example.data.local.BayanDao
import com.example.data.local.ClipboardEntity
import com.example.data.local.CustomBotEntity
import com.example.data.local.CustomProviderEntity
import com.example.data.local.LearnedWordEntity
import com.example.data.local.PasswordEntryEntity
import com.example.data.local.SeedDictionary
import com.example.data.local.SettingsRepository
import com.example.data.local.WordBigramEntity
import com.example.data.model.BotIconOption
import com.example.data.model.BotKnowledgeSource
import com.example.data.model.CustomBotItem
import com.example.data.model.KeyboardLanguage
import com.example.data.model.PasswordEntryItem
import com.example.data.model.PasswordServiceBrand
import com.example.util.VaultCryptoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.min

data class SuggestionCandidate(
    val text: String,
    val isSpellCorrection: Boolean = false,
    val targetLanguageSwitch: KeyboardLanguage? = null,
    val replacesComposing: Boolean = true
)

class BayanRepository(
    private val dao: BayanDao,
    private val settings: SettingsRepository
) {
    val clipboardItems: Flow<List<ClipboardEntity>> = dao.observeClipboardItems()
    val learnedWords: Flow<List<LearnedWordEntity>> = dao.observeLearnedWords()
    val learnedWordsCount: Flow<Int> = dao.observeLearnedWordsCount()
    val customProviders: Flow<List<CustomProviderEntity>> = dao.observeCustomProviders()
    val customBots: Flow<List<CustomBotItem>> = dao.observeCustomBots().map { rows ->
        rows.map { entity ->
            CustomBotItem(
                id = entity.id,
                name = entity.name,
                prompt = entity.prompt,
                iconOption = BotIconOption.fromId(entity.iconId),
                accentArgb = entity.accentArgb,
                knowledgeSources = BotKnowledgeSource.listFromJson(entity.knowledgeSourcesJson),
                isPinnedToToolbar = entity.isPinnedToToolbar,
                updatedAt = entity.updatedAt
            )
        }
    }
    val passwordEntries: Flow<List<PasswordEntryItem>> = dao.observePasswordEntries().map { rows ->
        rows.map { entity ->
            PasswordEntryItem(
                id = entity.id,
                siteName = entity.siteName,
                siteUrl = entity.siteUrl,
                username = entity.username,
                password = VaultCryptoHelper.decrypt(entity.encryptedPassword),
                brand = PasswordServiceBrand.fromId(entity.brandId),
                localIconPath = entity.localIconPath,
                updatedAt = entity.updatedAt
            )
        }
    }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        val isEmptyDb = dao.getTopWordsForLanguage(KeyboardLanguage.ARABIC.code, 1).isEmpty()
        if (!isEmptyDb && settings.isDictionarySeeded()) return@withContext
        val now = System.currentTimeMillis()
        val entities = buildList {
            SeedDictionary.arabicCoreWords.forEachIndexed { idx, w ->
                add(LearnedWordEntity(w, KeyboardLanguage.ARABIC.code, frequency = 250 - idx.coerceAtMost(200), lastUsedAt = now))
            }
            SeedDictionary.englishCoreWords.forEachIndexed { idx, w ->
                add(LearnedWordEntity(w, KeyboardLanguage.ENGLISH.code, frequency = 250 - idx.coerceAtMost(200), lastUsedAt = now))
            }
            SeedDictionary.frenchCoreWords.forEachIndexed { idx, w ->
                add(LearnedWordEntity(w, KeyboardLanguage.FRENCH.code, frequency = 120 - idx.coerceAtMost(90), lastUsedAt = now))
            }
            SeedDictionary.spanishCoreWords.forEachIndexed { idx, w ->
                add(LearnedWordEntity(w, KeyboardLanguage.SPANISH.code, frequency = 120 - idx.coerceAtMost(90), lastUsedAt = now))
            }
            SeedDictionary.turkishCoreWords.forEachIndexed { idx, w ->
                add(LearnedWordEntity(w, KeyboardLanguage.TURKISH.code, frequency = 120 - idx.coerceAtMost(90), lastUsedAt = now))
            }
        }
        dao.insertWordsIgnore(entities)
        SeedDictionary.initialBigrams.forEach { (prev, next, lang) ->
            dao.insertBigram(WordBigramEntity(prev, next, lang, frequency = 25, lastUsedAt = now))
        }
        // Add helpful starter pinned clips
        if (dao.getClipboardItemsOnce(1).isEmpty()) {
            dao.insertClipboardItem(
                ClipboardEntity(
                    text = "السلام عليكم ورحمة الله وبركاته",
                    timestamp = now,
                    isPinned = true
                )
            )
            dao.insertClipboardItem(
                ClipboardEntity(
                    text = "Thank you for reaching out! I will get back to you shortly.",
                    timestamp = now - 1000,
                    isPinned = true
                )
            )
        }
        settings.markDictionarySeeded()
    }

    // --- Clipboard ---
    suspend fun captureClipboardText(rawText: String) = withContext(Dispatchers.IO) {
        val text = rawText.trim()
        if (text.isBlank()) return@withContext
        val existing = dao.findClipboardByText(text)
        if (existing != null) {
            dao.touchClipboardTimestamp(existing.id)
        } else {
            dao.insertClipboardItem(ClipboardEntity(text = text, isPinned = false))
        }
    }

    suspend fun addManualClip(text: String, pinned: Boolean) = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return@withContext
        val existing = dao.findClipboardByText(trimmed)
        if (existing != null) {
            dao.updateClipboardPinned(existing.id, pinned || existing.isPinned)
        } else {
            dao.insertClipboardItem(ClipboardEntity(text = trimmed, isPinned = pinned, pinOrder = if (pinned) System.currentTimeMillis() else 0L))
        }
    }

    suspend fun togglePinClipboard(item: ClipboardEntity) = withContext(Dispatchers.IO) {
        val nextOrder = if (!item.isPinned) System.currentTimeMillis() else 0L
        dao.updateClipboardPinned(item.id, !item.isPinned, nextOrder)
    }

    suspend fun movePinnedClipboard(item: ClipboardEntity, direction: Int) = withContext(Dispatchers.IO) {
        if (!item.isPinned) return@withContext
        val pinned = dao.getClipboardItemsOnce(200).filter { it.isPinned }
        val index = pinned.indexOfFirst { it.id == item.id }
        val target = index + direction
        if (index < 0 || target !in pinned.indices) return@withContext
        val other = pinned[target]
        val currentOrder = item.pinOrder
        dao.updateClipboardPinOrder(item.id, other.pinOrder)
        dao.updateClipboardPinOrder(other.id, currentOrder)
    }

    suspend fun deleteClipboard(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteClipboardItem(id)
    }

    suspend fun clearUnpinnedClipboard() = withContext(Dispatchers.IO) {
        dao.clearUnpinnedClipboard()
    }


    suspend fun exportBackup(): String = withContext(Dispatchers.IO) {
        val words = dao.getAllLearnedWords()
        val wordsJson = JSONArray().apply {
            words.forEach { word ->
                put(JSONObject().apply {
                    put("word", word.word)
                    put("languageCode", word.languageCode)
                    put("frequency", word.frequency)
                    put("lastUsedAt", word.lastUsedAt)
                    put("isUserAdded", word.isUserAdded)
                })
            }
        }
        JSONObject().apply {
            put("backupVersion", 1)
            put("createdAt", System.currentTimeMillis())
            put("learnedWords", wordsJson)
            put("settings", settings.exportSettingsJson())
        }.toString(2)
    }

    suspend fun importBackup(rawJson: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val root = JSONObject(rawJson)
            val wordsJson = root.optJSONArray("learnedWords") ?: JSONArray()
            val restored = buildList {
                for (index in 0 until wordsJson.length()) {
                    val item = wordsJson.optJSONObject(index) ?: continue
                    val word = item.optString("word").trim()
                    val language = item.optString("languageCode").trim()
                    if (word.isNotBlank() && language.isNotBlank()) {
                        add(LearnedWordEntity(word, language, item.optInt("frequency", 1), item.optLong("lastUsedAt", System.currentTimeMillis()), item.optBoolean("isUserAdded", false)))
                    }
                }
            }
            val settingsJson = root.optJSONArray("settings") ?: JSONArray()
            settings.importSettingsJson(settingsJson)
            dao.clearAllLearnedWords()
            if (restored.isNotEmpty()) dao.insertWordsIgnore(restored)
            restored.size
        }
    }

    // --- Continuous Learning ---
    suspend fun learnCommittedWord(
        rawWord: String,
        previousWord: String?,
        activeLanguage: KeyboardLanguage,
        enabledLanguages: List<KeyboardLanguage>
    ) = withContext(Dispatchers.IO) {
        val cleaned = rawWord.trim().trim { it in ".,!?;:\"'()[]{}،؛؟" }
        if (cleaned.length < 2 || cleaned.all { it.isDigit() }) return@withContext
        val detectedLang = detectLanguageOfWord(cleaned, activeLanguage, enabledLanguages)
        val normalized = if (detectedLang == KeyboardLanguage.ARABIC) cleaned else cleaned.lowercase()
        val existing = dao.getWord(normalized, detectedLang.code)
        val now = System.currentTimeMillis()
        dao.insertWord(
            LearnedWordEntity(
                word = normalized,
                languageCode = detectedLang.code,
                frequency = (existing?.frequency ?: 0) + 2,
                lastUsedAt = now,
                isUserAdded = existing?.isUserAdded ?: false
            )
        )
        val prevCleaned = previousWord?.trim()?.trim { it in ".,!?;:\"'()[]{}،؛؟" }
        if (!prevCleaned.isNullOrBlank() && prevCleaned.length >= 2) {
            val prevNorm = if (detectedLang == KeyboardLanguage.ARABIC) prevCleaned else prevCleaned.lowercase()
            val prevBigram = dao.getBigram(prevNorm, normalized, detectedLang.code)
            dao.insertBigram(
                WordBigramEntity(
                    prevWord = prevNorm,
                    nextWord = normalized,
                    languageCode = detectedLang.code,
                    frequency = (prevBigram?.frequency ?: 0) + 1,
                    lastUsedAt = now
                )
            )
        }
    }

    suspend fun addCustomWord(word: String, language: KeyboardLanguage) = withContext(Dispatchers.IO) {
        val cleaned = word.trim()
        if (cleaned.isBlank()) return@withContext
        dao.insertWord(
            LearnedWordEntity(
                word = cleaned,
                languageCode = language.code,
                frequency = 500,
                lastUsedAt = System.currentTimeMillis(),
                isUserAdded = true
            )
        )
    }

    suspend fun deleteLearnedWord(word: String, languageCode: String) = withContext(Dispatchers.IO) {
        dao.deleteLearnedWord(word, languageCode)
    }

    suspend fun resetLearnedDictionary() = withContext(Dispatchers.IO) {
        dao.clearAllLearnedWords()
    }

    // --- Saved Custom AI Providers ---
    suspend fun saveCustomProvider(
        id: Long = 0,
        name: String,
        baseUrl: String,
        apiKey: String,
        selectedModel: String,
        fetchedModels: List<String>
    ): CustomProviderEntity = withContext(Dispatchers.IO) {
        val cleanName = name.trim().ifBlank { "Custom Provider" }
        val cleanUrl = baseUrl.trim()
        val cleanKey = apiKey.trim()
        val cleanModel = selectedModel.trim()
        val entity = CustomProviderEntity(
            id = id,
            name = cleanName,
            baseUrl = cleanUrl,
            apiKey = cleanKey,
            selectedModel = cleanModel,
            fetchedModelsCsv = fetchedModels.joinToString("\n"),
            updatedAt = System.currentTimeMillis()
        )
        val savedId = dao.insertCustomProvider(entity)
        val finalId = if (id > 0L) id else savedId
        entity.copy(id = finalId)
    }

    suspend fun deleteCustomProvider(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteCustomProvider(id)
    }

    // --- Password Manager ---
    suspend fun savePasswordEntry(
        id: Long = 0,
        siteName: String,
        siteUrl: String,
        username: String,
        password: String,
        brand: PasswordServiceBrand? = null,
        localIconPath: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val cleanSite = siteName.trim()
        val cleanUrl = siteUrl.trim()
        val cleanUser = username.trim()
        val resolvedBrand = brand ?: PasswordServiceBrand.detectFrom(cleanSite, cleanUrl)
        val encrypted = VaultCryptoHelper.encrypt(password)
        dao.insertPasswordEntry(
            PasswordEntryEntity(
                id = id,
                siteName = cleanSite.ifBlank { cleanUser },
                siteUrl = cleanUrl,
                username = cleanUser,
                encryptedPassword = encrypted,
                brandId = resolvedBrand.id,
                localIconPath = localIconPath,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deletePasswordEntry(id: Long) = withContext(Dispatchers.IO) {
        dao.deletePasswordEntry(id)
    }

    // --- Custom AI Bots ---
    suspend fun saveCustomBot(
        id: Long = 0,
        name: String,
        prompt: String,
        iconOption: BotIconOption,
        accentArgb: Long = iconOption.accentArgb,
        knowledgeSources: List<BotKnowledgeSource> = emptyList(),
        isPinnedToToolbar: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        val cleanName = name.trim().ifBlank { "مساعد مخصص" }
        val cleanPrompt = prompt.trim()
        dao.insertCustomBot(
            CustomBotEntity(
                id = id,
                name = cleanName,
                prompt = cleanPrompt,
                iconId = iconOption.id,
                accentArgb = accentArgb,
                knowledgeSourcesJson = BotKnowledgeSource.listToJson(knowledgeSources),
                isPinnedToToolbar = isPinnedToToolbar,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleBotPinnedToToolbar(bot: CustomBotItem) = withContext(Dispatchers.IO) {
        val existing = dao.getCustomBotById(bot.id) ?: return@withContext
        dao.insertCustomBot(
            existing.copy(
                isPinnedToToolbar = !existing.isPinnedToToolbar,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteCustomBot(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteCustomBot(id)
    }

    // --- Smart Suggestions + Spell Correction + Cross-Layout Auto-Switch Detection ---
    suspend fun getSuggestions(
        composingWord: String,
        previousWord: String?,
        activeLanguage: KeyboardLanguage,
        enabledLanguages: List<KeyboardLanguage>,
        autoSwitchEnabled: Boolean
    ): List<SuggestionCandidate> = withContext(Dispatchers.IO) {
        val query = composingWord.trim()
        if (query.isEmpty()) {
            // Do not show generic dictionary words before there is typing context.
            // Once a word has been committed, use only that user's learned next-word sequence.
            if (previousWord.isNullOrBlank()) return@withContext emptyList()
            val prevNorm = if (activeLanguage == KeyboardLanguage.ARABIC) previousWord.trim()
            else previousWord.trim().lowercase()
            return@withContext dao.getNextWordPredictions(prevNorm, activeLanguage.code, 4)
                .map { SuggestionCandidate(text = it.nextWord, replacesComposing = false) }
        }

        val results = mutableListOf<SuggestionCandidate>()
        val searchQuery = if (activeLanguage == KeyboardLanguage.ARABIC) query else query.lowercase()

        // Rank words learned after the preceding word ahead of generic prefix matches.
        if (!previousWord.isNullOrBlank()) {
            val prevNorm = if (activeLanguage == KeyboardLanguage.ARABIC) previousWord.trim()
            else previousWord.trim().lowercase()
            dao.getNextWordPredictions(prevNorm, activeLanguage.code, 8)
                .asSequence()
                .map { it.nextWord }
                .filter { it.startsWith(searchQuery, ignoreCase = activeLanguage != KeyboardLanguage.ARABIC) }
                .distinct()
                .forEach { results.add(SuggestionCandidate(text = it, replacesComposing = true)) }
        }

        // 1. Exact prefix matches in active language
        val prefixMatches = dao.searchWordsByPrefix(searchQuery, activeLanguage.code, 6)
        prefixMatches.forEach { entity ->
            if (results.none { it.text.equals(entity.word, ignoreCase = true) }) {
                results.add(SuggestionCandidate(text = entity.word, replacesComposing = true))
            }
        }

        // 2. Cross-layout conversion hint (e.g., user typed on AR keys while meaning EN, or vice versa)
        if (autoSwitchEnabled && query.length >= 3 && prefixMatches.isEmpty()) {
            if (activeLanguage == KeyboardLanguage.ARABIC && enabledLanguages.contains(KeyboardLanguage.ENGLISH)) {
                val mappedToEn = mapArabicKeysToEnglish(query)
                val enMatches = dao.searchWordsByPrefix(mappedToEn.lowercase(), KeyboardLanguage.ENGLISH.code, 2)
                enMatches.firstOrNull()?.let { match ->
                    results.add(
                        0,
                        SuggestionCandidate(
                            text = match.word,
                            isSpellCorrection = true,
                            targetLanguageSwitch = KeyboardLanguage.ENGLISH,
                            replacesComposing = true
                        )
                    )
                }
            } else if (activeLanguage != KeyboardLanguage.ARABIC && enabledLanguages.contains(KeyboardLanguage.ARABIC)) {
                val mappedToAr = mapEnglishKeysToArabic(query)
                val arMatches = dao.searchWordsByPrefix(mappedToAr, KeyboardLanguage.ARABIC.code, 2)
                arMatches.firstOrNull()?.let { match ->
                    results.add(
                        0,
                        SuggestionCandidate(
                            text = match.word,
                            isSpellCorrection = true,
                            targetLanguageSwitch = KeyboardLanguage.ARABIC,
                            replacesComposing = true
                        )
                    )
                }
            }
        }

        // 3. Local fuzzy spelling correction (Levenshtein distance <= 2)
        if (results.size < 3 && query.length >= 3) {
            val candidates = dao.getTopWordsForLanguage(activeLanguage.code, 200)
            val fuzzy = candidates
                .asSequence()
                .filter { abs(it.word.length - searchQuery.length) <= 2 }
                .map { it to levenshtein(searchQuery, if (activeLanguage == KeyboardLanguage.ARABIC) it.word else it.word.lowercase()) }
                .filter { (_, dist) -> dist in 1..2 }
                .sortedWith(compareBy<Pair<LearnedWordEntity, Int>> { it.second }.thenByDescending { it.first.frequency })
                .take(3 - results.size)
                .toList()
            fuzzy.forEach { (entity, _) ->
                if (results.none { it.text.equals(entity.word, ignoreCase = true) }) {
                    results.add(
                        SuggestionCandidate(
                            text = entity.word,
                            isSpellCorrection = true,
                            replacesComposing = true
                        )
                    )
                }
            }
        }

        // 4. Cross-language direct prefix matches (when typing an English word while on another Latin layout, etc.)
        if (results.size < 3) {
            val cross = dao.searchWordsAnyLanguage(searchQuery, 4)
            cross.forEach { entity ->
                val targetLang = KeyboardLanguage.fromCode(entity.languageCode)
                if (enabledLanguages.contains(targetLang) && results.none { it.text.equals(entity.word, ignoreCase = true) }) {
                    results.add(
                        SuggestionCandidate(
                            text = entity.word,
                            targetLanguageSwitch = if (targetLang != activeLanguage && autoSwitchEnabled) targetLang else null,
                            replacesComposing = true
                        )
                    )
                }
            }
        }

        results.take(4)
    }

    fun detectLanguageOfText(
        text: String,
        current: KeyboardLanguage,
        enabled: List<KeyboardLanguage>
    ): KeyboardLanguage {
        val arabicCount = text.count { it in '\u0600'..'\u06FF' }
        val latinCount = text.count { it in 'a'..'z' || it in 'A'..'Z' }
        return when {
            arabicCount > latinCount && enabled.contains(KeyboardLanguage.ARABIC) -> KeyboardLanguage.ARABIC
            latinCount > arabicCount -> {
                if (current != KeyboardLanguage.ARABIC && enabled.contains(current)) current
                else enabled.firstOrNull { !it.isRtl } ?: current
            }
            else -> current
        }
    }

    private fun detectLanguageOfWord(
        word: String,
        activeLanguage: KeyboardLanguage,
        enabledLanguages: List<KeyboardLanguage>
    ): KeyboardLanguage {
        val hasArabic = word.any { it in '\u0600'..'\u06FF' }
        if (hasArabic && enabledLanguages.contains(KeyboardLanguage.ARABIC)) {
            return KeyboardLanguage.ARABIC
        }
        val hasLatin = word.any { it in 'a'..'z' || it in 'A'..'Z' }
        if (hasLatin && activeLanguage.isRtl) {
            return enabledLanguages.firstOrNull { !it.isRtl } ?: KeyboardLanguage.ENGLISH
        }
        return activeLanguage
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val temp = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = min(min(dp[j] + 1, dp[j - 1] + 1), prev + cost)
                prev = temp
            }
        }
        return dp[b.length]
    }

    private fun mapArabicKeysToEnglish(text: String): String {
        val map = mapOf(
            'ض' to 'q', 'ص' to 'w', 'ث' to 'e', 'ق' to 'r', 'ف' to 't', 'غ' to 'y', 'ع' to 'u', 'ه' to 'i', 'خ' to 'o', 'ح' to 'p',
            'ش' to 'a', 'س' to 's', 'ي' to 'd', 'ب' to 'f', 'ل' to 'g', 'ا' to 'h', 'ت' to 'j', 'ن' to 'k', 'م' to 'l',
            'ئ' to 'z', 'ء' to 'x', 'ؤ' to 'c', 'ر' to 'v', 'ى' to 'n', 'ة' to 'm', 'و' to ',', 'ز' to '.'
        )
        return text.map { map[it] ?: it }.joinToString("")
    }

    private fun mapEnglishKeysToArabic(text: String): String {
        val map = mapOf(
            'q' to 'ض', 'w' to 'ص', 'e' to 'ث', 'r' to 'ق', 't' to 'ف', 'y' to 'غ', 'u' to 'ع', 'i' to 'ه', 'o' to 'خ', 'p' to 'ح',
            'a' to 'ش', 's' to 'س', 'd' to 'ي', 'f' to 'ب', 'g' to 'ل', 'h' to 'ا', 'j' to 'ت', 'k' to 'ن', 'l' to 'م',
            'z' to 'ئ', 'x' to 'ء', 'c' to 'ؤ', 'v' to 'ر', 'b' to 'ل', 'n' to 'ى', 'm' to 'ة'
        )
        return text.lowercase().map { map[it] ?: it }.joinToString("")
    }
}
