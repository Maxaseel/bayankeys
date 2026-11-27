package com.example.ui

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BayanApplication
import com.example.data.local.ClipboardEntity
import com.example.data.local.CustomProviderEntity
import com.example.data.local.LearnedWordEntity
import com.example.data.local.SettingsRepository
import com.example.data.model.AiProviderConfig
import com.example.data.model.AiProviderType
import com.example.data.model.BotIconOption
import com.example.data.model.BotKnowledgeSource
import com.example.data.model.BuiltinBotAction
import com.example.data.model.CustomBotItem
import com.example.data.model.GradualPasteConfig
import com.example.data.model.KeyButtonShape
import com.example.data.model.KeyboardFontStyle
import com.example.data.model.KeyboardLanguage
import com.example.data.model.KeyboardSizeConfig
import com.example.data.model.KeyboardThemeConfig
import com.example.data.model.KeyboardThemePreset
import com.example.data.model.PasswordEntryItem
import com.example.data.model.PasswordServiceBrand
import com.example.data.model.ProofreadDiffReview
import com.example.data.model.TargetTranslateLanguage
import com.example.data.model.ThemeMode
import com.example.data.model.ToolbarVisibilityConfig
import com.example.data.model.TranslationProviderType
import com.example.data.remote.AiTranslationService
import com.example.data.repository.BayanRepository
import com.example.data.repository.SuggestionCandidate
import com.example.ime.BayanInputMethodService
import com.example.ime.GradualPasteEngine
import com.example.ime.KeyboardLayer
import com.example.ime.KeyboardPanel
import com.example.ime.KeyboardUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ConnectionTestState {
    data object Idle : ConnectionTestState
    data object Testing : ConnectionTestState
    data class Success(val reply: String, val latencyMs: Long) : ConnectionTestState
    data class Error(val message: String) : ConnectionTestState
}

sealed interface FetchedModelsState {
    data object Idle : FetchedModelsState
    data object Loading : FetchedModelsState
    data class Success(val models: List<String>) : FetchedModelsState
    data class Info(val message: String) : FetchedModelsState
    data class Error(val message: String) : FetchedModelsState
}

data class ImeStatus(
    val isEnabled: Boolean = false,
    val isDefault: Boolean = false
)

class BayanViewModel(
    private val settings: SettingsRepository,
    private val repository: BayanRepository,
    private val aiService: AiTranslationService
) : ViewModel() {

    val appUiLanguage: StateFlow<String> = settings.appUiLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ar")

    val themeConfig: StateFlow<KeyboardThemeConfig> = settings.themeConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KeyboardThemeConfig())

    val sizeConfig: StateFlow<KeyboardSizeConfig> = settings.sizeConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KeyboardSizeConfig())

    val toolbarVisibility: StateFlow<ToolbarVisibilityConfig> = settings.toolbarVisibilityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ToolbarVisibilityConfig())

    val gradualPasteConfig: StateFlow<GradualPasteConfig> = settings.gradualPasteConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GradualPasteConfig())

    val aiConfig: StateFlow<AiProviderConfig> = settings.aiConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiProviderConfig())

    val enabledLanguages: StateFlow<List<KeyboardLanguage>> = settings.enabledLanguagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(KeyboardLanguage.ARABIC, KeyboardLanguage.ENGLISH))

    val activeLanguage: StateFlow<KeyboardLanguage> = settings.activeLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KeyboardLanguage.ARABIC)

    val clipboardItems: StateFlow<List<ClipboardEntity>> = repository.clipboardItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val learnedWords: StateFlow<List<LearnedWordEntity>> = repository.learnedWords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val learnedWordsCount: StateFlow<Int> = repository.learnedWordsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val customProviders: StateFlow<List<CustomProviderEntity>> = repository.customProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customBots: StateFlow<List<CustomBotItem>> = repository.customBots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val passwordEntries: StateFlow<List<PasswordEntryItem>> = repository.passwordEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _botTestBusy = MutableStateFlow(false)
    val botTestBusy: StateFlow<Boolean> = _botTestBusy.asStateFlow()

    private val _botTestResult = MutableStateFlow<Result<String>?>(null)
    val botTestResult: StateFlow<Result<String>?> = _botTestResult.asStateFlow()

    private val _imeStatus = MutableStateFlow(ImeStatus())
    val imeStatus: StateFlow<ImeStatus> = _imeStatus.asStateFlow()

    private val _playgroundText = MutableStateFlow("اهلا بكم في لوحه المفاتيح الذكيه هذا نص للتجربه والتصحيح.")
    val playgroundText: StateFlow<String> = _playgroundText.asStateFlow()

    private val _playgroundUndoText = MutableStateFlow<String?>(null)
    val playgroundUndoText: StateFlow<String?> = _playgroundUndoText.asStateFlow()

    private val _playgroundBusy = MutableStateFlow(false)
    val playgroundBusy: StateFlow<Boolean> = _playgroundBusy.asStateFlow()

    private val _playgroundStatus = MutableStateFlow<Pair<String, Boolean>?>(null)
    val playgroundStatus: StateFlow<Pair<String, Boolean>?> = _playgroundStatus.asStateFlow()

    private val _playgroundDiffReview = MutableStateFlow<ProofreadDiffReview?>(null)
    val playgroundDiffReview: StateFlow<ProofreadDiffReview?> = _playgroundDiffReview.asStateFlow()

    private var playgroundGradualJob: Job? = null

    private val _aiTestState = MutableStateFlow<ConnectionTestState>(ConnectionTestState.Idle)
    val aiTestState: StateFlow<ConnectionTestState> = _aiTestState.asStateFlow()

    private val _translationTestState = MutableStateFlow<ConnectionTestState>(ConnectionTestState.Idle)
    val translationTestState: StateFlow<ConnectionTestState> = _translationTestState.asStateFlow()

    private val _fetchedModelsState = MutableStateFlow<FetchedModelsState>(FetchedModelsState.Idle)
    val fetchedModelsState: StateFlow<FetchedModelsState> = _fetchedModelsState.asStateFlow()

    private val _previewKeyboardState = MutableStateFlow(
        KeyboardUiState(
            activeLanguage = KeyboardLanguage.ARABIC,
            suggestions = listOf(
                SuggestionCandidate("مرحبا"),
                SuggestionCandidate("السلام"),
                SuggestionCandidate("شكرا")
            )
        )
    )
    val previewKeyboardState: StateFlow<KeyboardUiState> = _previewKeyboardState.asStateFlow()

    fun refreshImeStatus(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val enabledList = imm?.enabledInputMethodList.orEmpty()
        val myPackage = context.packageName
        val myServiceName = BayanInputMethodService::class.java.name
        val isEnabled = enabledList.any { it.packageName == myPackage }
        val defaultIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD).orEmpty()
        val isDefault = defaultIme.contains(myPackage) || defaultIme.contains(myServiceName)
        _imeStatus.value = ImeStatus(isEnabled = isEnabled, isDefault = isDefault)
    }

    fun toggleAppUiLanguage() {
        viewModelScope.launch {
            val next = if (appUiLanguage.value == "ar") "en" else "ar"
            settings.setAppUiLanguage(next)
        }
    }

    // Playground Actions
    fun updatePlaygroundText(text: String) {
        playgroundGradualJob?.cancel()
        playgroundGradualJob = null
        _playgroundText.value = text
        if (_playgroundDiffReview.value != null) {
            _playgroundDiffReview.value = null
        }
    }

    fun proofreadPlayground() {
        val text = _playgroundText.value
        if (text.isBlank()) return
        playgroundGradualJob?.cancel()
        playgroundGradualJob = null
        _playgroundBusy.value = true
        _playgroundStatus.value = null
        _playgroundDiffReview.value = null
        viewModelScope.launch {
            val res = aiService.proofreadAndCorrect(text, aiConfig.value)
            _playgroundBusy.value = false
            res.fold(
                onSuccess = { corrected ->
                    _playgroundDiffReview.value = ProofreadDiffReview(
                        originalText = text,
                        correctedText = corrected,
                        replaceWholeField = true
                    )
                    _playgroundStatus.value = Pair(
                        if (appUiLanguage.value == "ar") "تم فحص النص — راجع التعديلات بالأسفل ثم اختر نسخ أو تطبيق"
                        else "Proofread ready — review changes below and tap Copy or Apply",
                        false
                    )
                },
                onFailure = { err ->
                    _playgroundStatus.value = Pair(err.message ?: "Error", true)
                }
            )
        }
    }

    fun applyPlaygroundDiff() {
        val review = _playgroundDiffReview.value ?: return
        playgroundGradualJob?.cancel()
        playgroundGradualJob = null
        _playgroundUndoText.value = _playgroundText.value
        _playgroundText.value = review.correctedText
        _playgroundDiffReview.value = null
        _playgroundStatus.value = Pair(
            if (appUiLanguage.value == "ar") "تم تطبيق التصحيح على النص"
            else "Corrected text applied",
            false
        )
    }

    fun applyPlaygroundDiffGradually() {
        val review = _playgroundDiffReview.value ?: return
        playgroundGradualJob?.cancel()
        _playgroundUndoText.value = _playgroundText.value
        _playgroundDiffReview.value = null
        val isAr = appUiLanguage.value == "ar"
        _playgroundStatus.value = Pair(
            if (isAr) "⚡ تم تفعيل اللصق التدريجي • جاري الاستبدال..."
            else "⚡ Gradual paste active • Replacing...",
            false
        )

        playgroundGradualJob = viewModelScope.launch {
            try {
                val savedCfg = settings.gradualPasteConfigFlow.first()
                val activeCfg = if (savedCfg.enabled) savedCfg else savedCfg.copy(enabled = true)
                if (!savedCfg.enabled) {
                    settings.updateGradualPasteConfig { it.copy(enabled = true) }
                }
                var cleared = false
                val completed = GradualPasteEngine.stream(review.correctedText, activeCfg) { chunk ->
                    if (!cleared) {
                        _playgroundText.value = chunk
                        cleared = true
                    } else {
                        _playgroundText.value += chunk
                    }
                    true
                }
                if (completed) {
                    _playgroundStatus.value = Pair(
                        if (isAr) "تم استبدال النص باللصق التدريجي ⚡"
                        else "Text replaced via gradual paste ⚡",
                        false
                    )
                }
            } catch (_: CancellationException) {
                // Cancelled by subsequent edit or instant apply
            }
        }
    }

    fun onPlaygroundDiffCopied(copiedText: String) {
        viewModelScope.launch { repository.captureClipboardText(copiedText) }
        _playgroundDiffReview.value = null
        _playgroundStatus.value = Pair(
            if (appUiLanguage.value == "ar") "تم نسخ النص المصحح إلى الحافظة"
            else "Corrected text copied to clipboard",
            false
        )
    }

    fun dismissPlaygroundDiff() {
        _playgroundDiffReview.value = null
    }

    fun translatePlayground(target: TargetTranslateLanguage) {
        val text = _playgroundText.value
        if (text.isBlank()) return
        _playgroundUndoText.value = text
        _playgroundBusy.value = true
        _playgroundStatus.value = null
        viewModelScope.launch {
            val res = aiService.translateText(text, target, aiConfig.value)
            _playgroundBusy.value = false
            res.fold(
                onSuccess = { translated ->
                    _playgroundText.value = translated
                    _playgroundStatus.value = Pair(
                        if (appUiLanguage.value == "ar") "تمت الترجمة إلى ${target.labelAr}"
                        else "Translated to ${target.labelEn}",
                        false
                    )
                },
                onFailure = { err ->
                    _playgroundStatus.value = Pair(err.message ?: "Error", true)
                }
            )
        }
    }

    fun undoPlayground() {
        val prev = _playgroundUndoText.value ?: return
        _playgroundText.value = prev
        _playgroundUndoText.value = null
    }

    // AI & Translation Config
    fun selectAiProvider(provider: AiProviderType) {
        _aiTestState.value = ConnectionTestState.Idle
        _fetchedModelsState.value = FetchedModelsState.Idle
        viewModelScope.launch { settings.selectAiProvider(provider) }
    }

    fun saveAiCredentials(provider: AiProviderType, apiKey: String, model: String, customBaseUrl: String?) {
        viewModelScope.launch {
            settings.updateAiCredentials(provider, apiKey, model, customBaseUrl)
        }
    }

    fun fetchModels(provider: AiProviderType, apiKey: String, customBaseUrl: String) {
        val isArabic = appUiLanguage.value == "ar"
        when {
            provider == AiProviderType.GROKCLOUD -> {
                _fetchedModelsState.value = FetchedModelsState.Success(provider.suggestedModels)
                return
            }
            provider == AiProviderType.EXAM1 ||
                (provider == AiProviderType.CUSTOM && provider.isApiKeyOptional(customBaseUrl)) -> {
                val defaultModel = if (provider == AiProviderType.EXAM1) provider.defaultModel else AiProviderType.EXAM1.defaultModel
                _fetchedModelsState.value = FetchedModelsState.Info(
                    if (isArabic) "هذا المزوّد لا يدعم جلب قائمة النماذج تلقائيًا؛ سيُستخدم النموذج الافتراضي $defaultModel."
                    else "This provider does not support automatic model listing; the default model $defaultModel will be used."
                )
                return
            }
        }
        _fetchedModelsState.value = FetchedModelsState.Loading
        viewModelScope.launch {
            val result = aiService.fetchAvailableModels(provider, apiKey, customBaseUrl)
            _fetchedModelsState.value = result.fold(
                onSuccess = { models -> FetchedModelsState.Success(models) },
                onFailure = { err -> FetchedModelsState.Error(err.message ?: "Failed to fetch models") }
            )
        }
    }

    fun resetFetchedModelsState() {
        _fetchedModelsState.value = FetchedModelsState.Idle
    }

    fun saveAndActivateCustomProvider(
        id: Long = 0,
        name: String,
        baseUrl: String,
        apiKey: String,
        selectedModel: String,
        fetchedModels: List<String>
    ) {
        viewModelScope.launch {
            val saved = repository.saveCustomProvider(
                id = id,
                name = name,
                baseUrl = baseUrl,
                apiKey = apiKey,
                selectedModel = selectedModel,
                fetchedModels = fetchedModels
            )
            settings.activateCustomProvider(
                customId = saved.id,
                name = saved.name,
                baseUrl = saved.baseUrl,
                apiKey = saved.apiKey,
                model = saved.selectedModel
            )
        }
    }

    fun activateSavedCustomProvider(entity: CustomProviderEntity) {
        _aiTestState.value = ConnectionTestState.Idle
        val savedModels = entity.fetchedModelsList()
        _fetchedModelsState.value = if (savedModels.isNotEmpty()) {
            FetchedModelsState.Success(savedModels)
        } else {
            FetchedModelsState.Idle
        }
        viewModelScope.launch {
            settings.activateCustomProvider(
                customId = entity.id,
                name = entity.name,
                baseUrl = entity.baseUrl,
                apiKey = entity.apiKey,
                model = entity.selectedModel
            )
        }
    }

    fun deleteCustomProvider(entity: CustomProviderEntity) {
        viewModelScope.launch {
            repository.deleteCustomProvider(entity.id)
            if (aiConfig.value.activeCustomProviderId == entity.id) {
                settings.selectAiProvider(AiProviderType.GEMINI)
            }
        }
    }

    fun testAiConnection(provider: AiProviderType, apiKey: String, model: String, customBaseUrl: String?) {
        _aiTestState.value = ConnectionTestState.Testing
        viewModelScope.launch {
            if (provider != AiProviderType.CUSTOM) {
                settings.updateAiCredentials(provider, apiKey, model, customBaseUrl)
            }
            val testConfig = aiConfig.value.copy(
                aiProvider = provider,
                aiApiKey = apiKey.trim(),
                aiModel = model.trim().ifBlank { provider.defaultModel },
                aiCustomBaseUrl = customBaseUrl?.trim().orEmpty().ifBlank { provider.defaultBaseUrl }
            )
            val result = aiService.testAiConnection(testConfig)
            _aiTestState.value = result.fold(
                onSuccess = { (reply, ms) -> ConnectionTestState.Success(reply, ms) },
                onFailure = { err -> ConnectionTestState.Error(err.message ?: "Connection failed") }
            )
        }
    }

    fun saveTranslationConfig(
        provider: TranslationProviderType,
        apiKey: String,
        endpoint: String,
        target: TargetTranslateLanguage
    ) {
        viewModelScope.launch {
            settings.updateTranslationConfig(provider, apiKey, endpoint, target)
        }
    }

    fun testTranslationConnection(
        provider: TranslationProviderType,
        apiKey: String,
        endpoint: String,
        target: TargetTranslateLanguage
    ) {
        _translationTestState.value = ConnectionTestState.Testing
        viewModelScope.launch {
            settings.updateTranslationConfig(provider, apiKey, endpoint, target)
            val testConfig = aiConfig.value.copy(
                translationProvider = provider,
                translationApiKey = apiKey.trim(),
                translationEndpoint = endpoint.trim().ifBlank { provider.defaultEndpoint },
                defaultTargetLanguage = target
            )
            val result = aiService.testTranslationConnection(testConfig)
            _translationTestState.value = result.fold(
                onSuccess = { (reply, ms) -> ConnectionTestState.Success(reply, ms) },
                onFailure = { err -> ConnectionTestState.Error(err.message ?: "Translation failed") }
            )
        }
    }

    // Theme & Sizing
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun applyThemePreset(preset: KeyboardThemePreset) {
        viewModelScope.launch { settings.applyThemePreset(preset) }
    }

    fun updateCustomColors(
        backgroundArgb: Long? = null,
        keyArgb: Long? = null,
        specialKeyArgb: Long? = null,
        accentArgb: Long? = null,
        textArgb: Long? = null,
        cornerRadiusDp: Float? = null,
        showBorders: Boolean? = null
    ) {
        viewModelScope.launch {
            settings.updateCustomThemeColors(
                backgroundArgb, keyArgb, specialKeyArgb, accentArgb, textArgb, cornerRadiusDp, showBorders
            )
        }
    }

    fun updateSizeConfig(transform: (KeyboardSizeConfig) -> KeyboardSizeConfig) {
        viewModelScope.launch { settings.updateSizeConfig(transform) }
    }

    fun updateToolbarVisibility(transform: (ToolbarVisibilityConfig) -> ToolbarVisibilityConfig) {
        viewModelScope.launch { settings.updateToolbarVisibility(transform) }
    }

    fun setKeyShape(shape: KeyButtonShape) {
        viewModelScope.launch { settings.setKeyShape(shape) }
    }

    fun setKeyboardFontStyle(style: KeyboardFontStyle) {
        viewModelScope.launch { settings.setKeyboardFontStyle(style) }
    }

    fun updateGradualPasteConfig(transform: (GradualPasteConfig) -> GradualPasteConfig) {
        viewModelScope.launch { settings.updateGradualPasteConfig(transform) }
    }

    fun toggleLanguageEnabled(lang: KeyboardLanguage) {
        viewModelScope.launch { settings.toggleLanguageEnabled(lang) }
    }

    // Preview keyboard interactions
    fun onPreviewSwitchLanguage() {
        val langs = enabledLanguages.value
        val cur = _previewKeyboardState.value.activeLanguage
        val next = langs[(langs.indexOf(cur) + 1).mod(langs.size)]
        _previewKeyboardState.value = _previewKeyboardState.value.copy(
            activeLanguage = next,
            suggestions = if (next == KeyboardLanguage.ARABIC) {
                listOf(SuggestionCandidate("مرحبا"), SuggestionCandidate("السلام"), SuggestionCandidate("شكرا"))
            } else {
                listOf(SuggestionCandidate("Hello"), SuggestionCandidate("Thanks"), SuggestionCandidate("Great"))
            }
        )
    }

    fun onPreviewShift() {
        _previewKeyboardState.value = _previewKeyboardState.value.copy(
            isShifted = !_previewKeyboardState.value.isShifted
        )
    }

    fun onPreviewSymbols() {
        val next = when (_previewKeyboardState.value.layer) {
            KeyboardLayer.LETTERS -> KeyboardLayer.SYMBOLS_1
            KeyboardLayer.SYMBOLS_1 -> KeyboardLayer.SYMBOLS_2
            KeyboardLayer.SYMBOLS_2 -> KeyboardLayer.LETTERS
            KeyboardLayer.NUMPAD -> KeyboardLayer.LETTERS
        }
        _previewKeyboardState.value = _previewKeyboardState.value.copy(layer = next)
    }

    fun onPreviewPanel(panel: KeyboardPanel) {
        _previewKeyboardState.value = _previewKeyboardState.value.copy(panel = panel)
    }

    // Clipboard & Dictionary
    fun addManualClip(text: String, pinned: Boolean) {
        viewModelScope.launch { repository.addManualClip(text, pinned) }
    }

    fun togglePinClipboard(item: ClipboardEntity) {
        viewModelScope.launch { repository.togglePinClipboard(item) }
    }

    fun movePinnedClipboard(item: ClipboardEntity, direction: Int) {
        viewModelScope.launch { repository.movePinnedClipboard(item, direction) }
    }

    fun deleteClipboard(id: Long) {
        viewModelScope.launch { repository.deleteClipboard(id) }
    }

    fun clearUnpinnedClipboard() {
        viewModelScope.launch { repository.clearUnpinnedClipboard() }
    }

    fun addCustomWord(word: String, lang: KeyboardLanguage) {
        viewModelScope.launch { repository.addCustomWord(word, lang) }
    }

    fun deleteLearnedWord(word: String, languageCode: String) {
        viewModelScope.launch { repository.deleteLearnedWord(word, languageCode) }
    }

    fun resetLearnedDictionary() {
        viewModelScope.launch {
            repository.resetLearnedDictionary()
            repository.ensureSeeded()
        }
    }

    suspend fun exportBackup(): String = repository.exportBackup()

    suspend fun importBackup(rawJson: String): Result<Int> = repository.importBackup(rawJson)

    // Password Manager
    fun savePasswordEntry(
        id: Long = 0,
        siteName: String,
        siteUrl: String,
        username: String,
        password: String,
        brand: PasswordServiceBrand? = null,
        localIconPath: String? = null
    ) {
        viewModelScope.launch {
            repository.savePasswordEntry(
                id = id,
                siteName = siteName,
                siteUrl = siteUrl,
                username = username,
                password = password,
                brand = brand,
                localIconPath = localIconPath
            )
        }
    }

    fun deletePasswordEntry(id: Long) {
        viewModelScope.launch { repository.deletePasswordEntry(id) }
    }

    // Custom AI Bots
    fun saveCustomBot(
        id: Long = 0,
        name: String,
        prompt: String,
        iconOption: BotIconOption,
        accentArgb: Long = iconOption.accentArgb,
        knowledgeSources: List<BotKnowledgeSource> = emptyList(),
        isPinnedToToolbar: Boolean = true
    ) {
        viewModelScope.launch {
            repository.saveCustomBot(
                id = id,
                name = name,
                prompt = prompt,
                iconOption = iconOption,
                accentArgb = accentArgb,
                knowledgeSources = knowledgeSources,
                isPinnedToToolbar = isPinnedToToolbar
            )
        }
    }

    fun toggleBotPinnedToToolbar(bot: CustomBotItem) {
        viewModelScope.launch { repository.toggleBotPinnedToToolbar(bot) }
    }

    fun deleteCustomBot(id: Long) {
        viewModelScope.launch { repository.deleteCustomBot(id) }
    }

    fun testCustomBot(bot: CustomBotItem, inputText: String) {
        _botTestBusy.value = true
        _botTestResult.value = null
        viewModelScope.launch {
            val res = aiService.runCustomBot(inputText, bot, aiConfig.value)
            _botTestBusy.value = false
            _botTestResult.value = res
        }
    }

    fun clearBotTestResult() {
        _botTestResult.value = null
    }

    fun runBuiltinActionPlayground(action: BuiltinBotAction) {
        val text = _playgroundText.value
        if (action == BuiltinBotAction.PROOFREAD) {
            proofreadPlayground()
            return
        }
        if (action == BuiltinBotAction.TRANSLATE) {
            translatePlayground(aiConfig.value.defaultTargetLanguage)
            return
        }
        if (text.isBlank()) return
        playgroundGradualJob?.cancel()
        playgroundGradualJob = null
        _playgroundBusy.value = true
        _playgroundStatus.value = null
        _playgroundDiffReview.value = null
        viewModelScope.launch {
            val res = aiService.runQuickBotAction(text, action, aiConfig.value.defaultTargetLanguage, aiConfig.value)
            _playgroundBusy.value = false
            res.fold(
                onSuccess = { output ->
                    _playgroundDiffReview.value = ProofreadDiffReview(
                        originalText = text,
                        correctedText = output,
                        replaceWholeField = true
                    )
                    _playgroundStatus.value = Pair(
                        if (appUiLanguage.value == "ar") "تم تنفيذ «${action.labelAr}» — اختر نسخ أو تطبيق"
                        else "${action.labelEn} ready — review and tap Copy or Apply",
                        false
                    )
                },
                onFailure = { err ->
                    _playgroundStatus.value = Pair(err.message ?: "Error", true)
                }
            )
        }
    }

    fun runCustomBotPlayground(bot: CustomBotItem) {
        val text = _playgroundText.value
        playgroundGradualJob?.cancel()
        playgroundGradualJob = null
        _playgroundBusy.value = true
        _playgroundStatus.value = null
        _playgroundDiffReview.value = null
        viewModelScope.launch {
            val res = aiService.runCustomBot(text, bot, aiConfig.value)
            _playgroundBusy.value = false
            res.fold(
                onSuccess = { output ->
                    _playgroundDiffReview.value = ProofreadDiffReview(
                        originalText = text,
                        correctedText = output,
                        replaceWholeField = true
                    )
                    _playgroundStatus.value = Pair(
                        if (appUiLanguage.value == "ar") "رد «${bot.name}» جاهز — اختر نسخ أو تطبيق"
                        else "${bot.name} ready — review and tap Copy or Apply",
                        false
                    )
                },
                onFailure = { err ->
                    _playgroundStatus.value = Pair(err.message ?: "Error", true)
                }
            )
        }
    }

    class Factory(private val app: BayanApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BayanViewModel(
                app.settingsRepository,
                app.bayanRepository,
                app.aiTranslationService
            ) as T
        }
    }
}
