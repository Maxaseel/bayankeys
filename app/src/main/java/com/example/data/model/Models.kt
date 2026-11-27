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
    HINDI("hi", "HI", "🇮🇳", "Hindi", "الهندية"),
    DUTCH("nl", "NL", "🇳🇱", "Dutch", "الهولندية"),
    GREEK("el", "EL", "🇬🇷", "Greek", "اليونانية"),
    SWEDISH("sv", "SV", "🇸🇪", "Swedish", "السويدية"),
    NORWEGIAN("no", "NB", "🇳🇴", "Norwegian", "النرويجية"),
    DANISH("da", "DA", "🇩🇰", "Danish", "الدنماركية"),
    FINNISH("fi", "FI", "🇫🇮", "Finnish", "الفنلندية"),
    POLISH("pl", "PL", "🇵🇱", "Polish", "البولندية"),
    CZECH("cs", "CS", "🇨🇿", "Czech", "التشيكية"),
    SLOVAK("sk", "SK", "🇸🇰", "Slovak", "السلوفاكية"),
    UKRAINIAN("uk", "UK", "🇺🇦", "Ukrainian", "الأوكرانية"),
    BULGARIAN("bg", "BG", "🇧🇬", "Bulgarian", "البلغارية"),
    ROMANIAN("ro", "RO", "🇷🇴", "Romanian", "الرومانية"),
    HUNGARIAN("hu", "HU", "🇭🇺", "Hungarian", "الهنغارية"),
    CROATIAN("hr", "HR", "🇭🇷", "Croatian", "الكرواتية"),
    SERBIAN("sr", "SR", "🇷🇸", "Serbian", "الصربية"),
    SLOVENIAN("sl", "SL", "🇸🇮", "Slovenian", "السلوفينية"),
    HEBREW("he", "HE", "🇮🇱", "Hebrew", "العبرية"),
    THAI("th", "TH", "🇹🇭", "Thai", "التايلندية"),
    VIETNAMESE("vi", "VI", "🇻🇳", "Vietnamese", "الفيتنامية"),
    INDONESIAN("id", "ID", "🇮🇩", "Indonesian", "الإندونيسية"),
    MALAY("ms", "MS", "🇲🇾", "Malay", "الملايوية"),
    FILIPINO("tl", "TL", "🇵🇭", "Filipino", "الفلبينية"),
    BENGALI("bn", "BN", "🇧🇩", "Bengali", "البنغالية"),
    PERSIAN("fa", "FA", "🇮🇷", "Persian", "الفارسية"),
    SWAHILI("sw", "SW", "🇰🇪", "Swahili", "السواحيلية"),
    AFRIKAANS("af", "AF", "🇿🇦", "Afrikaans", "الأفريقانية"),
    AZERBAIJANI("az", "AZ", "🇦🇿", "Azerbaijani", "الأذربيجانية"),
    CATALAN("ca", "CA", "🇪🇸", "Catalan", "الكتالونية"),
    ICELANDIC("is", "IS", "🇮🇸", "Icelandic", "الآيسلندية"),
    MACEDONIAN("mk", "MK", "🇲🇰", "Macedonian", "المقدونية"),
    NEPALI("ne", "NE", "🇳🇵", "Nepali", "النيبالية"),
    TAMIL("ta", "TA", "🇮🇳", "Tamil", "التاميلية"),
    TELUGU("te", "TE", "🇮🇳", "Telugu", "التيلوغوية"),
    MARATHI("mr", "MR", "🇮🇳", "Marathi", "الماراثية"),
    KANNADA("kn", "KN", "🇮🇳", "Kannada", "الكانادا"),
    PUNJABI("pa", "PA", "🇮🇳", "Punjabi", "البنجابية"),
    BURMESE("my", "MY", "🇲🇲", "Burmese", "البورمية"),
    KHMER("km", "KM", "🇰🇭", "Khmer", "الخميرية"),
    GEORGIAN("ka", "KA", "🇬🇪", "Georgian", "الجورجية"),
    ARMENIAN("hy", "HY", "🇦🇲", "Armenian", "الأرمنية"),
    UZBEK("uz", "UZ", "🇺🇿", "Uzbek", "الأوزبكية"),
    KAZAKH("kk", "KK", "🇰🇿", "Kazakh", "الكازاخية"),
    ALBANIAN("sq", "SQ", "🇦🇱", "Albanian", "الألبانية"),
    BOSNIAN("bs", "BS", "🇧🇦", "Bosnian", "البوسنية"),
    IRISH("ga", "GA", "🇮🇪", "Irish", "الأيرلندية"),
    SOMALI("so", "SO", "🇸🇴", "Somali", "الصومالية"),
    PASHTO("ps", "PS", "🇦🇫", "Pashto", "البشتوية"),
    WELSH("cy", "CY", "🏴", "Welsh", "الويلزية"),
    AMHARIC("am", "AM", "🇪🇹", "Amharic", "الأمهرية");

    companion object {
        val quickChoices: List<TargetTranslateLanguage> = listOf(
            ARABIC, ENGLISH, FRENCH, SPANISH, GERMAN, TURKISH, ITALIAN,
            PORTUGUESE, RUSSIAN, CHINESE, JAPANESE, KOREAN, URDU, HINDI
        )

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
    val toolbarArgb: Long? = null,
    val isNew: Boolean = false,
    val keyPaletteArgb: List<Long> = emptyList()
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
        roseQuartz,
        KeyboardThemePreset("graphite_gray", "Graphite Gray", "رمادي الغرافيت", true, 0xFF20252B, 0xFF343B44, 0xFF292F36, 0xFF9BA8B5, 0xFFF2F5F7, isNew = true),
        KeyboardThemePreset("deep_black", "Deep Black", "أسود عميق", true, 0xFF090A0C, 0xFF191B1F, 0xFF111317, 0xFF7C9CFF, 0xFFF5F6F8, isNew = true),
        KeyboardThemePreset("os_premium_white", "OS Premium White", "أبيض فاخر", false, 0xFFE9EDF2, 0xFFFFFFFF, 0xFFD9E0E8, 0xFF2769C5, 0xFF182331, isNew = true),
        KeyboardThemePreset("os_glass_white", "OS Glass White", "زجاج أبيض", false, 0xFFE6EDF4, 0xFFF9FCFF, 0xFFD1DDE8, 0xFF397CA8, 0xFF172A39, isNew = true),
        KeyboardThemePreset("pearl_white", "Pearl White", "لؤلؤ أبيض", false, 0xFFF1F0ED, 0xFFFFFEFC, 0xFFE0DED8, 0xFF8A6B45, 0xFF292722, isNew = true),
        KeyboardThemePreset("crimson_night", "Crimson Night", "ليل قرمزي", true, 0xFF200D16, 0xFF381522, 0xFF2C101B, 0xFFFF5574, 0xFFFFF2F4, isNew = true),
        KeyboardThemePreset("silver_glass", "Silver Glass", "زجاج فضي", false, 0xFFDDE3E8, 0xFFF8FAFC, 0xFFC7D0D8, 0xFF496B88, 0xFF172431, isNew = true),
        KeyboardThemePreset("pink_shadow", "Pink Shadow", "ظل وردي", true, 0xFF261624, 0xFF3B2338, 0xFF301B2D, 0xFFF27DB5, 0xFFFFF2F8, isNew = true),
        KeyboardThemePreset("peach_glass", "Peach Glass", "زجاج خوخي", false, 0xFFF6E7DF, 0xFFFFF8F2, 0xFFEAD3C7, 0xFFB65D3E, 0xFF34221B, isNew = true),
        KeyboardThemePreset("mystic_purple", "Mystic Purple", "أرجوان غامض", true, 0xFF19132C, 0xFF2B2145, 0xFF221A39, 0xFFB69AFF, 0xFFF6F1FF, isNew = true),
        KeyboardThemePreset("colorpulse", "ColorPulse", "نبض الألوان", true, 0xFF101B27, 0xFF1D2D3B, 0xFF172431, 0xFF43E0C0, 0xFFF1FAFF, isNew = true, keyPaletteArgb = listOf(0xFFFF8393L, 0xFFFFC46B, 0xFF8ADE7D, 0xFF70D5E8, 0xFF9F9AFF, 0xFFFF8AD5)),
        KeyboardThemePreset("desert_gold", "Desert Gold", "ذهب الصحراء", false, 0xFFF0E6D4, 0xFFFFFAEF, 0xFFE0D0B2, 0xFF946019, 0xFF302517, isNew = true),
        KeyboardThemePreset("aquanova", "AquaNova", "أكوانوفا", true, 0xFF09232B, 0xFF123640, 0xFF0E2D37, 0xFF46D9D0, 0xFFE9FFFF, isNew = true),
        KeyboardThemePreset("berrypop", "BerryPop", "توت نابض", false, 0xFFF4E5EF, 0xFFFFF9FD, 0xFFE7CCDE, 0xFF9B3477, 0xFF32162A, isNew = true),
        KeyboardThemePreset("matcha_keys", "MatchaKeys", "ماتشا أخضر", false, 0xFFE7EBDD, 0xFFFAFCF5, 0xFFD0D9C3, 0xFF52723A, 0xFF20291A, isNew = true),
        KeyboardThemePreset("sunset_orange", "Sunset Orange", "برتقالي الغروب", true, 0xFF2B1913, 0xFF46271B, 0xFF382017, 0xFFFF9A55, 0xFFFFF4E9, isNew = true),
        KeyboardThemePreset("soft_sky", "SoftSky", "سماء هادئة", false, 0xFFE4F0F8, 0xFFF9FCFF, 0xFFCCDFED, 0xFF347BA8, 0xFF172B3B, isNew = true),
        KeyboardThemePreset("unicorn_nova", "UnicornNova", "نجمة قوس قزح", false, 0xFFF0E8F7, 0xFFFFFAFF, 0xFFE2D3F0, 0xFF7749AD, 0xFF291B37, isNew = true),
        KeyboardThemePreset("minimal_red", "Minimal Red", "أحمر بسيط", false, 0xFFF0E9E8, 0xFFFFFBFA, 0xFFE2D4D2, 0xFFB52B36, 0xFF2C1A1B, isNew = true),
        KeyboardThemePreset("flower_aura", "FlowerAura", "هالة الزهور", false, 0xFFF2E9EF, 0xFFFFFBFD, 0xFFE6D5E0, 0xFF9B4C78, 0xFF321C29, isNew = true),
        KeyboardThemePreset("bear_forest", "Bear Forest", "غابة هادئة", true, 0xFF17231D, 0xFF27372C, 0xFF1F2D24, 0xFF9BC77C, 0xFFF1F8ED, isNew = true),
        KeyboardThemePreset("elite_board", "EliteBoard", "لوحة النخبة", true, 0xFF111923, 0xFF202D3B, 0xFF192431, 0xFFD6B36A, 0xFFF8F5EC, isNew = true),
        KeyboardThemePreset("heart_glow", "HeartGlow", "وهج القلب", true, 0xFF29131E, 0xFF421D30, 0xFF351827, 0xFFFF759D, 0xFFFFF2F6, isNew = true),
        KeyboardThemePreset("bunny_bloom", "BunnyBloom", "زهر الأرنب", false, 0xFFF4EAF0, 0xFFFFFCFE, 0xFFEAD7E2, 0xFF98617D, 0xFF30202A, isNew = true),
        KeyboardThemePreset("reactor_glow", "ReactorGlow", "وهج المفاعل", true, 0xFF101C1B, 0xFF1C302D, 0xFF162623, 0xFF63E6A5, 0xFFEFFFF6, isNew = true),
        KeyboardThemePreset("cotton_keys", "CottonKeys", "قطن ناعم", false, 0xFFF0EDF0, 0xFFFFFCFF, 0xFFE0DAE2, 0xFF79627F, 0xFF29232D, isNew = true),
        KeyboardThemePreset("meow_mist", "MeowMist", "ضباب القطط", false, 0xFFE9EAF2, 0xFFFCFCFF, 0xFFD6D9E6, 0xFF5B6599, 0xFF202338, isNew = true),
        KeyboardThemePreset("leaf_flow", "LeafFlow", "انسياب الأوراق", false, 0xFFE8EFE7, 0xFFFAFCF8, 0xFFD2E0D0, 0xFF3D7952, 0xFF1D2D21, isNew = true),
        KeyboardThemePreset("halloween_keys", "Halloween Keys", "مفاتيح الهالوين", true, 0xFF21152C, 0xFF35223F, 0xFF2A1B34, 0xFFFF9D3D, 0xFFFFF5E9, isNew = true),
        KeyboardThemePreset("piano_keys", "Piano Keys", "مفاتيح البيانو", false, 0xFFE5E5E5, 0xFFFFFFFF, 0xFFCCCCCC, 0xFF30343B, 0xFF17191D, isNew = true),
        KeyboardThemePreset("captain_glow", "CaptainGlow", "وهج القبطان", true, 0xFF102033, 0xFF1B3450, 0xFF152A41, 0xFFFFC857, 0xFFF3F8FF, isNew = true),
        KeyboardThemePreset("cat_verse", "CatVerse", "عالم القطط", true, 0xFF201A2D, 0xFF332940, 0xFF292136, 0xFFCAA6FF, 0xFFF8F2FF, isNew = true),
        KeyboardThemePreset("kitty_nest", "KittyNest", "عش القطط", false, 0xFFF2EAE5, 0xFFFFFBF8, 0xFFE5D7CF, 0xFF9C6958, 0xFF30221D, isNew = true),
        KeyboardThemePreset("dragon_flight", "Dragon Flight", "تحليق التنين", true, 0xFF14221F, 0xFF243832, 0xFF1B2D28, 0xFFFF765F, 0xFFF5FFF9, isNew = true),
        KeyboardThemePreset("rainbow_dream", "RainbowDream", "حلم قوس قزح", false, 0xFFE8EDF5, 0xFFFCFDFF, 0xFFD4DCE9, 0xFF6556B3, 0xFF202137, isNew = true, keyPaletteArgb = listOf(0xFFFF9AA2L, 0xFFFFC98BL, 0xFFFFE895L, 0xFFA9DFA4L, 0xFF8CD7E8L, 0xFF9DB9F2L, 0xFFC5A6EAL)),
        KeyboardThemePreset("melody_pop", "MelodyPop", "لحن نابض", false, 0xFFF3E8F0, 0xFFFFFBFE, 0xFFE7D4E2, 0xFF9C477F, 0xFF321C2B, isNew = true, keyPaletteArgb = listOf(0xFFFF6E74L, 0xFFFFB44BL, 0xFFE8DF45L, 0xFF61D276L, 0xFF43C7D7L, 0xFF607BE5L, 0xFFC063D6L)),
        KeyboardThemePreset("happy_board", "HappyBoard", "لوحة مرحة", false, 0xFFE8F4EB, 0xFFFFFCF3, 0xFFD7E8DC, 0xFFEC719D, 0xFF28372D, isNew = true, keyPaletteArgb = listOf(0xFFFFF7E8L, 0xFFF3F7EAL, 0xFFE3F2EAL, 0xFFDFF0F5L, 0xFFFFE5EDL)),
        KeyboardThemePreset("music_nova", "MusicNova Keys", "موسيقى نوفا", false, 0xFFF4E8F3, 0xFFFFFCFF, 0xFFE4D5E7, 0xFFAA4DB5, 0xFF2A1930, isNew = true, keyPaletteArgb = listOf(0xFFFF7386L, 0xFFFFB65CL, 0xFF86CC67L, 0xFF55C7C3L, 0xFF608EEDL, 0xFFB167D9L)),
        KeyboardThemePreset("leaf_board", "LeafBoard", "لوحة الأوراق", false, 0xFFEAF0DD, 0xFFFFFDF6, 0xFFD6E1C1, 0xFF577B32, 0xFF263019, isNew = true),
        KeyboardThemePreset("candy_pop_keys", "CandyPop Keys", "مفاتيح الحلوى", false, 0xFFF0EFF3, 0xFFFFFCFF, 0xFFDDD8E8, 0xFFE748AE, 0xFF2B2433, isNew = true, keyPaletteArgb = listOf(0xFFFFA2B7L, 0xFFFFCA73L, 0xFFB7DB78L, 0xFF76D3DCL, 0xFF86A8F2L, 0xFFC18BE8L))
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
                    toolbarBackground = Color(preset.toolbarArgb ?: preset.backgroundArgb),
                    keyPalette = preset.keyPaletteArgb.map { Color(it) }
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
                    toolbarBackground = Color(preset.toolbarArgb ?: preset.backgroundArgb),
                    keyPalette = preset.keyPaletteArgb.map { Color(it) }
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
    val toolbarBackground: Color = Color.Transparent,
    val keyPalette: List<Color> = emptyList()
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
    val canRedo: Boolean = false,
    val targetLanguage: TargetTranslateLanguage? = null
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
    val localIconPath: String? = null,
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
