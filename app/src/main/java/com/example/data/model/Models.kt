package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class AiProviderType(
    val id: String,
    val displayNameEn: String,
    val displayNameAr: String,
    val defaultModel: String,
    val suggestedModels: List<String>,
    val defaultBaseUrl: String,
    val requiresApiKey: Boolean = true
) {
    GEMINI(
        id = "gemini",
        displayNameEn = "Google Gemini",
        displayNameAr = "Google Gemini",
        defaultModel = "gemini-2.5-flash",
        suggestedModels = listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.0-flash"),
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta"
    ),
    OPENAI(
        id = "openai",
        displayNameEn = "OpenAI (ChatGPT)",
        displayNameAr = "OpenAI (ChatGPT)",
        defaultModel = "gpt-4o-mini",
        suggestedModels = listOf("gpt-4o-mini", "gpt-4o", "gpt-4.1-mini"),
        defaultBaseUrl = "https://api.openai.com/v1/chat/completions"
    ),
    ANTHROPIC(
        id = "anthropic",
        displayNameEn = "Anthropic (Claude)",
        displayNameAr = "Anthropic (Claude)",
        defaultModel = "claude-3-5-haiku-latest",
        suggestedModels = listOf("claude-3-5-haiku-latest", "claude-3-7-sonnet-latest"),
        defaultBaseUrl = "https://api.anthropic.com/v1/messages"
    ),
    GROQ(
        id = "groq",
        displayNameEn = "Groq (Ultra Fast)",
        displayNameAr = "Groq (فائق السرعة)",
        defaultModel = "llama-3.3-70b-versatile",
        suggestedModels = listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant"),
        defaultBaseUrl = "https://api.groq.com/openai/v1/chat/completions"
    ),
    OPENROUTER(
        id = "openrouter",
        displayNameEn = "OpenRouter",
        displayNameAr = "OpenRouter",
        defaultModel = "google/gemini-2.0-flash-001",
        suggestedModels = listOf("google/gemini-2.0-flash-001", "openai/gpt-4o-mini", "anthropic/claude-3.5-haiku"),
        defaultBaseUrl = "https://openrouter.ai/api/v1/chat/completions"
    ),
    EXAM1(
        id = "exam1",
        displayNameEn = "Exam1 · api.live",
        displayNameAr = "Exam1 · api.live (بدون مفتاح)",
        defaultModel = "gpt-4o-mini",
        suggestedModels = listOf("gpt-4o-mini"),
        defaultBaseUrl = "https://us-central1-conquer-apps-2ad61.cloudfunctions.net/prod/api.live",
        requiresApiKey = false
    ),
    GROKCLOUD(
        id = "grokcloud",
        displayNameEn = "GrokCloud",
        displayNameAr = "GrokCloud (بدون مفتاح)",
        defaultModel = "grok-4.7",
        suggestedModels = listOf("grok-4.7", "grok-4.6", "grok-4.5", "grok-4.3"),
        defaultBaseUrl = "https://multimodelhandler-746036526161.us-central1.run.app",
        requiresApiKey = false
    ),
    CUSTOM(
        id = "custom",
        displayNameEn = "Custom (OpenAI-Compatible)",
        displayNameAr = "مزود مخصص (متوافق مع OpenAI)",
        defaultModel = "deepseek-chat",
        suggestedModels = listOf("deepseek-chat", "qwen-plus", "mistral-small-latest"),
        defaultBaseUrl = "https://api.deepseek.com/v1/chat/completions"
    );

    fun isApiKeyOptional(customBaseUrl: String? = null): Boolean =
        !requiresApiKey || (this == CUSTOM && customBaseUrl.orEmpty().trimEnd('/').endsWith("api.live", ignoreCase = true))

    fun supportsFeature(feature: String): Boolean = when (feature.lowercase()) {
        "chat" -> true
        else -> this !in setOf(EXAM1, GROKCLOUD)
    }

    companion object {
        fun fromId(id: String?): AiProviderType = entries.firstOrNull { it.id == id } ?: GEMINI
    }
}

enum class TranslationProviderType(
    val id: String,
    val displayNameEn: String,
    val displayNameAr: String,
    val defaultEndpoint: String,
    val requiresApiKey: Boolean
) {
    USE_AI(
        id = "ai",
        displayNameEn = "Active AI Provider",
        displayNameAr = "مزود الذكاء الاصطناعي النشط",
        defaultEndpoint = "",
        requiresApiKey = false
    ),
    GOOGLE_TRANSLATE(
        id = "google_translate",
        displayNameEn = "Google Cloud Translate API",
        displayNameAr = "Google Cloud Translate API",
        defaultEndpoint = "https://translation.googleapis.com/language/translate/v2",
        requiresApiKey = true
    ),
    DEEPL(
        id = "deepl",
        displayNameEn = "DeepL API",
        displayNameAr = "DeepL API",
        defaultEndpoint = "https://api-free.deepl.com/v2/translate",
        requiresApiKey = true
    ),
    LIBRE_TRANSLATE(
        id = "libre",
        displayNameEn = "LibreTranslate / Custom API",
        displayNameAr = "LibreTranslate / نقطة نهاية مخصصة",
        defaultEndpoint = "https://libretranslate.com/translate",
        requiresApiKey = false
    );

    companion object {
        fun fromId(id: String?): TranslationProviderType = entries.firstOrNull { it.id == id } ?: USE_AI
    }
}

enum class KeyboardLanguage(
    val code: String,
    val labelEn: String,
    val labelAr: String,
    val shortBadge: String,
    val spaceLabel: String,
    val isRtl: Boolean
) {
    ARABIC("ar", "Arabic", "العربية", "ع", "العربية", true),
    ENGLISH("en", "English", "الإنجليزية", "EN", "English", false),
    FRENCH("fr", "Français", "الفرنسية", "FR", "Français", false),
    SPANISH("es", "Español", "الإسبانية", "ES", "Español", false),
    TURKISH("tr", "Türkçe", "التركية", "TR", "Türkçe", false);

    companion object {
        fun fromCode(code: String?): KeyboardLanguage = entries.firstOrNull { it.code == code } ?: ARABIC
    }
}

enum class TargetTranslateLanguage(
    val code: String,
    val deepLCode: String,
    val flagEmoji: String,
    val labelEn: String,
    val labelAr: String
) {
    ARABIC("ar", "AR", "🇸🇦", "Arabic", "العربية"),
    ENGLISH("en", "EN", "🇺🇸", "English", "الإنجليزية"),
    FRENCH("fr", "FR", "🇫🇷", "French", "الفرنسية"),
    SPANISH("es", "ES", "🇪🇸", "Spanish", "الإسبانية"),
    GERMAN("de", "DE", "🇩🇪", "German", "الألمانية"),
    TURKISH("tr", "TR", "🇹🇷", "Turkish", "التركية"),
    ITALIAN("it", "IT", "🇮🇹", "Italian", "الإيطالية"),
    PORTUGUESE("pt", "PT", "🇵🇹", "Portuguese", "البرتغالية"),
    RUSSIAN("ru", "RU", "🇷🇺", "Russian", "الروسية"),
    CHINESE("zh", "ZH", "🇨🇳", "Chinese", "الصينية"),
    JAPANESE("ja", "JA", "🇯🇵", "Japanese", "اليابانية"),
    KOREAN("ko", "KO", "🇰🇷", "Korean", "الكورية"),
    URDU("ur", "UR", "🇵🇰", "Urdu", "الأردية"),
    HINDI("hi", "HI", "🇮🇳", "Hindi", "الهندية");

    companion object {
        fun fromCode(code: String?): TargetTranslateLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

enum class ThemeMode(val id: String, val labelEn: String, val labelAr: String) {
    SYSTEM("system", "System", "النظام"),
    LIGHT("light", "Light", "فاتح"),
    DARK("dark", "Dark", "داكن"),
    CUSTOM("custom", "Custom", "مخصص");

    companion object {
        fun fromId(id: String?): ThemeMode = entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

data class KeyboardThemePreset(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val isDark: Boolean,
    val backgroundArgb: Long,
    val keyArgb: Long,
    val specialKeyArgb: Long,
    val accentArgb: Long,
    val textArgb: Long,
    val toolbarArgb: Long? = null
)

object KeyboardThemePresets {
    val midnightEmerald = KeyboardThemePreset(
        id = "midnight_emerald",
        nameEn = "Midnight Emerald",
        nameAr = "زمرد الليل",
        isDark = true,
        backgroundArgb = 0xFF0B1120,
        keyArgb = 0xFF1E293B,
        specialKeyArgb = 0xFF151E30,
        accentArgb = 0xFF10B981,
        textArgb = 0xFFF8FAFC
    )

    val ivoryPaper = KeyboardThemePreset(
        id = "ivory_paper",
        nameEn = "Ivory Studio",
        nameAr = "عاجي أنيق",
        isDark = false,
        backgroundArgb = 0xFFEAEFF5,
        keyArgb = 0xFFFFFFFF,
        specialKeyArgb = 0xFFD8E1EC,
        accentArgb = 0xFF0D9488,
        textArgb = 0xFF0F172A
    )

    val obsidianViolet = KeyboardThemePreset(
        id = "obsidian_violet",
        nameEn = "Obsidian Pulse",
        nameAr = "أوبسيديان",
        isDark = true,
        backgroundArgb = 0xFF09090B,
        keyArgb = 0xFF18181B,
        specialKeyArgb = 0xFF121216,
        accentArgb = 0xFF8B5CF6,
        textArgb = 0xFFFAFAFA
    )

    val desertAmber = KeyboardThemePreset(
        id = "desert_amber",
        nameEn = "Warm Sandstone",
        nameAr = "رمال ذهبية",
        isDark = false,
        backgroundArgb = 0xFFF2ECE1,
        keyArgb = 0xFFFFFBF4,
        specialKeyArgb = 0xFFE4D9C5,
        accentArgb = 0xFFD97706,
        textArgb = 0xFF292524
    )

    val royalOcean = KeyboardThemePreset(
        id = "royal_ocean",
        nameEn = "Royal Ocean",
        nameAr = "محيط ملكي",
        isDark = true,
        backgroundArgb = 0xFF082032,
        keyArgb = 0xFF1A374D,
        specialKeyArgb = 0xFF112A3D,
        accentArgb = 0xFF38BDF8,
        textArgb = 0xFFF0F9FF
    )

    val bayanMidnightBlue = KeyboardThemePreset(
        id = "bayan_midnight_blue",
        nameEn = "Bayan Midnight Blue",
        nameAr = "بيان الأزرق الليلي",
        isDark = true,
        backgroundArgb = 0xFF101010,
        keyArgb = 0xFF252324,
        specialKeyArgb = 0xFF45689F,
        accentArgb = 0xFF6196EC,
        textArgb = 0xFFF5F5F7,
        toolbarArgb = 0xFF252324
    )

    val roseQuartz = KeyboardThemePreset(
        id = "rose_quartz",
        nameEn = "Rose Mist",
        nameAr = "وردي هادئ",
        isDark = false,
        backgroundArgb = 0xFFF9ECEF,
        keyArgb = 0xFFFFFFFF,
        specialKeyArgb = 0xFFEDD6DC,
        accentArgb = 0xFFE11D48,
        textArgb = 0xFF3B1520
    )

    val all = listOf(
        midnightEmerald,
        ivoryPaper,
        obsidianViolet,
        desertAmber,
        royalOcean,
        bayanMidnightBlue,
        roseQuartz
    )
}

enum class KeyButtonShape(
    val id: String,
    val labelAr: String,
    val labelEn: String
) {
    ROUNDED("rounded", "مدور (قياسي)", "Rounded (Standard)"),
    SHARP_SQUARE("sharp", "مربع حاد", "Sharp Square"),
    NO_BACKGROUND("borderless", "بدون خلفية", "No Background"),
    SOFT_CIRCLE("circle", "دائري ناعم", "Soft Circle");

    companion object {
        fun fromId(id: String?): KeyButtonShape =
            entries.firstOrNull { it.id == id } ?: ROUNDED
    }
}

enum class KeyboardFontStyle(
    val id: String,
    val labelAr: String,
    val labelEn: String
) {
    CAIRO("cairo", "كايرو", "Cairo"),
    IBM_PLEX("ibm_plex", "IBM Plex Sans Arabic", "IBM Plex Sans Arabic"),
    SYSTEM_SANS("system_sans", "خط الهاتف", "System sans"),
    SYSTEM_SERIF("system_serif", "نسخي النظام", "System serif");

    companion object {
        fun fromId(id: String?): KeyboardFontStyle = entries.firstOrNull { it.id == id } ?: CAIRO
    }
}

data class KeyboardThemeConfig(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val presetId: String = KeyboardThemePresets.midnightEmerald.id,
    val backgroundArgb: Long = KeyboardThemePresets.midnightEmerald.backgroundArgb,
    val keyArgb: Long = KeyboardThemePresets.midnightEmerald.keyArgb,
    val specialKeyArgb: Long = KeyboardThemePresets.midnightEmerald.specialKeyArgb,
    val accentArgb: Long = KeyboardThemePresets.midnightEmerald.accentArgb,
    val textArgb: Long = KeyboardThemePresets.midnightEmerald.textArgb,
    val keyCornerRadiusDp: Float = 9f,
    val showKeyBorders: Boolean = true,
    val keyShape: KeyButtonShape = KeyButtonShape.ROUNDED,
    val fontStyle: KeyboardFontStyle = KeyboardFontStyle.CAIRO
) {
    fun resolvePalette(isSystemDark: Boolean): ResolvedKeyboardPalette {
        val useDark = when (themeMode) {
            ThemeMode.SYSTEM -> isSystemDark
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.CUSTOM -> isColorDark(backgroundArgb)
        }
        return when (themeMode) {
            ThemeMode.CUSTOM -> ResolvedKeyboardPalette(
                background = Color(backgroundArgb),
                key = Color(keyArgb),
                specialKey = Color(specialKeyArgb),
                accent = Color(accentArgb),
                text = Color(textArgb),
                isDark = useDark,
                cornerRadiusDp = keyCornerRadiusDp,
                showBorders = showKeyBorders,
                keyShape = keyShape
            )
            ThemeMode.SYSTEM -> {
                val preset = if (isSystemDark) {
                    KeyboardThemePresets.all.firstOrNull { it.id == presetId && it.isDark }
                        ?: KeyboardThemePresets.midnightEmerald
                } else {
                    KeyboardThemePresets.all.firstOrNull { it.id == presetId && !it.isDark }
                        ?: KeyboardThemePresets.ivoryPaper
                }
                ResolvedKeyboardPalette(
                    background = Color(preset.backgroundArgb),
                    key = Color(preset.keyArgb),
                    specialKey = Color(preset.specialKeyArgb),
                    accent = Color(preset.accentArgb),
                    text = Color(preset.textArgb),
                    isDark = preset.isDark,
                    cornerRadiusDp = keyCornerRadiusDp,
                    showBorders = showKeyBorders,
                    keyShape = keyShape,
                    toolbarBackground = Color(preset.toolbarArgb ?: preset.backgroundArgb)
                )
            }
            ThemeMode.DARK, ThemeMode.LIGHT -> {
                val targetDark = themeMode == ThemeMode.DARK
                val preset = KeyboardThemePresets.all.firstOrNull { it.id == presetId && it.isDark == targetDark }
                    ?: if (targetDark) KeyboardThemePresets.midnightEmerald else KeyboardThemePresets.ivoryPaper
                ResolvedKeyboardPalette(
                    background = Color(preset.backgroundArgb),
                    key = Color(preset.keyArgb),
                    specialKey = Color(preset.specialKeyArgb),
                    accent = Color(preset.accentArgb),
                    text = Color(preset.textArgb),
                    isDark = preset.isDark,
                    cornerRadiusDp = keyCornerRadiusDp,
                    showBorders = showKeyBorders,
                    keyShape = keyShape,
                    toolbarBackground = Color(preset.toolbarArgb ?: preset.backgroundArgb)
                )
            }
        }
    }

    private fun isColorDark(argb: Long): Boolean {
        val r = ((argb shr 16) and 0xFF) / 255.0
        val g = ((argb shr 8) and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0
        val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
        return luminance < 0.5
    }
}

data class ResolvedKeyboardPalette(
    val background: Color,
    val key: Color,
    val specialKey: Color,
    val accent: Color,
    val text: Color,
    val isDark: Boolean,
    val cornerRadiusDp: Float,
    val showBorders: Boolean,
    val keyShape: KeyButtonShape = KeyButtonShape.ROUNDED,
    val toolbarBackground: Color = Color.Transparent
)

enum class NumberRowStyle(
    val id: String,
    val labelAr: String,
    val labelEn: String,
    val sampleDigits: String
) {
    ARABIC_EASTERN("arabic_eastern", "١٢٣ عربي", "١٢٣ Arabic", "١٢٣"),
    WESTERN("western", "123 إنجليزي", "123 English", "123");

    companion object {
        fun fromId(id: String?): NumberRowStyle =
            entries.firstOrNull { it.id == id } ?: WESTERN
    }
}

enum class OneHandedMode(val id: String, val labelAr: String, val labelEn: String) {
    OFF("off", "عادي", "Off"),
    RIGHT("right", "يمين", "Right"),
    LEFT("left", "يسار", "Left");

    companion object {
        fun fromId(id: String?): OneHandedMode =
            entries.firstOrNull { it.id == id } ?: OFF
    }
}

data class KeyboardSizeConfig(
    val heightScale: Float = 1.0f,
    val fontScale: Float = 1.0f,
    val bottomPaddingDp: Float = 6f,
    val showNumberRow: Boolean = true,
    val numberRowStyle: NumberRowStyle = NumberRowStyle.WESTERN,
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val autoCapitalize: Boolean = true,
    val autoSwitchLanguage: Boolean = true,
    val learningEnabled: Boolean = true,
    val oneHandedMode: OneHandedMode = OneHandedMode.OFF,
    val splitKeyboard: Boolean = false,
    val incognitoMode: Boolean = false,
    val autoCorrectEnabled: Boolean = false,
    val showSuggestions: Boolean = true
)

data class AiProviderConfig(
    val aiProvider: AiProviderType = AiProviderType.GEMINI,
    val activeCustomProviderId: Long? = null,
    val activeCustomProviderName: String? = null,
    val aiApiKey: String = "",
    val aiModel: String = AiProviderType.GEMINI.defaultModel,
    val aiCustomBaseUrl: String = AiProviderType.CUSTOM.defaultBaseUrl,
    val translationProvider: TranslationProviderType = TranslationProviderType.USE_AI,
    val translationApiKey: String = "",
    val translationEndpoint: String = TranslationProviderType.GOOGLE_TRANSLATE.defaultEndpoint,
    val defaultTargetLanguage: TargetTranslateLanguage = TargetTranslateLanguage.ENGLISH,
    val stream: Boolean = false
) {
    fun keyFor(provider: AiProviderType = aiProvider): String =
        if (provider == AiProviderType.EXAM1 || provider == AiProviderType.GROKCLOUD) "no-key-needed" else aiApiKey

    val activeDisplayName: String
        get() = if (aiProvider == AiProviderType.CUSTOM && !activeCustomProviderName.isNullOrBlank()) {
            activeCustomProviderName
        } else {
            aiProvider.displayNameEn
        }
}

enum class ToolbarAction(
    val id: String,
    val labelAr: String,
    val labelEn: String
) {
    PROOFREAD("proofread", "تصحيح الذكاء الاصطناعي", "AI Proofread"),
    BOTS("bots", "مساعد روبوت", "AI Bots"),
    CLIPBOARD("clipboard", "الحافظة", "Clipboard"),
    QUICK_TOOLS("quick_tools", "أدوات الإدخال", "Input Tools"),
    VOICE("voice", "الكتابة بالصوت", "Voice Typing"),
    PASSWORDS("passwords", "مدير كلمات المرور", "Passwords"),
    TRANSLATE("translate", "الترجمة", "Translate"),
    EMOJI("emoji", "الرموز التعبيرية", "Emoji"),
    SETTINGS("settings", "الإعدادات", "Settings");

    companion object {
        fun fromId(id: String?): ToolbarAction? = entries.firstOrNull { it.id == id }

        fun defaultOrder(): List<ToolbarAction> = listOf(
            PROOFREAD, BOTS, CLIPBOARD, QUICK_TOOLS, VOICE, PASSWORDS, TRANSLATE, EMOJI, SETTINGS
        )
    }
}

data class ToolbarVisibilityConfig(
    val order: List<ToolbarAction> = ToolbarAction.defaultOrder(),
    val hiddenActions: Set<ToolbarAction> = emptySet()
) {
    fun isVisible(action: ToolbarAction): Boolean = !hiddenActions.contains(action)

    val showProofread: Boolean get() = isVisible(ToolbarAction.PROOFREAD)
    val showBots: Boolean get() = isVisible(ToolbarAction.BOTS)
    val showClipboard: Boolean get() = isVisible(ToolbarAction.CLIPBOARD)
    val showQuickTools: Boolean get() = isVisible(ToolbarAction.QUICK_TOOLS)
    val showVoice: Boolean get() = isVisible(ToolbarAction.VOICE)
    val showPasswords: Boolean get() = isVisible(ToolbarAction.PASSWORDS)
    val showTranslate: Boolean get() = isVisible(ToolbarAction.TRANSLATE)
    val showEmoji: Boolean get() = isVisible(ToolbarAction.EMOJI)
    val showSettings: Boolean get() = isVisible(ToolbarAction.SETTINGS)

    fun withVisibility(action: ToolbarAction, visible: Boolean): ToolbarVisibilityConfig {
        val nextHidden = if (visible) hiddenActions - action else hiddenActions + action
        return copy(hiddenActions = nextHidden)
    }

    fun moveAction(action: ToolbarAction, delta: Int): ToolbarVisibilityConfig {
        val currentIdx = order.indexOf(action)
        if (currentIdx == -1) return this
        val targetIdx = (currentIdx + delta).coerceIn(0, order.lastIndex)
        if (targetIdx == currentIdx) return this
        val mutable = order.toMutableList()
        val removed = mutable.removeAt(currentIdx)
        mutable.add(targetIdx, removed)
        return copy(order = mutable)
    }
}

data class ProofreadDiffReview(
    val originalText: String,
    val correctedText: String,
    val replaceWholeField: Boolean,
    val title: String = "نتيجة التصحيح",
    val isApplied: Boolean = false,
    val canRedo: Boolean = false
)

enum class PasswordServiceBrand(
    val id: String,
    val labelAr: String,
    val labelEn: String,
    val badgeBgArgb: Long,
    val badgeFgArgb: Long
) {
    GOOGLE("google", "جوجل", "Google", 0xFFFFFFFF, 0xFF4285F4),
    X_TWITTER("x", "إكس", "X", 0xFF000000, 0xFFFFFFFF),
    GITHUB("github", "جيت هب", "GitHub", 0xFF111827, 0xFFFFFFFF),
    INSTAGRAM("instagram", "إنستغرام", "Instagram", 0xFFE1306C, 0xFFFFFFFF),
    FACEBOOK("facebook", "فيسبوك", "Facebook", 0xFF1877F2, 0xFFFFFFFF),
    MICROSOFT("microsoft", "مايكروسوفت", "Microsoft", 0xFF0F172A, 0xFF38BDF8),
    APPLE("apple", "أبل", "Apple", 0xFF1E293B, 0xFFFFFFFF),
    TELEGRAM("telegram", "تيليجرام", "Telegram", 0xFF229ED9, 0xFFFFFFFF),
    YOUTUBE("youtube", "يوتيوب", "YouTube", 0xFFFF0000, 0xFFFFFFFF),
    BANK("bank", "بنك / مالي", "Bank", 0xFF0F766E, 0xFFFFFFFF),
    CUSTOM("custom", "موقع / تطبيق", "Website", 0xFFF8FAFC, 0xFF94A3B8);

    companion object {
        fun fromId(id: String?): PasswordServiceBrand =
            entries.firstOrNull { it.id == id } ?: CUSTOM

        fun detectFrom(siteName: String, siteUrl: String): PasswordServiceBrand {
            val haystack = "$siteName $siteUrl".lowercase()
            return when {
                "google" in haystack || "gmail" in haystack -> GOOGLE
                "twitter" in haystack || "x.com" in haystack || haystack.trim() == "x" -> X_TWITTER
                "github" in haystack -> GITHUB
                "instagram" in haystack || "insta" in haystack -> INSTAGRAM
                "facebook" in haystack || "fb.com" in haystack -> FACEBOOK
                "microsoft" in haystack || "outlook" in haystack || "live.com" in haystack -> MICROSOFT
                "apple" in haystack || "icloud" in haystack -> APPLE
                "telegram" in haystack || "t.me" in haystack -> TELEGRAM
                "youtube" in haystack || "youtu.be" in haystack -> YOUTUBE
                "bank" in haystack || "بنك" in haystack || "مصرف" in haystack -> BANK
                else -> CUSTOM
            }
        }
    }
}

data class PasswordEntryItem(
    val id: Long = 0,
    val siteName: String,
    val siteUrl: String = "",
    val username: String,
    val password: String,
    val brand: PasswordServiceBrand = PasswordServiceBrand.CUSTOM,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class GradualPasteMode(
    val id: String,
    val labelAr: String,
    val labelEn: String
) {
    CHAR_BY_CHAR("char", "النمط: حرف بحرف", "Mode: Character by Character"),
    WORD_BY_WORD("word", "النمط: كلمة بكلمة", "Mode: Word by Word");

    companion object {
        fun fromId(id: String?): GradualPasteMode =
            entries.firstOrNull { it.id == id } ?: CHAR_BY_CHAR
    }
}

data class GradualPasteConfig(
    val enabled: Boolean = false,
    val mode: GradualPasteMode = GradualPasteMode.CHAR_BY_CHAR,
    val charSpeed: Int = 25,
    val wordSpeed: Int = 22,
    val spaceNewlineRhythm: Int = 20,
    val arrowNavSpeed: Int = 25,
    val naturalRhythmPulse: Int = 15,
    val breathIntensity: Int = 18,
    val charsBeforeBreath: Int = 25,
    val breathPauseDuration: Int = 20,
    val startDelay: Int = 10
) {
    fun charDelayMs(): Int = (169 - (charSpeed.coerceIn(1, 50) - 1) * 3.333f).toInt().coerceAtLeast(8)
    fun wordDelayMs(): Int = (280 - (wordSpeed.coerceIn(1, 50) - 1) * 5.2f).toInt().coerceAtLeast(18)
    fun spaceDelayMs(): Int = (150 - (spaceNewlineRhythm.coerceIn(1, 50) - 1) * 3.21f).toInt().coerceAtLeast(10)
    fun arrowDelayMs(): Int = (151 - (arrowNavSpeed.coerceIn(1, 50) - 1) * 3.0f).toInt().coerceAtLeast(10)
    fun breathPauseMs(): Int = (40 + breathPauseDuration.coerceIn(1, 50) * 19.8f).toInt().coerceIn(50, 1100)
    fun startDelayMs(): Int = (26 + startDelay.coerceIn(1, 50) * 10).coerceIn(20, 600)
}

enum class QuickToolCategory(val labelAr: String, val labelEn: String) {
    EDIT_CLIPBOARD("التحرير والحافظة", "Edit & Clipboard"),
    CURSOR_NAV("التنقل والمؤشر", "Cursor & Navigation"),
    MODES_ACTIONS("الأوضاع والأدوات", "Modes & Tools")
}

enum class QuickInputTool(
    val id: String,
    val labelAr: String,
    val labelEn: String,
    val category: QuickToolCategory
) {
    // Edit & Clipboard
    SELECT_ALL("select_all", "تحديد الكل", "Select All", QuickToolCategory.EDIT_CLIPBOARD),
    SELECT_WORD("select_word", "اختر كلمة", "Select Word", QuickToolCategory.EDIT_CLIPBOARD),
    COPY("copy", "نسخ", "Copy", QuickToolCategory.EDIT_CLIPBOARD),
    CUT("cut", "قصّ", "Cut", QuickToolCategory.EDIT_CLIPBOARD),
    PASTE("paste", "لصق", "Paste", QuickToolCategory.EDIT_CLIPBOARD),
    UNDO("undo", "تراجع", "Undo", QuickToolCategory.EDIT_CLIPBOARD),
    REDO("redo", "إعادة", "Redo", QuickToolCategory.EDIT_CLIPBOARD),
    CLEAR_CLIPBOARD("clear_clipboard", "مسح الحافظة", "Clear Clip", QuickToolCategory.EDIT_CLIPBOARD),

    // Cursor & Navigation
    CURSOR_LEFT("cursor_left", "يسار", "Left", QuickToolCategory.CURSOR_NAV),
    CURSOR_RIGHT("cursor_right", "يمين", "Right", QuickToolCategory.CURSOR_NAV),
    CURSOR_UP("cursor_up", "أعلى", "Up", QuickToolCategory.CURSOR_NAV),
    CURSOR_DOWN("cursor_down", "أسفل", "Down", QuickToolCategory.CURSOR_NAV),
    WORD_LEFT("word_left", "كلمة لليسار", "Word Left", QuickToolCategory.CURSOR_NAV),
    WORD_RIGHT("word_right", "كلمة لليمين", "Word Right", QuickToolCategory.CURSOR_NAV),
    LINE_START("line_start", "أقصى اليسار", "Line Start", QuickToolCategory.CURSOR_NAV),
    LINE_END("line_end", "أقصى اليمين", "Line End", QuickToolCategory.CURSOR_NAV),
    PAGE_UP("page_up", "الصفحة السابقة", "Page Up", QuickToolCategory.CURSOR_NAV),
    PAGE_DOWN("page_down", "الصفحة التالية", "Page Down", QuickToolCategory.CURSOR_NAV),
    DOC_START("doc_start", "بداية الصفحة", "Top of Page", QuickToolCategory.CURSOR_NAV),
    DOC_END("doc_end", "نهاية الصفحة", "End of Page", QuickToolCategory.CURSOR_NAV),

    // Modes & Tools
    NUMPAD("numpad", "لوحة الأرقام", "Number Pad", QuickToolCategory.MODES_ACTIONS),
    ONE_HANDED("one_handed", "بيد واحدة", "One-Handed", QuickToolCategory.MODES_ACTIONS),
    SPLIT_KEYBOARD("split_keyboard", "لوحة مقسمة", "Split Keys", QuickToolCategory.MODES_ACTIONS),
    INCOGNITO("incognito", "تصفح متخفي", "Incognito", QuickToolCategory.MODES_ACTIONS),
    AUTO_CORRECT("auto_correct", "تصحيح تلقائي", "Auto-Correct", QuickToolCategory.MODES_ACTIONS),
    VOICE_INPUT("voice_input", "إدخال صوتي", "Voice Input", QuickToolCategory.MODES_ACTIONS),
    CLIPBOARD("clipboard", "الحافظة", "Clipboard", QuickToolCategory.MODES_ACTIONS),
    EMOJI("emoji", "إيموجي", "Emoji", QuickToolCategory.MODES_ACTIONS),
    SETTINGS("settings", "الإعدادات", "Settings", QuickToolCategory.MODES_ACTIONS)
}

enum class BotIconOption(
    val id: String,
    val labelAr: String,
    val labelEn: String,
    val accentArgb: Long
) {
    BOT_CHAT("bot_chat", "مساعد ذكي", "Smart Bot", 0xFF3B82F6),
    CONTENT_CREATOR("content_creator", "مُنشئ محتوى", "Content", 0xFF8B5CF6),
    CODE_DEV("code_dev", "مبرمج", "Developer", 0xFF10B981),
    SALES_PRO("sales_pro", "مبيعات", "Sales", 0xFFF59E0B),
    TEACHER("teacher", "معلم ومدقق", "Tutor", 0xFF06B6D4),
    TRANSLATOR("translator", "مترجم", "Translator", 0xFFEC4899),
    SUPPORT("support", "دعم فني", "Support", 0xFF14B8A6),
    SOCIAL("social", "تسويق ونشر", "Social", 0xFFEF4444),
    LEGAL("legal", "رسمي وقانوني", "Formal", 0xFF6366F1),
    IDEA_SPARK("idea_spark", "أفكار وإبداع", "Creative", 0xFFEAB308);

    companion object {
        fun fromId(id: String?): BotIconOption =
            entries.firstOrNull { it.id == id } ?: BOT_CHAT
    }
}

data class BotKnowledgeSource(
    val id: String,
    val title: String,
    val content: String,
    val sourceType: String = "text"
) {
    companion object {
        fun listFromJson(raw: String): List<BotKnowledgeSource> {
            if (raw.isBlank() || raw == "[]") return emptyList()
            return try {
                val arr = org.json.JSONArray(raw)
                buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val title = obj.optString("title").trim()
                        val content = obj.optString("content")
                        if (title.isNotBlank() && content.isNotBlank()) {
                            add(
                                BotKnowledgeSource(
                                    id = obj.optString("id").ifBlank { "src_$i" },
                                    title = title,
                                    content = content,
                                    sourceType = obj.optString("sourceType", "text")
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

        fun listToJson(items: List<BotKnowledgeSource>): String {
            val arr = org.json.JSONArray()
            items.forEach { item ->
                arr.put(
                    org.json.JSONObject().apply {
                        put("id", item.id)
                        put("title", item.title)
                        put("content", item.content)
                        put("sourceType", item.sourceType)
                    }
                )
            }
            return arr.toString()
        }
    }
}

data class CustomBotItem(
    val id: Long = 0,
    val name: String,
    val prompt: String,
    val iconOption: BotIconOption = BotIconOption.BOT_CHAT,
    val accentArgb: Long = iconOption.accentArgb,
    val knowledgeSources: List<BotKnowledgeSource> = emptyList(),
    val isPinnedToToolbar: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class BuiltinBotAction(
    val id: String,
    val labelAr: String,
    val labelEn: String
) {
    GRAMMAR("grammar", "قواعد اللغة", "Grammar"),
    TRANSLATE("translate", "ترجمة", "Translate"),
    REPHRASE("rephrase", "إعادة صياغة", "Rephrase"),
    CHANGE_TONE("change_tone", "تغيير النبرة", "Change Tone"),
    ENHANCE_WORDS("enhance_words", "تعزيز الكلمات", "Enhance Words"),
    ASK_AI("ask_ai", "اسأل الذكاء الاصطناعي", "Ask AI"),
    TO_EMOJI("to_emoji", "تحويل إلى إيموجي", "To Emoji"),
    REPLY("reply", "الرد", "Reply"),
    PROOFREAD("proofread", "تصحيح", "Proofread"),
    CONTINUE_WRITING("continue_writing", "متابعة الكتابة", "Continue Writing")
}
