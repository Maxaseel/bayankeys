package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AiProviderConfig
import com.example.data.model.AiProviderType
import com.example.data.model.GradualPasteConfig
import com.example.data.model.GradualPasteMode
import com.example.data.model.KeyButtonShape
import com.example.data.model.KeyboardFontStyle
import com.example.data.model.KeyboardLanguage
import com.example.data.model.KeyboardSizeConfig
import com.example.data.model.KeyboardThemeConfig
import com.example.data.model.KeyboardThemePreset
import com.example.data.model.KeyboardThemePresets
import com.example.data.model.NumberRowStyle
import com.example.data.model.OneHandedMode
import com.example.data.model.TargetTranslateLanguage
import com.example.data.model.ThemeMode
import com.example.data.model.ToolbarAction
import com.example.data.model.ToolbarVisibilityConfig
import com.example.data.model.isQuickInputTool
import com.example.data.model.TranslationProviderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.bayanDataStore: DataStore<Preferences> by preferencesDataStore(name = "bayan_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val APP_UI_LANG = stringPreferencesKey("app_ui_lang")
        val DICT_SEEDED = booleanPreferencesKey("dict_seeded_v1")

        // Theme
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val PRESET_ID = stringPreferencesKey("preset_id")
        val BG_ARGB = longPreferencesKey("bg_argb")
        val KEY_ARGB = longPreferencesKey("key_argb")
        val SPECIAL_KEY_ARGB = longPreferencesKey("special_key_argb")
        val ACCENT_ARGB = longPreferencesKey("accent_argb")
        val TEXT_ARGB = longPreferencesKey("text_argb")
        val KEY_CORNER_RADIUS = floatPreferencesKey("key_corner_radius")
        val SHOW_KEY_BORDERS = booleanPreferencesKey("show_key_borders")
        val KEY_SHAPE = stringPreferencesKey("key_shape")
        val FONT_STYLE = stringPreferencesKey("font_style")

        // Gradual Paste & Typing
        val GP_ENABLED = booleanPreferencesKey("gp_enabled")
        val GP_MODE = stringPreferencesKey("gp_mode")
        val GP_CHAR_SPEED = intPreferencesKey("gp_char_speed")
        val GP_WORD_SPEED = intPreferencesKey("gp_word_speed")
        val GP_SPACE_RHYTHM = intPreferencesKey("gp_space_rhythm")
        val GP_ARROW_SPEED = intPreferencesKey("gp_arrow_speed")
        val GP_NATURAL_PULSE = intPreferencesKey("gp_natural_pulse")
        val GP_BREATH_INTENSITY = intPreferencesKey("gp_breath_intensity")
        val GP_CHARS_BEFORE_BREATH = intPreferencesKey("gp_chars_before_breath")
        val GP_BREATH_DURATION = intPreferencesKey("gp_breath_duration")
        val GP_START_DELAY = intPreferencesKey("gp_start_delay")

        // Size & Behavior
        val HEIGHT_SCALE = floatPreferencesKey("height_scale")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val TOOLBAR_ICON_SCALE = floatPreferencesKey("toolbar_icon_scale")
        val QUICK_TOOLS_SECOND_ROW = booleanPreferencesKey("quick_tools_second_row")
        val BOTTOM_PADDING = floatPreferencesKey("bottom_padding")
        val SHOW_NUMBER_ROW = booleanPreferencesKey("show_number_row")
        val NUMBER_ROW_STYLE = stringPreferencesKey("number_row_style")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SOUND_FEEDBACK = booleanPreferencesKey("sound_feedback")
        val AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize")
        val AUTO_SWITCH_LANG = booleanPreferencesKey("auto_switch_lang")
        val LEARNING_ENABLED = booleanPreferencesKey("learning_enabled")
        val ONE_HANDED_MODE = stringPreferencesKey("one_handed_mode")
        val SPLIT_KEYBOARD = booleanPreferencesKey("split_keyboard")
        val INCOGNITO_MODE = booleanPreferencesKey("incognito_mode")
        val AUTO_CORRECT_ENABLED = booleanPreferencesKey("auto_correct_enabled")
        val SHOW_SUGGESTIONS = booleanPreferencesKey("show_suggestions")

        // Languages
        val ENABLED_LANGUAGES = stringPreferencesKey("enabled_languages")
        val ACTIVE_LANGUAGE = stringPreferencesKey("active_language")

        // AI & Translation Providers
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val ACTIVE_CUSTOM_ID = longPreferencesKey("active_custom_provider_id")
        val ACTIVE_CUSTOM_NAME = stringPreferencesKey("active_custom_provider_name")
        val AI_CUSTOM_BASE_URL = stringPreferencesKey("ai_custom_base_url")
        val TRANSLATION_PROVIDER = stringPreferencesKey("translation_provider")
        val TRANSLATION_API_KEY = stringPreferencesKey("translation_api_key")
        val TRANSLATION_ENDPOINT = stringPreferencesKey("translation_endpoint")
        val TARGET_TRANSLATE_LANG = stringPreferencesKey("target_translate_lang")

        // Toolbar Icon Visibility & Order
        val TB_ORDER = stringPreferencesKey("tb_order")
        val TB_HIDDEN = stringPreferencesKey("tb_hidden")
        val TB_SHOW_PROOFREAD = booleanPreferencesKey("tb_show_proofread")
        val TB_SHOW_BOTS = booleanPreferencesKey("tb_show_bots")
        val TB_SHOW_CLIPBOARD = booleanPreferencesKey("tb_show_clipboard")
        val TB_SHOW_QUICK_TOOLS = booleanPreferencesKey("tb_show_quick_tools")
        val TB_SHOW_VOICE = booleanPreferencesKey("tb_show_voice")
        val TB_SHOW_PASSWORDS = booleanPreferencesKey("tb_show_passwords")
        val TB_SHOW_TRANSLATE = booleanPreferencesKey("tb_show_translate")
        val TB_SHOW_EMOJI = booleanPreferencesKey("tb_show_emoji")
        val TB_SHOW_SETTINGS = booleanPreferencesKey("tb_show_settings")

        fun aiKeyFor(providerId: String) = stringPreferencesKey("ai_key_$providerId")
        fun aiModelFor(providerId: String) = stringPreferencesKey("ai_model_$providerId")
    }

    val appUiLanguageFlow: Flow<String> = context.bayanDataStore.data.map { prefs ->
        prefs[Keys.APP_UI_LANG] ?: "ar"
    }

    val appThemeModeFlow: Flow<ThemeMode> = context.bayanDataStore.data.map { prefs ->
        prefs[Keys.APP_THEME_MODE]?.let { id ->
            ThemeMode.entries.firstOrNull { it.id == id }
        } ?: ThemeMode.SYSTEM
    }

    val themeConfigFlow: Flow<KeyboardThemeConfig> = context.bayanDataStore.data.map { prefs ->
        val defaultPreset = KeyboardThemePresets.midnightEmerald
        KeyboardThemeConfig(
            themeMode = ThemeMode.fromId(prefs[Keys.THEME_MODE]),
            presetId = prefs[Keys.PRESET_ID] ?: defaultPreset.id,
            backgroundArgb = prefs[Keys.BG_ARGB] ?: defaultPreset.backgroundArgb,
            keyArgb = prefs[Keys.KEY_ARGB] ?: defaultPreset.keyArgb,
            specialKeyArgb = prefs[Keys.SPECIAL_KEY_ARGB] ?: defaultPreset.specialKeyArgb,
            accentArgb = prefs[Keys.ACCENT_ARGB] ?: defaultPreset.accentArgb,
            textArgb = prefs[Keys.TEXT_ARGB] ?: defaultPreset.textArgb,
            keyCornerRadiusDp = prefs[Keys.KEY_CORNER_RADIUS] ?: 9f,
            showKeyBorders = prefs[Keys.SHOW_KEY_BORDERS] ?: true,
            keyShape = KeyButtonShape.fromId(prefs[Keys.KEY_SHAPE]),
            fontStyle = KeyboardFontStyle.fromId(prefs[Keys.FONT_STYLE])
        )
    }

    val gradualPasteConfigFlow: Flow<GradualPasteConfig> = context.bayanDataStore.data.map { prefs ->
        GradualPasteConfig(
            enabled = prefs[Keys.GP_ENABLED] ?: false,
            mode = GradualPasteMode.fromId(prefs[Keys.GP_MODE]),
            charSpeed = (prefs[Keys.GP_CHAR_SPEED] ?: 25).coerceIn(1, 50),
            wordSpeed = (prefs[Keys.GP_WORD_SPEED] ?: 22).coerceIn(1, 50),
            spaceNewlineRhythm = (prefs[Keys.GP_SPACE_RHYTHM] ?: 20).coerceIn(1, 50),
            arrowNavSpeed = (prefs[Keys.GP_ARROW_SPEED] ?: 25).coerceIn(1, 50),
            naturalRhythmPulse = (prefs[Keys.GP_NATURAL_PULSE] ?: 15).coerceIn(1, 50),
            breathIntensity = (prefs[Keys.GP_BREATH_INTENSITY] ?: 18).coerceIn(1, 50),
            charsBeforeBreath = (prefs[Keys.GP_CHARS_BEFORE_BREATH] ?: 25).coerceIn(1, 50),
            breathPauseDuration = (prefs[Keys.GP_BREATH_DURATION] ?: 20).coerceIn(1, 50),
            startDelay = (prefs[Keys.GP_START_DELAY] ?: 10).coerceIn(1, 50)
        )
    }

    val sizeConfigFlow: Flow<KeyboardSizeConfig> = context.bayanDataStore.data.map { prefs ->
        KeyboardSizeConfig(
            heightScale = prefs[Keys.HEIGHT_SCALE] ?: 1.0f,
            fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
            toolbarIconScale = (prefs[Keys.TOOLBAR_ICON_SCALE] ?: 1.0f).coerceIn(0.75f, 1.5f),
            quickToolsSecondRow = prefs[Keys.QUICK_TOOLS_SECOND_ROW] ?: false,
            bottomPaddingDp = prefs[Keys.BOTTOM_PADDING] ?: 6f,
            showNumberRow = prefs[Keys.SHOW_NUMBER_ROW] ?: true,
            numberRowStyle = NumberRowStyle.fromId(prefs[Keys.NUMBER_ROW_STYLE]),
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            soundFeedback = prefs[Keys.SOUND_FEEDBACK] ?: false,
            autoCapitalize = prefs[Keys.AUTO_CAPITALIZE] ?: true,
            autoSwitchLanguage = prefs[Keys.AUTO_SWITCH_LANG] ?: true,
            learningEnabled = prefs[Keys.LEARNING_ENABLED] ?: true,
            oneHandedMode = OneHandedMode.fromId(prefs[Keys.ONE_HANDED_MODE]),
            splitKeyboard = prefs[Keys.SPLIT_KEYBOARD] ?: false,
            incognitoMode = prefs[Keys.INCOGNITO_MODE] ?: false,
            autoCorrectEnabled = prefs[Keys.AUTO_CORRECT_ENABLED] ?: false,
            showSuggestions = prefs[Keys.SHOW_SUGGESTIONS] ?: true
        )
    }

    val enabledLanguagesFlow: Flow<List<KeyboardLanguage>> = context.bayanDataStore.data.map { prefs ->
        val raw = prefs[Keys.ENABLED_LANGUAGES] ?: "ar,en"
        val parsed = raw.split(",").mapNotNull { code ->
            KeyboardLanguage.entries.firstOrNull { it.code == code.trim() }
        }.distinct()
        parsed.ifEmpty { listOf(KeyboardLanguage.ARABIC, KeyboardLanguage.ENGLISH) }
    }

    val activeLanguageFlow: Flow<KeyboardLanguage> = context.bayanDataStore.data.map { prefs ->
        KeyboardLanguage.fromCode(prefs[Keys.ACTIVE_LANGUAGE])
    }

    val aiConfigFlow: Flow<AiProviderConfig> = context.bayanDataStore.data.map { prefs ->
        val provider = AiProviderType.fromId(prefs[Keys.AI_PROVIDER])
        val customId = prefs[Keys.ACTIVE_CUSTOM_ID]?.takeIf { it > 0L }
        val customName = prefs[Keys.ACTIVE_CUSTOM_NAME]
        val tProvider = TranslationProviderType.fromId(prefs[Keys.TRANSLATION_PROVIDER])
        val savedModel = prefs[Keys.aiModelFor(provider.id)]?.trim().orEmpty()
        val selectedModel = when (provider) {
            AiProviderType.EXAM1 -> AiProviderType.EXAM1.defaultModel
            AiProviderType.GROKCLOUD -> savedModel.takeIf { it.startsWith("grok-") } ?: AiProviderType.GROKCLOUD.defaultModel
            else -> savedModel.ifBlank { provider.defaultModel }
        }
        AiProviderConfig(
            aiProvider = provider,
            activeCustomProviderId = if (provider == AiProviderType.CUSTOM) customId else null,
            activeCustomProviderName = if (provider == AiProviderType.CUSTOM) customName else null,
            aiApiKey = prefs[Keys.aiKeyFor(provider.id)] ?: "",
            aiModel = selectedModel,
            aiCustomBaseUrl = prefs[Keys.AI_CUSTOM_BASE_URL] ?: AiProviderType.CUSTOM.defaultBaseUrl,
            translationProvider = tProvider,
            translationApiKey = prefs[Keys.TRANSLATION_API_KEY] ?: "",
            translationEndpoint = prefs[Keys.TRANSLATION_ENDPOINT] ?: tProvider.defaultEndpoint.ifBlank {
                TranslationProviderType.GOOGLE_TRANSLATE.defaultEndpoint
            },
            defaultTargetLanguage = TargetTranslateLanguage.fromCode(prefs[Keys.TARGET_TRANSLATE_LANG])
        )
    }

    val toolbarVisibilityFlow: Flow<ToolbarVisibilityConfig> = context.bayanDataStore.data.map { prefs ->
        val rawOrder = prefs[Keys.TB_ORDER]
        val parsedOrder = if (rawOrder.isNullOrBlank()) {
            ToolbarAction.defaultOrder() + ToolbarAction.entries.filter { it.isQuickInputTool }
        } else {
            val saved = rawOrder.split(",").mapNotNull { ToolbarAction.fromId(it.trim()) }.distinct()
            val missing = ToolbarAction.entries.filter { it !in saved }
            saved + missing
        }
        val rawHidden = prefs[Keys.TB_HIDDEN]
        val hiddenSet = if (rawHidden != null) {
            rawHidden.split(",").mapNotNull { ToolbarAction.fromId(it.trim()) }.toSet()
        } else {
            buildSet {
                if (prefs[Keys.TB_SHOW_PROOFREAD] == false) add(ToolbarAction.PROOFREAD)
                if (prefs[Keys.TB_SHOW_BOTS] == false) add(ToolbarAction.BOTS)
                if (prefs[Keys.TB_SHOW_CLIPBOARD] == false) add(ToolbarAction.CLIPBOARD)
                if (prefs[Keys.TB_SHOW_QUICK_TOOLS] == false) add(ToolbarAction.QUICK_TOOLS)
                if (prefs[Keys.TB_SHOW_VOICE] == false) add(ToolbarAction.VOICE)
                if (prefs[Keys.TB_SHOW_PASSWORDS] == false) add(ToolbarAction.PASSWORDS)
                if (prefs[Keys.TB_SHOW_TRANSLATE] == false) add(ToolbarAction.TRANSLATE)
                if (prefs[Keys.TB_SHOW_EMOJI] == false) add(ToolbarAction.EMOJI)
                if (prefs[Keys.TB_SHOW_SETTINGS] == false) add(ToolbarAction.SETTINGS)
                ToolbarAction.entries.filter { it.isQuickInputTool && (rawOrder.isNullOrBlank() || !rawOrder.split(",").contains(it.id)) }
                    .forEach(::add)
            }
        }
        ToolbarVisibilityConfig(
            order = parsedOrder,
            hiddenActions = hiddenSet
        )
    }

    suspend fun isDictionarySeeded(): Boolean =
        context.bayanDataStore.data.first()[Keys.DICT_SEEDED] == true

    suspend fun markDictionarySeeded() {
        context.bayanDataStore.edit { it[Keys.DICT_SEEDED] = true }
    }

    suspend fun setAppUiLanguage(lang: String) {
        context.bayanDataStore.edit { it[Keys.APP_UI_LANG] = lang }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.bayanDataStore.edit { it[Keys.THEME_MODE] = mode.id }
    }

    suspend fun setAppThemeMode(mode: ThemeMode) {
        context.bayanDataStore.edit { it[Keys.APP_THEME_MODE] = mode.id }
    }

    suspend fun applyThemePreset(preset: KeyboardThemePreset) {
        context.bayanDataStore.edit { prefs ->
            prefs[Keys.PRESET_ID] = preset.id
            prefs[Keys.THEME_MODE] = if (preset.isDark) ThemeMode.DARK.id else ThemeMode.LIGHT.id
            prefs[Keys.BG_ARGB] = preset.backgroundArgb
            prefs[Keys.KEY_ARGB] = preset.keyArgb
            prefs[Keys.SPECIAL_KEY_ARGB] = preset.specialKeyArgb
            prefs[Keys.ACCENT_ARGB] = preset.accentArgb
            prefs[Keys.TEXT_ARGB] = preset.textArgb
        }
    }

    suspend fun updateCustomThemeColors(
        backgroundArgb: Long? = null,
        keyArgb: Long? = null,
        specialKeyArgb: Long? = null,
        accentArgb: Long? = null,
        textArgb: Long? = null,
        cornerRadiusDp: Float? = null,
        showBorders: Boolean? = null
    ) {
        context.bayanDataStore.edit { prefs ->
            if (backgroundArgb != null || keyArgb != null || specialKeyArgb != null || accentArgb != null || textArgb != null) {
                prefs[Keys.THEME_MODE] = ThemeMode.CUSTOM.id
            }
            backgroundArgb?.let { prefs[Keys.BG_ARGB] = it }
            keyArgb?.let { prefs[Keys.KEY_ARGB] = it }
            specialKeyArgb?.let { prefs[Keys.SPECIAL_KEY_ARGB] = it }
            accentArgb?.let { prefs[Keys.ACCENT_ARGB] = it }
            textArgb?.let { prefs[Keys.TEXT_ARGB] = it }
            cornerRadiusDp?.let { prefs[Keys.KEY_CORNER_RADIUS] = it }
            showBorders?.let { prefs[Keys.SHOW_KEY_BORDERS] = it }
        }
    }

    suspend fun updateSizeConfig(transform: (KeyboardSizeConfig) -> KeyboardSizeConfig) {
        val current = sizeConfigFlow.first()
        val next = transform(current)
        context.bayanDataStore.edit { prefs ->
            prefs[Keys.HEIGHT_SCALE] = next.heightScale.coerceIn(0.78f, 1.35f)
            prefs[Keys.FONT_SCALE] = next.fontScale.coerceIn(0.85f, 1.30f)
            prefs[Keys.TOOLBAR_ICON_SCALE] = next.toolbarIconScale.coerceIn(0.75f, 1.5f)
            prefs[Keys.QUICK_TOOLS_SECOND_ROW] = next.quickToolsSecondRow
            prefs[Keys.BOTTOM_PADDING] = next.bottomPaddingDp.coerceIn(0f, 32f)
            prefs[Keys.SHOW_NUMBER_ROW] = next.showNumberRow
            prefs[Keys.NUMBER_ROW_STYLE] = next.numberRowStyle.id
            prefs[Keys.HAPTIC_FEEDBACK] = next.hapticFeedback
            prefs[Keys.SOUND_FEEDBACK] = next.soundFeedback
            prefs[Keys.AUTO_CAPITALIZE] = next.autoCapitalize
            prefs[Keys.AUTO_SWITCH_LANG] = next.autoSwitchLanguage
            prefs[Keys.LEARNING_ENABLED] = next.learningEnabled
            prefs[Keys.ONE_HANDED_MODE] = next.oneHandedMode.id
            prefs[Keys.SPLIT_KEYBOARD] = next.splitKeyboard
            prefs[Keys.INCOGNITO_MODE] = next.incognitoMode
            prefs[Keys.AUTO_CORRECT_ENABLED] = next.autoCorrectEnabled
            prefs[Keys.SHOW_SUGGESTIONS] = next.showSuggestions
        }
    }

    suspend fun toggleLanguageEnabled(language: KeyboardLanguage) {
        val current = enabledLanguagesFlow.first().toMutableList()
        if (current.contains(language)) {
            if (current.size > 1) {
                current.remove(language)
            }
        } else {
            current.add(language)
        }
        context.bayanDataStore.edit { prefs ->
            prefs[Keys.ENABLED_LANGUAGES] = current.joinToString(",") { it.code }
            val active = KeyboardLanguage.fromCode(prefs[Keys.ACTIVE_LANGUAGE])
            if (!current.contains(active)) {
                prefs[Keys.ACTIVE_LANGUAGE] = current.first().code
            }
        }
    }

    suspend fun setActiveLanguage(language: KeyboardLanguage) {
        context.bayanDataStore.edit { it[Keys.ACTIVE_LANGUAGE] = language.code }
    }

    suspend fun selectAiProvider(provider: AiProviderType) {
        context.bayanDataStore.edit { prefs ->
            prefs[Keys.AI_PROVIDER] = provider.id
            if (provider != AiProviderType.CUSTOM) {
                prefs[Keys.ACTIVE_CUSTOM_ID] = -1L
                prefs[Keys.ACTIVE_CUSTOM_NAME] = ""
            }
        }
    }

    suspend fun activateCustomProvider(
        customId: Long,
        name: String,
        baseUrl: String,
        apiKey: String,
        model: String
    ) {
        context.bayanDataStore.edit { prefs ->
            prefs[Keys.AI_PROVIDER] = AiProviderType.CUSTOM.id
            prefs[Keys.ACTIVE_CUSTOM_ID] = customId
            prefs[Keys.ACTIVE_CUSTOM_NAME] = name.trim()
            prefs[Keys.AI_CUSTOM_BASE_URL] = baseUrl.trim()
            prefs[Keys.aiKeyFor(AiProviderType.CUSTOM.id)] = apiKey.trim()
            prefs[Keys.aiModelFor(AiProviderType.CUSTOM.id)] = model.trim()
        }
    }

    suspend fun updateAiCredentials(
        provider: AiProviderType,
        apiKey: String,
        model: String,
        customBaseUrl: String? = null
    ) {
        context.bayanDataStore.edit { prefs ->
            prefs[Keys.AI_PROVIDER] = provider.id
            prefs[Keys.aiKeyFor(provider.id)] = apiKey.trim()
            prefs[Keys.aiModelFor(provider.id)] = model.trim().ifBlank { provider.defaultModel }
            customBaseUrl?.let { prefs[Keys.AI_CUSTOM_BASE_URL] = it.trim() }
        }
    }

    suspend fun updateTranslationConfig(
        provider: TranslationProviderType,
        apiKey: String,
        endpoint: String,
        targetLanguage: TargetTranslateLanguage
    ) {
        context.bayanDataStore.edit { prefs ->
            prefs[Keys.TRANSLATION_PROVIDER] = provider.id
            prefs[Keys.TRANSLATION_API_KEY] = apiKey.trim()
            prefs[Keys.TRANSLATION_ENDPOINT] = endpoint.trim().ifBlank { provider.defaultEndpoint }
            prefs[Keys.TARGET_TRANSLATE_LANG] = targetLanguage.code
        }
    }

    suspend fun setTargetTranslateLanguage(targetLanguage: TargetTranslateLanguage) {
        context.bayanDataStore.edit { it[Keys.TARGET_TRANSLATE_LANG] = targetLanguage.code }
    }

    suspend fun setKeyShape(shape: KeyButtonShape) {
        context.bayanDataStore.edit { it[Keys.KEY_SHAPE] = shape.id }
    }

    suspend fun setKeyboardFontStyle(style: KeyboardFontStyle) {
        context.bayanDataStore.edit { it[Keys.FONT_STYLE] = style.id }
    }

    suspend fun updateGradualPasteConfig(transform: (GradualPasteConfig) -> GradualPasteConfig) {
        val current = gradualPasteConfigFlow.first()
        val next = transform(current)
        context.bayanDataStore.edit { prefs ->
            prefs[Keys.GP_ENABLED] = next.enabled
            prefs[Keys.GP_MODE] = next.mode.id
            prefs[Keys.GP_CHAR_SPEED] = next.charSpeed.coerceIn(1, 50)
            prefs[Keys.GP_WORD_SPEED] = next.wordSpeed.coerceIn(1, 50)
            prefs[Keys.GP_SPACE_RHYTHM] = next.spaceNewlineRhythm.coerceIn(1, 50)
            prefs[Keys.GP_ARROW_SPEED] = next.arrowNavSpeed.coerceIn(1, 50)
            prefs[Keys.GP_NATURAL_PULSE] = next.naturalRhythmPulse.coerceIn(1, 50)
            prefs[Keys.GP_BREATH_INTENSITY] = next.breathIntensity.coerceIn(1, 50)
            prefs[Keys.GP_CHARS_BEFORE_BREATH] = next.charsBeforeBreath.coerceIn(1, 50)
            prefs[Keys.GP_BREATH_DURATION] = next.breathPauseDuration.coerceIn(1, 50)
            prefs[Keys.GP_START_DELAY] = next.startDelay.coerceIn(1, 50)
        }
    }


    suspend fun exportSettingsJson(): JSONArray {
        val snapshot = context.bayanDataStore.data.first()
        return JSONArray().apply {
            snapshot.asMap().forEach { (key, value) ->
                val item = JSONObject().put("name", key.name)
                when (value) {
                    is Boolean -> item.put("type", "boolean").put("value", value)
                    is Int -> item.put("type", "int").put("value", value)
                    is Long -> item.put("type", "long").put("value", value)
                    is Float -> item.put("type", "float").put("value", value.toDouble())
                    is String -> item.put("type", "string").put("value", value)
                }
                put(item)
            }
        }
    }

    suspend fun importSettingsJson(settingsJson: JSONArray) {
        context.bayanDataStore.edit { prefs ->
            for (index in 0 until settingsJson.length()) {
                val item = settingsJson.optJSONObject(index) ?: continue
                val name = item.optString("name").takeIf { it.isNotBlank() } ?: continue
                val type = item.optString("type")
                when (type) {
                    "boolean" -> prefs[booleanPreferencesKey(name)] = item.optBoolean("value")
                    "int" -> prefs[intPreferencesKey(name)] = item.optInt("value")
                    "long" -> prefs[longPreferencesKey(name)] = item.optLong("value")
                    "float" -> prefs[floatPreferencesKey(name)] = item.optDouble("value").toFloat()
                    "string" -> prefs[stringPreferencesKey(name)] = item.optString("value")
                }
            }
        }
    }

    suspend fun updateToolbarVisibility(transform: (ToolbarVisibilityConfig) -> ToolbarVisibilityConfig) {
        val current = toolbarVisibilityFlow.first()
        val next = transform(current)
        context.bayanDataStore.edit { prefs ->
            prefs[Keys.TB_ORDER] = next.order.joinToString(",") { it.id }
            prefs[Keys.TB_HIDDEN] = next.hiddenActions.joinToString(",") { it.id }
            prefs[Keys.TB_SHOW_PROOFREAD] = next.showProofread
            prefs[Keys.TB_SHOW_BOTS] = next.showBots
            prefs[Keys.TB_SHOW_CLIPBOARD] = next.showClipboard
            prefs[Keys.TB_SHOW_QUICK_TOOLS] = next.showQuickTools
            prefs[Keys.TB_SHOW_VOICE] = next.showVoice
            prefs[Keys.TB_SHOW_PASSWORDS] = next.showPasswords
            prefs[Keys.TB_SHOW_TRANSLATE] = next.showTranslate
            prefs[Keys.TB_SHOW_EMOJI] = next.showEmoji
            prefs[Keys.TB_SHOW_SETTINGS] = next.showSettings
        }
    }
}
