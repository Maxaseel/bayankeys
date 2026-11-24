package com.example.ime

import android.Manifest
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.BayanApplication
import com.example.MainActivity
import com.example.data.local.ClipboardEntity
import com.example.data.model.AiProviderConfig
import com.example.data.model.BuiltinBotAction
import com.example.data.model.CustomBotItem
import com.example.data.model.GradualPasteConfig
import com.example.data.model.KeyButtonShape
import com.example.data.model.KeyboardLanguage
import com.example.data.model.KeyboardSizeConfig
import com.example.data.model.KeyboardThemeConfig
import com.example.data.model.OneHandedMode
import com.example.data.model.ProofreadDiffReview
import com.example.data.model.QuickInputTool
import com.example.data.model.TargetTranslateLanguage
import com.example.data.model.ToolbarVisibilityConfig
import com.example.data.repository.SuggestionCandidate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BayanInputMethodService :
    InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val app: BayanApplication by lazy { application as BayanApplication }
    private val settings by lazy { app.settingsRepository }
    private val repository by lazy { app.bayanRepository }
    private val aiService by lazy { app.aiTranslationService }

    private val keyboardState = MutableStateFlow(KeyboardUiState())
    private var enabledLanguages: List<KeyboardLanguage> = listOf(KeyboardLanguage.ARABIC, KeyboardLanguage.ENGLISH)
    private var sizeConfig = KeyboardSizeConfig()
    private var aiConfig = AiProviderConfig()
    private var gradualPasteConfig = GradualPasteConfig()

    private val composingWord = StringBuilder()
    private var previousWord: String? = null
    private var lastShiftTapTime = 0L

    private var undoBackupFullText: String? = null
    private var redoBackupFullText: String? = null
    private var retryCurrentAiOperation: (suspend () -> Result<String>)? = null
    private var aiSourceWasWholeField = true
    private var aiSourceSelectionStart = 0
    private var aiSourceSelectionEnd = 0
    private var lastSeenClipSignature: String? = null
    private var statusJob: Job? = null
    private var suggestionJob: Job? = null
    private var gradualPasteJob: Job? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        captureSystemClipboard()
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.addPrimaryClipChangedListener(clipboardListener)
        captureSystemClipboard()

        serviceScope.launch {
            settings.enabledLanguagesFlow.collectLatest { enabledLanguages = it }
        }
        serviceScope.launch {
            settings.sizeConfigFlow.collectLatest { sizeConfig = it }
        }
        serviceScope.launch {
            settings.aiConfigFlow.collectLatest { cfg ->
                aiConfig = cfg
                keyboardState.value = keyboardState.value.copy(targetTranslateLang = cfg.defaultTargetLanguage)
            }
        }
        serviceScope.launch {
            settings.activeLanguageFlow.collectLatest { lang ->
                keyboardState.value = keyboardState.value.copy(activeLanguage = lang)
                refreshSuggestions()
            }
        }
        serviceScope.launch {
            settings.gradualPasteConfigFlow.collectLatest { gradualPasteConfig = it }
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        window?.window?.decorView?.let { decor ->
            decor.setViewTreeLifecycleOwner(this)
            decor.setViewTreeViewModelStoreOwner(this)
            decor.setViewTreeSavedStateRegistryOwner(this)
        }

        return ComposeView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by keyboardState.collectAsState()
                val themeConfig by settings.themeConfigFlow.collectAsState(initial = KeyboardThemeConfig())
                val currentSize by settings.sizeConfigFlow.collectAsState(initial = KeyboardSizeConfig())
                val toolbarConfig by settings.toolbarVisibilityFlow.collectAsState(initial = ToolbarVisibilityConfig())
                val currentGradualConfig by settings.gradualPasteConfigFlow.collectAsState(initial = GradualPasteConfig())
                val clips by repository.clipboardItems.collectAsState(initial = emptyList())
                val passwordEntries by repository.passwordEntries.collectAsState(initial = emptyList())
                val customBots by repository.customBots.collectAsState(initial = emptyList())

                BayanKeyboardView(
                    state = state,
                    themeConfig = themeConfig,
                    sizeConfig = currentSize,
                    toolbarConfig = toolbarConfig,
                    gradualPasteConfig = currentGradualConfig,
                    clipboardItems = clips,
                    passwordEntries = passwordEntries,
                    customBots = customBots,
                    isPreviewMode = false,
                    onKeyText = ::handleTextInput,
                    onBackspace = ::handleBackspace,
                    onShiftToggle = ::handleShiftToggle,
                    onSymbolsToggle = ::handleSymbolsToggle,
                    onSwitchLanguage = ::cycleLanguage,
                    onEnter = ::handleEnter,
                    onSuggestionClick = ::applySuggestion,
                    onProofreadClick = ::handleAiProofread,
                    onRunCustomBot = ::handleRunCustomBot,
                    onRunBuiltinBotAction = ::handleRunBuiltinBotAction,
                    onOpenCreateBot = ::openCreateBotInApp,
                    onOpenBotsManager = ::openBotsManagerInApp,
                    onVoiceClick = ::handleVoiceClick,
                    onApplyProofreadDiff = ::handleApplyProofreadDiff,
                    onApplyProofreadDiffGradually = ::handleApplyProofreadDiffGradually,
                    onCopyProofreadDiff = ::handleCopyProofreadDiff,
                    onUndoAppliedReview = ::handleReviewUndo,
                    onRedoAppliedReview = ::handleReviewRedo,
                    onRegenerateReview = ::handleRegenerateReview,
                    onDismissProofreadDiff = ::handleDismissProofreadDiff,
                    onUpdateToolbarConfig = { transform ->
                        serviceScope.launch { settings.updateToolbarVisibility(transform) }
                    },
                    onUpdateSizeConfig = { transform ->
                        serviceScope.launch { settings.updateSizeConfig(transform) }
                    },
                    onSetKeyShape = { shape ->
                        serviceScope.launch { settings.setKeyShape(shape) }
                    },
                    onUpdateGradualPasteConfig = { transform ->
                        serviceScope.launch { settings.updateGradualPasteConfig(transform) }
                    },
                    onGradualPaste = ::handleGradualPaste,
                    onStopGradualPaste = ::stopGradualPaste,
                    onOpenGradualPasteSettings = ::openGradualPasteSettings,
                    onQuickToolClick = ::handleQuickTool,
                    onInsertPasswordCredential = { credential ->
                        // Stay on KeyboardPanel.PASSWORDS so user can paste email then password right away
                        dismissRecentClipSuggestion()
                        commitAndResetComposing(credential)
                    },
                    onOpenPasswordVault = ::openPasswordVaultInApp,
                    onTranslateClick = ::handleAiTranslate,
                    onSelectTranslateTarget = ::selectTranslateTarget,
                    onUndoAiClick = ::handleUndoAi,
                    onPanelChange = { panel ->
                        keyboardState.value = keyboardState.value.copy(
                            panel = panel,
                            customizeTabIndex = if (panel == KeyboardPanel.TOOLBAR_CUSTOMIZE) 0 else keyboardState.value.customizeTabIndex
                        )
                    },
                    onClipboardPaste = { text ->
                        dismissRecentClipSuggestion()
                        commitAndResetComposing(text)
                        keyboardState.value = keyboardState.value.copy(panel = KeyboardPanel.KEYS)
                    },
                    onClipboardPinToggle = { clip ->
                        serviceScope.launch { repository.togglePinClipboard(clip) }
                    },
                    onClipboardDelete = { id ->
                        serviceScope.launch { repository.deleteClipboard(id) }
                    },
                    onOpenSettings = ::openCompanionSettings
                )
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        stopGradualPaste()
        composingWord.clear()
        previousWord = null
        captureSystemClipboard()
        val keepPasswordsPanel = restarting && keyboardState.value.panel == KeyboardPanel.PASSWORDS
        keyboardState.value = keyboardState.value.copy(
            panel = if (keepPasswordsPanel) KeyboardPanel.PASSWORDS else KeyboardPanel.KEYS,
            customizeTabIndex = 0,
            layer = KeyboardLayer.LETTERS,
            canUndoAi = false,
            isGradualPasting = false,
            gradualPasteDisabledPrompt = false,
            suggestions = emptyList(),
            statusMessage = null
        )

        if (info != null && sizeConfig.autoSwitchLanguage) {
            val inputClass = info.inputType and InputType.TYPE_MASK_CLASS
            val variation = info.inputType and InputType.TYPE_MASK_VARIATION
            val forceEnglish = variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_URI ||
                variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                inputClass == InputType.TYPE_CLASS_NUMBER

            if (forceEnglish && enabledLanguages.contains(KeyboardLanguage.ENGLISH)) {
                setActiveLanguage(KeyboardLanguage.ENGLISH)
            } else {
                val before = currentInputConnection?.getTextBeforeCursor(80, 0)?.toString().orEmpty()
                if (before.isNotBlank()) {
                    val detected = repository.detectLanguageOfText(
                        before,
                        keyboardState.value.activeLanguage,
                        enabledLanguages
                    )
                    if (detected != keyboardState.value.activeLanguage) {
                        setActiveLanguage(detected)
                    }
                }
            }
        }
        updateAutoCapitalizeState(info)
        refreshSuggestions()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        stopVoiceListening()
        stopGradualPaste()
        commitCurrentWordToDictionary()
        composingWord.clear()
    }

    override fun onDestroy() {
        stopVoiceListening()
        stopGradualPaste()
        speechRecognizer?.destroy()
        speechRecognizer = null
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.removePrimaryClipChangedListener(clipboardListener)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun captureSystemClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = clipboard.primaryClip ?: return
        if (clip.description?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true ||
            clip.description?.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML) == true
        ) {
            val text = clip.getItemAt(0)?.coerceToText(this)?.toString()
            if (!text.isNullOrBlank()) {
                val timestamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    clip.description?.timestamp ?: 0L
                } else 0L
                val signature = "$text|$timestamp"
                if (signature != lastSeenClipSignature) {
                    lastSeenClipSignature = signature
                    keyboardState.value = keyboardState.value.copy(recentClipSuggestion = text)
                    if (!sizeConfig.incognitoMode) {
                        serviceScope.launch {
                            repository.captureClipboardText(text)
                        }
                    }
                }
            }
        }
    }

    private fun dismissRecentClipSuggestion() {
        if (keyboardState.value.recentClipSuggestion != null) {
            keyboardState.value = keyboardState.value.copy(recentClipSuggestion = null)
        }
    }

    private fun handleTextInput(text: String) {
        val ic = currentInputConnection ?: return
        // Once the user proceeds to type/input without tapping the recent copied chip, hide it from suggestions
        dismissRecentClipSuggestion()

        val isBoundary = text.length == 1 && (text[0] == ' ' || text[0] in ".,!?;:،؛؟")
        if (isBoundary) {
            if (text[0] == ' ' && sizeConfig.autoCorrectEnabled && composingWord.isNotEmpty()) {
                val autoCandidate = keyboardState.value.suggestions.firstOrNull {
                    it.isSpellCorrection && !it.text.equals(composingWord.toString(), ignoreCase = true)
                }
                if (autoCandidate != null) {
                    ic.setComposingText(autoCandidate.text, 1)
                    composingWord.clear()
                    composingWord.append(autoCandidate.text)
                }
            }
            commitCurrentWordToDictionary()
            composingWord.clear()
            ic.finishComposingText()
            ic.commitText(text, 1)
            updateAutoCapitalizeState(currentInputEditorInfo)
        } else {
            val shouldAutoSwitch = sizeConfig.autoSwitchLanguage
            val hasArabic = text.any { it in '\u0600'..'\u06FF' }
            if (hasArabic && keyboardState.value.activeLanguage != KeyboardLanguage.ARABIC &&
                shouldAutoSwitch && enabledLanguages.contains(KeyboardLanguage.ARABIC)
            ) {
                setActiveLanguage(KeyboardLanguage.ARABIC)
            }
            composingWord.append(text)
            ic.setComposingText(composingWord.toString(), 1)
            if (keyboardState.value.isShifted && !keyboardState.value.isCapsLocked &&
                keyboardState.value.activeLanguage != KeyboardLanguage.ARABIC
            ) {
                keyboardState.value = keyboardState.value.copy(isShifted = false)
            }
        }
        refreshSuggestions()
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        dismissRecentClipSuggestion()
        if (composingWord.isNotEmpty()) {
            composingWord.deleteCharAt(composingWord.length - 1)
            if (composingWord.isEmpty()) {
                ic.commitText("", 1)
            } else {
                ic.setComposingText(composingWord.toString(), 1)
            }
        } else {
            val selected = ic.getSelectedText(0)
            if (!selected.isNullOrEmpty()) {
                ic.commitText("", 1)
            } else {
                ic.deleteSurroundingText(1, 0)
            }
        }
        refreshSuggestions()
    }

    private fun handleShiftToggle() {
        val current = keyboardState.value
        val now = System.currentTimeMillis()
        if (current.activeLanguage == KeyboardLanguage.ARABIC) {
            keyboardState.value = current.copy(isShifted = !current.isShifted)
            return
        }
        if (current.isCapsLocked) {
            keyboardState.value = current.copy(isShifted = false, isCapsLocked = false)
        } else if (current.isShifted && now - lastShiftTapTime < 350L) {
            keyboardState.value = current.copy(isShifted = true, isCapsLocked = true)
        } else {
            keyboardState.value = current.copy(isShifted = !current.isShifted, isCapsLocked = false)
        }
        lastShiftTapTime = now
    }

    private fun handleSymbolsToggle() {
        val current = keyboardState.value
        val nextLayer = when (current.layer) {
            KeyboardLayer.LETTERS -> KeyboardLayer.SYMBOLS_1
            KeyboardLayer.SYMBOLS_1 -> KeyboardLayer.SYMBOLS_2
            KeyboardLayer.SYMBOLS_2 -> KeyboardLayer.LETTERS
            KeyboardLayer.NUMPAD -> KeyboardLayer.LETTERS
        }
        keyboardState.value = current.copy(layer = nextLayer)
    }

    private fun cycleLanguage(direction: Int) {
        if (enabledLanguages.size <= 1) return
        commitCurrentWordToDictionary()
        composingWord.clear()
        currentInputConnection?.finishComposingText()

        val currentIdx = enabledLanguages.indexOf(keyboardState.value.activeLanguage).coerceAtLeast(0)
        val nextIdx = (currentIdx + direction).mod(enabledLanguages.size)
        val nextLang = enabledLanguages[nextIdx]
        setActiveLanguage(nextLang)
        keyboardState.value = keyboardState.value.copy(
            layer = KeyboardLayer.LETTERS,
            isShifted = false,
            isCapsLocked = false
        )
        refreshSuggestions()
    }

    private fun setActiveLanguage(language: KeyboardLanguage) {
        keyboardState.value = keyboardState.value.copy(activeLanguage = language)
        serviceScope.launch { settings.setActiveLanguage(language) }
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        dismissRecentClipSuggestion()
        commitCurrentWordToDictionary()
        composingWord.clear()
        ic.finishComposingText()

        val imeAction = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        val isMultiLine = (currentInputEditorInfo?.inputType ?: 0) and InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0
        if (!isMultiLine && imeAction != EditorInfo.IME_ACTION_NONE && imeAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(imeAction)
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
        refreshSuggestions()
    }

    private fun applySuggestion(candidate: SuggestionCandidate) {
        val ic = currentInputConnection ?: return
        dismissRecentClipSuggestion()
        ic.beginBatchEdit()
        if (candidate.replacesComposing && composingWord.isNotEmpty()) {
            ic.setComposingText(candidate.text, 1)
            ic.finishComposingText()
            ic.commitText(" ", 1)
        } else {
            ic.finishComposingText()
            ic.commitText("${candidate.text} ", 1)
        }
        ic.endBatchEdit()

        val targetLang = candidate.targetLanguageSwitch ?: keyboardState.value.activeLanguage
        if (candidate.targetLanguageSwitch != null && sizeConfig.autoSwitchLanguage) {
            setActiveLanguage(candidate.targetLanguageSwitch)
        }
        learnAndRememberWord(candidate.text, targetLang)
        composingWord.clear()
        refreshSuggestions()
    }

    private fun commitCurrentWordToDictionary() {
        val word = composingWord.toString().trim()
        learnAndRememberWord(word, keyboardState.value.activeLanguage)
    }

    private fun learnAndRememberWord(rawWord: String, activeLanguage: KeyboardLanguage) {
        val word = rawWord.trim().trim { it in ".,!?;:\"'()[]{}،؛؟" }
        if (word.length < 2 || word.all { it.isDigit() } ||
            !sizeConfig.learningEnabled || sizeConfig.incognitoMode
        ) return

        // Keep prediction context available immediately, then refresh once persistence completes.
        val precedingWord = previousWord
        previousWord = word
        serviceScope.launch {
            repository.learnCommittedWord(
                rawWord = word,
                previousWord = precedingWord,
                activeLanguage = activeLanguage,
                enabledLanguages = enabledLanguages
            )
            refreshSuggestions()
        }
    }

    private fun commitAndResetComposing(text: String) {
        val ic = currentInputConnection ?: return
        dismissRecentClipSuggestion()
        commitCurrentWordToDictionary()
        composingWord.clear()
        ic.finishComposingText()
        ic.commitText(text, 1)
        refreshSuggestions()
    }

    private fun refreshSuggestions() {
        suggestionJob?.cancel()
        suggestionJob = serviceScope.launch {
            val currentWord = composingWord.toString()
            val list = repository.getSuggestions(
                composingWord = currentWord,
                previousWord = previousWord,
                activeLanguage = keyboardState.value.activeLanguage,
                enabledLanguages = enabledLanguages,
                autoSwitchEnabled = sizeConfig.autoSwitchLanguage
            )
            keyboardState.value = keyboardState.value.copy(suggestions = list)
        }
    }

    private fun updateAutoCapitalizeState(info: EditorInfo?) {
        if (!sizeConfig.autoCapitalize || keyboardState.value.activeLanguage == KeyboardLanguage.ARABIC) return
        val ic = currentInputConnection ?: return
        val mode = info?.inputType ?: 0
        val capsMode = ic.getCursorCapsMode(mode)
        if (capsMode != 0) {
            keyboardState.value = keyboardState.value.copy(isShifted = true)
        }
    }

    private fun handleAiProofread() {
        val ic = currentInputConnection ?: return
        ic.finishComposingText()
        composingWord.clear()

        val selected = ic.getSelectedText(0)?.toString()
        val extractedSnapshot = ic.getExtractedText(ExtractedTextRequest(), 0)
        val extracted = extractedSnapshot?.text?.toString()
        val fullBefore = ic.getTextBeforeCursor(5000, 0)?.toString().orEmpty()
        val fullAfter = ic.getTextAfterCursor(5000, 0)?.toString().orEmpty()
        val allFieldText = extracted ?: (fullBefore + fullAfter)

        val targetText = if (!selected.isNullOrBlank()) {
            selected
        } else {
            // Visually select the entire text in the field as requested!
            if (allFieldText.isNotEmpty()) {
                ic.setSelection(0, allFieldText.length)
            }
            allFieldText
        }

        if (targetText.isBlank()) {
            showTransientStatus(
                if (keyboardState.value.activeLanguage == KeyboardLanguage.ARABIC) "اكتب نصاً أولاً للتصحيح"
                else "Type some text first to proofread",
                isError = true
            )
            return
        }

        val replaceWholeField = selected.isNullOrBlank()
        rememberAiUndoState(allFieldText, selected, extractedSnapshot)
        retryCurrentAiOperation = { aiService.proofreadAndCorrect(targetText, aiConfig) }

        keyboardState.value = keyboardState.value.copy(
            isAiBusy = true,
            canUndoAi = false,
            statusMessage = if (keyboardState.value.activeLanguage == KeyboardLanguage.ARABIC)
                "تم تحديد النص • جاري التصحيح بالذكاء الاصطناعي..."
            else "Text selected • Proofreading with ${aiConfig.aiProvider.displayNameEn}...",
            isErrorStatus = false
        )

        serviceScope.launch {
            val result = aiService.proofreadAndCorrect(targetText, aiConfig)
            result.fold(
                onSuccess = { corrected ->
                    // Show colored diff review (Red = wrong, Green = corrected) with Copy / Apply buttons
                    keyboardState.value = keyboardState.value.copy(
                        isAiBusy = false,
                        statusMessage = null,
                        proofreadReview = ProofreadDiffReview(
                            originalText = targetText,
                            correctedText = corrected,
                            replaceWholeField = replaceWholeField
                        ),
                        panel = KeyboardPanel.PROOFREAD_DIFF
                    )
                },
                onFailure = { err ->
                    keyboardState.value = keyboardState.value.copy(isAiBusy = false)
                    showTransientStatus(err.message ?: "AI request failed", isError = true)
                }
            )
        }
    }

    private fun handleApplyProofreadDiff() {
        val review = keyboardState.value.proofreadReview ?: return
        if (review.isApplied || review.canRedo) return
        stopGradualPaste()
        replaceTextInEditor(review.correctedText, review.replaceWholeField)
        redoBackupFullText = buildAiResultSnapshot(review.correctedText) ?: getCurrentEditorText()
        keyboardState.value = keyboardState.value.copy(
            proofreadReview = review.copy(isApplied = true, canRedo = false),
            panel = KeyboardPanel.PROOFREAD_DIFF,
            canUndoAi = true
        )
        showTransientStatus(
            if (keyboardState.value.activeLanguage == KeyboardLanguage.ARABIC) "تم تطبيق النتيجة"
            else "Result applied",
            isError = false
        )
    }

    private fun handleApplyProofreadDiffGradually() {
        val review = keyboardState.value.proofreadReview ?: return
        val savedCfg = gradualPasteConfig
        val activeCfg = if (savedCfg.enabled) savedCfg else savedCfg.copy(enabled = true)
        if (!savedCfg.enabled) {
            gradualPasteConfig = activeCfg
            serviceScope.launch {
                settings.updateGradualPasteConfig { it.copy(enabled = true) }
            }
        }
        startGradualPaste(
            rawText = review.correctedText,
            cfg = activeCfg,
            replaceExistingTarget = true,
            replaceWholeField = review.replaceWholeField
        )
    }

    private fun handleCopyProofreadDiff() {
        val review = keyboardState.value.proofreadReview ?: return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(review.title, review.correctedText))
        if (!sizeConfig.incognitoMode) {
            serviceScope.launch {
                repository.captureClipboardText(review.correctedText)
            }
        }
        keyboardState.value = keyboardState.value.copy(
            proofreadReview = null,
            panel = KeyboardPanel.KEYS
        )
        showTransientStatus(
            if (keyboardState.value.activeLanguage == KeyboardLanguage.ARABIC) "تم نسخ النتيجة إلى الحافظة"
            else "Result copied to clipboard",
            isError = false
        )
    }

    private fun handleDismissProofreadDiff() {
        keyboardState.value = keyboardState.value.copy(
            proofreadReview = null,
            panel = KeyboardPanel.KEYS
        )
    }

    private fun handleReviewUndo() {
        val review = keyboardState.value.proofreadReview ?: return
        val original = undoBackupFullText ?: return
        if (!review.isApplied) return
        replaceTextInEditor(original, replaceWholeField = true)
        keyboardState.value = keyboardState.value.copy(
            proofreadReview = review.copy(isApplied = false, canRedo = true),
            canUndoAi = false
        )
        showTransientStatus("تم التراجع عن تطبيق النتيجة", isError = false)
    }

    private fun handleReviewRedo() {
        val review = keyboardState.value.proofreadReview ?: return
        val applied = redoBackupFullText ?: return
        if (!review.canRedo) return
        replaceTextInEditor(applied, replaceWholeField = true)
        keyboardState.value = keyboardState.value.copy(
            proofreadReview = review.copy(isApplied = true, canRedo = false),
            canUndoAi = true
        )
        showTransientStatus("تمت إعادة تطبيق النتيجة", isError = false)
    }

    private fun handleRegenerateReview() {
        if (keyboardState.value.isAiBusy) return
        val review = keyboardState.value.proofreadReview ?: return
        val retry = retryCurrentAiOperation ?: return
        keyboardState.value = keyboardState.value.copy(
            isAiBusy = true,
            statusMessage = "جارٍ إعادة إنشاء النتيجة للنص الأصلي...",
            isErrorStatus = false
        )
        serviceScope.launch {
            retry().fold(
                onSuccess = { regenerated ->
                    val wasApplied = review.isApplied
                    if (wasApplied) {
                        undoBackupFullText?.let { replaceTextInEditor(it, replaceWholeField = true) }
                    }
                    redoBackupFullText = if (wasApplied) buildAiResultSnapshot(regenerated) else null
                    keyboardState.value = keyboardState.value.copy(
                        isAiBusy = false,
                        statusMessage = null,
                        proofreadReview = review.copy(
                            correctedText = regenerated,
                            isApplied = false,
                            canRedo = wasApplied
                        ),
                        canUndoAi = false,
                        panel = KeyboardPanel.PROOFREAD_DIFF
                    )
                },
                onFailure = { error ->
                    keyboardState.value = keyboardState.value.copy(isAiBusy = false)
                    showTransientStatus(error.message ?: "تعذرت إعادة إنشاء النتيجة", isError = true)
                }
            )
        }
    }

    private fun handleRunCustomBot(bot: CustomBotItem) {
        if (keyboardState.value.isAiBusy) return
        val ic = currentInputConnection
        commitCurrentWordToDictionary()
        composingWord.clear()
        ic?.finishComposingText()

        val selected = ic?.getSelectedText(0)?.toString()
        val extractedSnapshot = ic?.getExtractedText(ExtractedTextRequest(), 0)
        val extracted = extractedSnapshot?.text?.toString()
        val fullBefore = ic?.getTextBeforeCursor(5000, 0)?.toString().orEmpty()
        val fullAfter = ic?.getTextAfterCursor(5000, 0)?.toString().orEmpty()
        val allFieldText = extracted ?: (fullBefore + fullAfter)

        val targetText = if (!selected.isNullOrBlank()) selected else allFieldText
        val replaceWholeField = selected.isNullOrBlank()
        rememberAiUndoState(allFieldText, selected, extractedSnapshot)
        retryCurrentAiOperation = { aiService.runCustomBot(targetText, bot, aiConfig) }

        keyboardState.value = keyboardState.value.copy(
            isAiBusy = true,
            canUndoAi = false,
            statusMessage = "جاري التشغيل بواسطة «${bot.name}»...",
            isErrorStatus = false
        )

        serviceScope.launch {
            val result = aiService.runCustomBot(targetText, bot, aiConfig)
            result.fold(
                onSuccess = { output ->
                    keyboardState.value = keyboardState.value.copy(
                        isAiBusy = false,
                        statusMessage = null,
                        proofreadReview = ProofreadDiffReview(
                            originalText = targetText,
                            correctedText = output,
                            replaceWholeField = replaceWholeField
                        ),
                        panel = KeyboardPanel.PROOFREAD_DIFF
                    )
                },
                onFailure = { err ->
                    keyboardState.value = keyboardState.value.copy(isAiBusy = false)
                    showTransientStatus(err.message ?: "Bot request failed", isError = true)
                }
            )
        }
    }

    private fun handleRunBuiltinBotAction(action: BuiltinBotAction) {
        if (keyboardState.value.isAiBusy) return
        if (action == BuiltinBotAction.PROOFREAD) {
            handleAiProofread()
            return
        }
        if (action == BuiltinBotAction.TRANSLATE) {
            keyboardState.value = keyboardState.value.copy(panel = KeyboardPanel.TRANSLATE_TARGET)
            return
        }

        val ic = currentInputConnection ?: return
        commitCurrentWordToDictionary()
        composingWord.clear()
        ic.finishComposingText()

        val selected = ic.getSelectedText(0)?.toString()
        val extractedSnapshot = ic.getExtractedText(ExtractedTextRequest(), 0)
        val extracted = extractedSnapshot?.text?.toString()
        val fullBefore = ic.getTextBeforeCursor(5000, 0)?.toString().orEmpty()
        val fullAfter = ic.getTextAfterCursor(5000, 0)?.toString().orEmpty()
        val allFieldText = extracted ?: (fullBefore + fullAfter)

        val targetText = if (!selected.isNullOrBlank()) selected else allFieldText
        if (targetText.isBlank()) {
            showTransientStatus("اكتب نصاً أولاً لتنفيذ «${action.labelAr}»", isError = true)
            return
        }

        val replaceWholeField = selected.isNullOrBlank()
        rememberAiUndoState(allFieldText, selected, extractedSnapshot)
        val targetLang = keyboardState.value.targetTranslateLang
        retryCurrentAiOperation = {
            aiService.runQuickBotAction(
                text = targetText,
                action = action,
                targetLanguage = targetLang,
                config = aiConfig
            )
        }

        keyboardState.value = keyboardState.value.copy(
            isAiBusy = true,
            canUndoAi = false,
            statusMessage = "جاري تنفيذ «${action.labelAr}»...",
            isErrorStatus = false
        )

        serviceScope.launch {
            val result = aiService.runQuickBotAction(
                text = targetText,
                action = action,
                targetLanguage = targetLang,
                config = aiConfig
            )
            result.fold(
                onSuccess = { output ->
                    keyboardState.value = keyboardState.value.copy(
                        isAiBusy = false,
                        statusMessage = null,
                        proofreadReview = ProofreadDiffReview(
                            originalText = targetText,
                            correctedText = output,
                            replaceWholeField = replaceWholeField
                        ),
                        panel = KeyboardPanel.PROOFREAD_DIFF
                    )
                },
                onFailure = { err ->
                    keyboardState.value = keyboardState.value.copy(isAiBusy = false)
                    showTransientStatus(err.message ?: "AI request failed", isError = true)
                }
            )
        }
    }

    private fun openCreateBotInApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.EXTRA_CREATE_BOT, true)
        }
        startActivity(intent)
    }

    private fun openBotsManagerInApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.EXTRA_OPEN_BOTS, true)
        }
        startActivity(intent)
    }

    private fun handleVoiceClick() {
        if (keyboardState.value.isVoiceListening) {
            stopVoiceListening()
            return
        }
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            showTransientStatus("يرجى منح إذن الميكروفون للكتابة بالصوت", isError = true)
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(MainActivity.EXTRA_REQUEST_MIC, true)
            }
            startActivity(intent)
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            showTransientStatus("خدمة التعرف على الصوت غير متوفرة على هذا الجهاز", isError = true)
            return
        }

        val recognizer = speechRecognizer ?: SpeechRecognizer.createSpeechRecognizer(this).also {
            speechRecognizer = it
        }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                keyboardState.value = keyboardState.value.copy(
                    isVoiceListening = true,
                    statusMessage = "جاري الاستماع... تحدث الآن 🎤",
                    isErrorStatus = false
                )
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                keyboardState.value = keyboardState.value.copy(isVoiceListening = false)
                showTransientStatus("تعذر التقاط الصوت (رمز $error)، حاول مجدداً", isError = true)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken = matches?.firstOrNull()?.trim().orEmpty()
                keyboardState.value = keyboardState.value.copy(
                    isVoiceListening = false,
                    statusMessage = null
                )
                if (spoken.isNotBlank()) {
                    commitAndResetComposing("$spoken ")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.trim()
                if (!partial.isNullOrBlank()) {
                    keyboardState.value = keyboardState.value.copy(
                        statusMessage = "🎤 $partial",
                        isErrorStatus = false
                    )
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val langTag = when (keyboardState.value.activeLanguage) {
            KeyboardLanguage.ARABIC -> "ar-SA"
            KeyboardLanguage.ENGLISH -> "en-US"
            KeyboardLanguage.FRENCH -> "fr-FR"
            KeyboardLanguage.SPANISH -> "es-ES"
            KeyboardLanguage.TURKISH -> "tr-TR"
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        keyboardState.value = keyboardState.value.copy(
            isVoiceListening = true,
            statusMessage = "جاري تشغيل الميكروفون...",
            isErrorStatus = false
        )
        recognizer.startListening(intent)
    }

    private fun stopVoiceListening() {
        if (keyboardState.value.isVoiceListening) {
            speechRecognizer?.stopListening()
            keyboardState.value = keyboardState.value.copy(
                isVoiceListening = false,
                statusMessage = null
            )
        }
    }

    private fun handleAiTranslate() {
        val ic = currentInputConnection ?: return
        ic.finishComposingText()
        composingWord.clear()

        val selected = ic.getSelectedText(0)?.toString()
        val extractedSnapshot = ic.getExtractedText(ExtractedTextRequest(), 0)
        val extracted = extractedSnapshot?.text?.toString()
        val fullBefore = ic.getTextBeforeCursor(5000, 0)?.toString().orEmpty()
        val fullAfter = ic.getTextAfterCursor(5000, 0)?.toString().orEmpty()
        val allFieldText = extracted ?: (fullBefore + fullAfter)

        val targetText = if (!selected.isNullOrBlank()) {
            selected
        } else {
            if (allFieldText.isNotEmpty()) {
                ic.setSelection(0, allFieldText.length)
            }
            allFieldText
        }

        if (targetText.isBlank()) {
            showTransientStatus(
                if (keyboardState.value.activeLanguage == KeyboardLanguage.ARABIC) "اكتب نصاً للترجمة"
                else "Type some text to translate",
                isError = true
            )
            return
        }

        val replaceWholeField = selected.isNullOrBlank()
        rememberAiUndoState(allFieldText, selected, extractedSnapshot)
        val targetLang = keyboardState.value.targetTranslateLang
        retryCurrentAiOperation = { aiService.translateText(targetText, targetLang, aiConfig) }

        keyboardState.value = keyboardState.value.copy(
            isAiBusy = true,
            canUndoAi = false,
            statusMessage = "جاري الترجمة إلى ${targetLang.labelAr}...",
            isErrorStatus = false
        )

        serviceScope.launch {
            val result = aiService.translateText(targetText, targetLang, aiConfig)
            result.fold(
                onSuccess = { translated ->
                    keyboardState.value = keyboardState.value.copy(
                        isAiBusy = false,
                        canUndoAi = false,
                        statusMessage = null,
                        proofreadReview = ProofreadDiffReview(
                            originalText = targetText,
                            correctedText = translated,
                            replaceWholeField = replaceWholeField,
                            title = "نتيجة الترجمة • ${targetLang.labelAr}"
                        ),
                        panel = KeyboardPanel.PROOFREAD_DIFF
                    )
                },
                onFailure = { err ->
                    keyboardState.value = keyboardState.value.copy(isAiBusy = false)
                    showTransientStatus(err.message ?: "Translation failed", isError = true)
                }
            )
        }
    }

    private fun selectTranslateTarget(target: TargetTranslateLanguage) {
        keyboardState.value = keyboardState.value.copy(
            targetTranslateLang = target,
            panel = KeyboardPanel.KEYS
        )
        serviceScope.launch { settings.setTargetTranslateLanguage(target) }
        handleAiTranslate()
    }

    private fun handleUndoAi() {
        if (keyboardState.value.proofreadReview?.isApplied == true) {
            handleReviewUndo()
            return
        }
        val backup = undoBackupFullText ?: return
        replaceTextInEditor(backup, replaceWholeField = true)
        undoBackupFullText = null
        redoBackupFullText = null
        keyboardState.value = keyboardState.value.copy(canUndoAi = false)
        showTransientStatus(
            if (keyboardState.value.activeLanguage == KeyboardLanguage.ARABIC) "تم التراجع عن التعديل"
            else "Reverted to original text",
            isError = false
        )
    }

    private fun getCurrentEditorText(): String? {
        val ic = currentInputConnection ?: return null
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)?.text?.toString()
        return extracted ?: (ic.getTextBeforeCursor(5000, 0)?.toString().orEmpty() +
            ic.getTextAfterCursor(5000, 0)?.toString().orEmpty())
    }

    private fun rememberAiUndoState(
        fullText: String,
        selectedText: String?,
        extracted: ExtractedText?
    ) {
        undoBackupFullText = fullText
        redoBackupFullText = null
        aiSourceWasWholeField = selectedText.isNullOrBlank()
        if (aiSourceWasWholeField) {
            aiSourceSelectionStart = 0
            aiSourceSelectionEnd = fullText.length
            return
        }
        val fallbackStart = fullText.indexOf(selectedText.orEmpty()).takeIf { it >= 0 } ?: 0
        val start = extracted?.selectionStart?.takeIf { it >= 0 } ?: fallbackStart
        val end = extracted?.selectionEnd?.takeIf { it >= 0 } ?: (start + selectedText.orEmpty().length)
        aiSourceSelectionStart = minOf(start, end).coerceIn(0, fullText.length)
        aiSourceSelectionEnd = maxOf(start, end).coerceIn(aiSourceSelectionStart, fullText.length)
    }

    private fun buildAiResultSnapshot(resultText: String): String? {
        val original = undoBackupFullText ?: return null
        if (aiSourceWasWholeField) return resultText
        val start = aiSourceSelectionStart.coerceIn(0, original.length)
        val end = aiSourceSelectionEnd.coerceIn(start, original.length)
        return original.replaceRange(start, end, resultText)
    }

    private fun replaceTextInEditor(newText: String, replaceWholeField: Boolean) {
        val ic = currentInputConnection ?: return
        ic.beginBatchEdit()
        if (replaceWholeField) {
            val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
            val currentLen = extracted?.text?.length
                ?: ((ic.getTextBeforeCursor(5000, 0)?.length ?: 0) + (ic.getTextAfterCursor(5000, 0)?.length ?: 0))
            val alreadyAllSelected = extracted != null &&
                ((extracted.selectionStart == 0 && extracted.selectionEnd == currentLen) ||
                    (extracted.selectionEnd == 0 && extracted.selectionStart == currentLen))
            if (!alreadyAllSelected) {
                ic.setSelection(0, currentLen)
            }
        }
        ic.commitText(newText, 1)
        ic.endBatchEdit()
    }

    private fun clearTargetTextForReplacement(ic: InputConnection, replaceWholeField: Boolean) {
        ic.beginBatchEdit()
        if (replaceWholeField) {
            val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
            val currentLen = extracted?.text?.length
                ?: ((ic.getTextBeforeCursor(5000, 0)?.length ?: 0) + (ic.getTextAfterCursor(5000, 0)?.length ?: 0))
            if (currentLen > 0) {
                ic.setSelection(0, currentLen)
                ic.commitText("", 1)
            }
        } else {
            val selected = ic.getSelectedText(0)
            if (!selected.isNullOrEmpty()) {
                ic.commitText("", 1)
            }
        }
        ic.endBatchEdit()
    }

    private fun handleGradualPaste(rawText: String) {
        if (rawText.isEmpty()) return
        dismissRecentClipSuggestion()
        val cfg = gradualPasteConfig
        if (!cfg.enabled) {
            val isAr = keyboardState.value.activeLanguage == KeyboardLanguage.ARABIC
            showTransientStatus(
                message = if (isAr) {
                    "اللصق التدريجي غير مفعّل — اضغط تفعيل ⚡ لتشغيله"
                } else {
                    "Gradual Paste is disabled — tap Enable ⚡ to turn it on"
                },
                isError = true,
                showGradualEnablePrompt = true
            )
            return
        }
        startGradualPaste(
            rawText = rawText,
            cfg = cfg,
            replaceExistingTarget = false,
            replaceWholeField = false
        )
    }

    private fun startGradualPaste(
        rawText: String,
        cfg: GradualPasteConfig,
        replaceExistingTarget: Boolean,
        replaceWholeField: Boolean
    ) {
        if (rawText.isEmpty() && !replaceExistingTarget) return
        dismissRecentClipSuggestion()
        stopGradualPaste()
        val ic = currentInputConnection ?: return
        commitCurrentWordToDictionary()
        composingWord.clear()
        ic.finishComposingText()

        val isAr = keyboardState.value.activeLanguage == KeyboardLanguage.ARABIC
        statusJob?.cancel()
        keyboardState.value = keyboardState.value.copy(
            proofreadReview = if (replaceExistingTarget) null else keyboardState.value.proofreadReview,
            panel = KeyboardPanel.KEYS,
            isGradualPasting = true,
            gradualPasteDisabledPrompt = false,
            canUndoAi = keyboardState.value.canUndoAi || replaceExistingTarget,
            isErrorStatus = false,
            statusMessage = when {
                replaceExistingTarget && isAr -> "⚡ تم تفعيل اللصق التدريجي • جاري الاستبدال..."
                replaceExistingTarget -> "⚡ Gradual paste active • Replacing..."
                isAr -> "⚡ جاري اللصق التدريجي..."
                else -> "⚡ Gradual pasting..."
            }
        )

        gradualPasteJob = serviceScope.launch {
            var completedNormally = false
            var targetCleared = !replaceExistingTarget
            try {
                if (rawText.isEmpty() && replaceExistingTarget) {
                    val activeIc = currentInputConnection
                    if (activeIc != null) {
                        clearTargetTextForReplacement(activeIc, replaceWholeField)
                    }
                    completedNormally = true
                } else {
                    completedNormally = GradualPasteEngine.stream(rawText, cfg) { chunk ->
                        val activeIc = currentInputConnection ?: return@stream false
                        if (!targetCleared) {
                            clearTargetTextForReplacement(activeIc, replaceWholeField)
                            targetCleared = true
                        }
                        activeIc.commitText(chunk, 1)
                        true
                    }
                }
            } catch (_: CancellationException) {
                // Stopped by user or focus change
            } finally {
                keyboardState.value = keyboardState.value.copy(
                    isGradualPasting = false
                )
                if (completedNormally) {
                    refreshSuggestions()
                    showTransientStatus(
                        message = when {
                            replaceExistingTarget && isAr -> "تم استبدال النص باللصق التدريجي ⚡"
                            replaceExistingTarget -> "Text replaced via gradual paste ⚡"
                            isAr -> "تم اللصق التدريجي بنجاح ⚡"
                            else -> "Gradual paste complete ⚡"
                        },
                        isError = false
                    )
                }
            }
        }
    }

    private fun stopGradualPaste() {
        val wasRunning = gradualPasteJob?.isActive == true || keyboardState.value.isGradualPasting
        gradualPasteJob?.cancel()
        gradualPasteJob = null
        if (wasRunning) {
            keyboardState.value = keyboardState.value.copy(
                isGradualPasting = false,
                statusMessage = null
            )
        }
    }

    private fun openGradualPasteSettings() {
        statusJob?.cancel()
        keyboardState.value = keyboardState.value.copy(
            panel = KeyboardPanel.TOOLBAR_CUSTOMIZE,
            customizeTabIndex = 1,
            statusMessage = null,
            gradualPasteDisabledPrompt = false
        )
    }

    private fun showTransientStatus(
        message: String,
        isError: Boolean,
        showGradualEnablePrompt: Boolean = false
    ) {
        statusJob?.cancel()
        keyboardState.value = keyboardState.value.copy(
            statusMessage = message,
            isErrorStatus = isError,
            gradualPasteDisabledPrompt = showGradualEnablePrompt
        )
        statusJob = serviceScope.launch {
            delay(if (showGradualEnablePrompt) 5200 else 4200)
            keyboardState.value = keyboardState.value.copy(
                statusMessage = null,
                gradualPasteDisabledPrompt = false
            )
        }
    }

    private fun handleQuickTool(tool: QuickInputTool) {
        val ic = currentInputConnection
        val isAr = keyboardState.value.activeLanguage == KeyboardLanguage.ARABIC
        when (tool) {
            QuickInputTool.SELECT_ALL -> {
                ic?.finishComposingText()
                composingWord.clear()
                if (ic != null && !ic.performContextMenuAction(android.R.id.selectAll)) {
                    val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)?.text
                    if (extracted != null) ic.setSelection(0, extracted.length)
                }
            }
            QuickInputTool.SELECT_WORD -> selectCurrentWord()
            QuickInputTool.COPY -> {
                val sel = ic?.getSelectedText(0)?.toString()
                ic?.performContextMenuAction(android.R.id.copy)
                if (!sel.isNullOrBlank()) {
                    val cb = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    cb?.setPrimaryClip(ClipData.newPlainText("Bayan Copy", sel))
                    if (!sizeConfig.incognitoMode) {
                        serviceScope.launch { repository.captureClipboardText(sel) }
                    }
                    showTransientStatus(if (isAr) "تم النسخ" else "Copied", isError = false)
                }
            }
            QuickInputTool.CUT -> {
                val sel = ic?.getSelectedText(0)?.toString()
                if (!sel.isNullOrBlank()) {
                    val cb = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    cb?.setPrimaryClip(ClipData.newPlainText("Bayan Cut", sel))
                    if (!sizeConfig.incognitoMode) {
                        serviceScope.launch { repository.captureClipboardText(sel) }
                    }
                }
                if (ic != null && !ic.performContextMenuAction(android.R.id.cut) && !sel.isNullOrEmpty()) {
                    ic.commitText("", 1)
                }
            }
            QuickInputTool.PASTE -> {
                dismissRecentClipSuggestion()
                if (ic != null) {
                    ic.finishComposingText()
                    composingWord.clear()
                    if (!ic.performContextMenuAction(android.R.id.paste)) {
                        val cb = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val text = cb?.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
                        if (!text.isNullOrEmpty()) {
                            ic.commitText(text, 1)
                        }
                    }
                }
            }
            QuickInputTool.UNDO -> {
                if (ic != null && !ic.performContextMenuAction(android.R.id.undo)) {
                    sendModifiedKey(KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON)
                }
            }
            QuickInputTool.REDO -> {
                if (ic != null && !ic.performContextMenuAction(android.R.id.redo)) {
                    sendModifiedKey(KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON)
                }
            }
            QuickInputTool.CLEAR_CLIPBOARD -> {
                dismissRecentClipSuggestion()
                serviceScope.launch { repository.clearUnpinnedClipboard() }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val cb = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    cb?.clearPrimaryClip()
                }
                showTransientStatus(if (isAr) "تم مسح الحافظة غير المثبتة" else "Unpinned clipboard cleared", isError = false)
            }
            QuickInputTool.CURSOR_LEFT -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
            QuickInputTool.CURSOR_RIGHT -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
            QuickInputTool.CURSOR_UP -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP)
            QuickInputTool.CURSOR_DOWN -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN)
            QuickInputTool.WORD_LEFT -> sendModifiedKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.META_CTRL_ON)
            QuickInputTool.WORD_RIGHT -> sendModifiedKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.META_CTRL_ON)
            QuickInputTool.LINE_START -> sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_HOME)
            QuickInputTool.LINE_END -> sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_END)
            QuickInputTool.PAGE_UP -> sendDownUpKeyEvents(KeyEvent.KEYCODE_PAGE_UP)
            QuickInputTool.PAGE_DOWN -> sendDownUpKeyEvents(KeyEvent.KEYCODE_PAGE_DOWN)
            QuickInputTool.DOC_START -> {
                val handled = ic?.getExtractedText(ExtractedTextRequest(), 0) != null
                if (handled) {
                    ic.setSelection(0, 0)
                } else {
                    sendModifiedKey(KeyEvent.KEYCODE_MOVE_HOME, KeyEvent.META_CTRL_ON)
                }
            }
            QuickInputTool.DOC_END -> {
                val extracted = ic?.getExtractedText(ExtractedTextRequest(), 0)?.text
                if (extracted != null) {
                    ic.setSelection(extracted.length, extracted.length)
                } else {
                    sendModifiedKey(KeyEvent.KEYCODE_MOVE_END, KeyEvent.META_CTRL_ON)
                }
            }
            QuickInputTool.NUMPAD -> {
                val nextLayer = if (keyboardState.value.layer == KeyboardLayer.NUMPAD) KeyboardLayer.LETTERS else KeyboardLayer.NUMPAD
                keyboardState.value = keyboardState.value.copy(
                    layer = nextLayer,
                    panel = KeyboardPanel.KEYS
                )
            }
            QuickInputTool.ONE_HANDED -> {
                val nextMode = when (sizeConfig.oneHandedMode) {
                    OneHandedMode.OFF -> OneHandedMode.RIGHT
                    OneHandedMode.RIGHT -> OneHandedMode.LEFT
                    OneHandedMode.LEFT -> OneHandedMode.OFF
                }
                serviceScope.launch { settings.updateSizeConfig { it.copy(oneHandedMode = nextMode) } }
                keyboardState.value = keyboardState.value.copy(panel = KeyboardPanel.KEYS)
            }
            QuickInputTool.SPLIT_KEYBOARD -> {
                val next = !sizeConfig.splitKeyboard
                serviceScope.launch { settings.updateSizeConfig { it.copy(splitKeyboard = next) } }
                keyboardState.value = keyboardState.value.copy(panel = KeyboardPanel.KEYS)
            }
            QuickInputTool.INCOGNITO -> {
                val next = !sizeConfig.incognitoMode
                serviceScope.launch { settings.updateSizeConfig { it.copy(incognitoMode = next) } }
                showTransientStatus(
                    message = if (next) {
                        if (isAr) "تم تفعيل التصفح المتخفي — لن يتم حفظ الكلمات أو النسخ" else "Incognito active — typing & clips won't be saved"
                    } else {
                        if (isAr) "تم إيقاف التصفح المتخفي" else "Incognito disabled"
                    },
                    isError = false
                )
            }
            QuickInputTool.AUTO_CORRECT -> {
                val next = !sizeConfig.autoCorrectEnabled
                serviceScope.launch { settings.updateSizeConfig { it.copy(autoCorrectEnabled = next) } }
                showTransientStatus(
                    message = if (next) {
                        if (isAr) "تم تفعيل التصحيح التلقائي" else "Auto-Correct enabled"
                    } else {
                        if (isAr) "تم إيقاف التصحيح التلقائي" else "Auto-Correct disabled"
                    },
                    isError = false
                )
            }
            QuickInputTool.VOICE_INPUT -> {
                keyboardState.value = keyboardState.value.copy(panel = KeyboardPanel.KEYS)
                handleVoiceClick()
            }
            QuickInputTool.CLIPBOARD -> {
                keyboardState.value = keyboardState.value.copy(panel = KeyboardPanel.CLIPBOARD)
            }
            QuickInputTool.EMOJI -> {
                keyboardState.value = keyboardState.value.copy(panel = KeyboardPanel.EMOJI)
            }
            QuickInputTool.SETTINGS -> openCompanionSettings()
        }
    }

    private fun selectCurrentWord() {
        val ic = currentInputConnection ?: return
        ic.finishComposingText()
        composingWord.clear()
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
        if (extracted != null && extracted.text != null) {
            val full = extracted.text.toString()
            val cursor = extracted.selectionStart.coerceIn(0, full.length)
            var start = cursor
            var end = cursor
            while (start > 0 && !full[start - 1].isWhitespace()) start--
            while (end < full.length && !full[end].isWhitespace()) end++
            if (end > start) {
                ic.setSelection(start, end)
            }
        } else {
            sendModifiedKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.META_CTRL_ON)
            sendModifiedKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON)
        }
    }

    private fun sendModifiedKey(keyCode: Int, metaState: Int) {
        val ic = currentInputConnection ?: return
        val now = SystemClock.uptimeMillis()
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState))
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState))
    }

    private fun openCompanionSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun openPasswordVaultInApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.EXTRA_OPEN_PASSWORDS, true)
        }
        startActivity(intent)
    }
}
