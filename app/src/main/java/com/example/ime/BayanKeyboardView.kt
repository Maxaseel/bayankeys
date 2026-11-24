package com.example.ime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.LastPage
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.rounded.BackHand
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.FirstPage
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.VerticalAlignBottom
import androidx.compose.material.icons.rounded.VerticalAlignTop
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.ClipboardEntity
import com.example.data.model.BuiltinBotAction
import com.example.data.model.CustomBotItem
import com.example.data.model.GradualPasteConfig
import com.example.data.model.KeyButtonShape
import com.example.data.model.KeyboardLanguage
import com.example.data.model.KeyboardSizeConfig
import com.example.data.model.KeyboardThemeConfig
import com.example.data.model.NumberRowStyle
import com.example.data.model.OneHandedMode
import com.example.data.model.PasswordEntryItem
import com.example.data.model.ProofreadDiffReview
import com.example.data.model.QuickInputTool
import com.example.data.model.QuickToolCategory
import com.example.data.model.ResolvedKeyboardPalette
import com.example.data.model.TargetTranslateLanguage
import com.example.data.model.ToolbarAction
import com.example.data.model.ToolbarVisibilityConfig
import com.example.data.repository.SuggestionCandidate
import com.example.ui.components.GradualPasteSettingsSection
import com.example.ui.components.KeyButtonShapeGrid
import com.example.ui.components.ServiceBrandIcon
import com.example.ui.screens.botIconVector
import com.example.ui.screens.builtinBotActionIcon
import com.example.ui.theme.CairoFontFamily
import com.example.util.WordDiffUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class KeyboardPanel {
    KEYS,
    BOTS,
    CLIPBOARD,
    QUICK_TOOLS,
    PASSWORDS,
    EMOJI,
    TRANSLATE_TARGET,
    TOOLBAR_CUSTOMIZE,
    PROOFREAD_DIFF
}

data class KeyboardUiState(
    val activeLanguage: KeyboardLanguage = KeyboardLanguage.ARABIC,
    val layer: KeyboardLayer = KeyboardLayer.LETTERS,
    val isShifted: Boolean = false,
    val isCapsLocked: Boolean = false,
    val panel: KeyboardPanel = KeyboardPanel.KEYS,
    val customizeTabIndex: Int = 0,
    val suggestions: List<SuggestionCandidate> = emptyList(),
    val recentClipSuggestion: String? = null,
    val isAiBusy: Boolean = false,
    val isVoiceListening: Boolean = false,
    val isGradualPasting: Boolean = false,
    val gradualPasteDisabledPrompt: Boolean = false,
    val statusMessage: String? = null,
    val isErrorStatus: Boolean = false,
    val canUndoAi: Boolean = false,
    val targetTranslateLang: TargetTranslateLanguage = TargetTranslateLanguage.ENGLISH,
    val proofreadReview: ProofreadDiffReview? = null
)

@Composable
fun BayanKeyboardView(
    state: KeyboardUiState,
    themeConfig: KeyboardThemeConfig,
    sizeConfig: KeyboardSizeConfig,
    toolbarConfig: ToolbarVisibilityConfig = ToolbarVisibilityConfig(),
    gradualPasteConfig: GradualPasteConfig = GradualPasteConfig(),
    clipboardItems: List<ClipboardEntity>,
    passwordEntries: List<PasswordEntryItem> = emptyList(),
    customBots: List<CustomBotItem> = emptyList(),
    isPreviewMode: Boolean = false,
    onKeyText: (String) -> Unit,
    onBackspace: () -> Unit,
    onShiftToggle: () -> Unit,
    onSymbolsToggle: () -> Unit,
    onSwitchLanguage: (Int) -> Unit,
    onEnter: () -> Unit,
    onSuggestionClick: (SuggestionCandidate) -> Unit,
    onProofreadClick: () -> Unit,
    onRunCustomBot: (CustomBotItem) -> Unit = {},
    onRunBuiltinBotAction: (BuiltinBotAction) -> Unit = {},
    onOpenCreateBot: () -> Unit = {},
    onOpenBotsManager: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
    onApplyProofreadDiff: () -> Unit = {},
    onApplyProofreadDiffGradually: () -> Unit = {},
    onCopyProofreadDiff: () -> Unit = {},
    onDismissProofreadDiff: () -> Unit = {},
    onUpdateToolbarConfig: ((ToolbarVisibilityConfig) -> ToolbarVisibilityConfig) -> Unit = {},
    onUpdateSizeConfig: ((KeyboardSizeConfig) -> KeyboardSizeConfig) -> Unit = {},
    onSetKeyShape: (KeyButtonShape) -> Unit = {},
    onUpdateGradualPasteConfig: ((GradualPasteConfig) -> GradualPasteConfig) -> Unit = {},
    onGradualPaste: (String) -> Unit = {},
    onStopGradualPaste: () -> Unit = {},
    onOpenGradualPasteSettings: () -> Unit = {},
    onQuickToolClick: (QuickInputTool) -> Unit = {},
    onInsertPasswordCredential: (String) -> Unit = {},
    onOpenPasswordVault: () -> Unit = {},
    onTranslateClick: () -> Unit,
    onSelectTranslateTarget: (TargetTranslateLanguage) -> Unit,
    onUndoAiClick: () -> Unit,
    onPanelChange: (KeyboardPanel) -> Unit,
    onClipboardPaste: (String) -> Unit,
    onClipboardPinToggle: (ClipboardEntity) -> Unit,
    onClipboardDelete: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val palette = themeConfig.resolvePalette(isSystemInDarkTheme())
    val haptic = LocalHapticFeedback.current
    val performHaptic = {
        if (sizeConfig.hapticFeedback) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.background)
            .then(
                if (!isPreviewMode) {
                    Modifier.windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                } else Modifier
            )
    ) {
        val baseKeyHeight = (if (isPreviewMode) 36.dp else 44.dp) * sizeConfig.heightScale
        val rowCount = if (sizeConfig.showNumberRow && state.layer == KeyboardLayer.LETTERS) 5 else 4
        val keyHeight = if (!isPreviewMode && constraints.hasBoundedHeight && maxHeight < 650.dp) {
            val budgetForKeys = (maxHeight * 0.44f - 82.dp).coerceAtLeast(130.dp)
            val maxRowKey = (budgetForKeys - 24.dp) / rowCount
            baseKeyHeight.coerceAtMost(maxRowKey).coerceIn(30.dp, 48.dp)
        } else {
            baseKeyHeight.coerceIn(30.dp, 54.dp)
        }
        val panelHeight = (keyHeight * 4) + 24.dp
        val keyFontSp = (if (isPreviewMode) 14f else 18f) * sizeConfig.fontScale

        // Keep physical key layout LTR so Arabic & English rows never mirror unexpectedly
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 4.dp,
                        bottom = if (isPreviewMode) 6.dp else sizeConfig.bottomPaddingDp.dp.coerceAtLeast(4.dp)
                    )
            ) {
                // 1. Smart AI + Tools Toolbar
                KeyboardToolbar(
                    state = state,
                    toolbarConfig = toolbarConfig,
                    sizeConfig = sizeConfig,
                    customBots = customBots,
                    palette = palette,
                    isPreviewMode = isPreviewMode,
                    onProofreadClick = {
                        performHaptic()
                        onProofreadClick()
                    },
                    onRunCustomBot = { bot ->
                        performHaptic()
                        onRunCustomBot(bot)
                    },
                    onVoiceClick = {
                        performHaptic()
                        onVoiceClick()
                    },
                    onToggleTranslateTarget = {
                        performHaptic()
                        onPanelChange(
                            if (state.panel == KeyboardPanel.TRANSLATE_TARGET) KeyboardPanel.KEYS
                            else KeyboardPanel.TRANSLATE_TARGET
                        )
                    },
                    onUndoAiClick = {
                        performHaptic()
                        onUndoAiClick()
                    },
                    onPanelChange = { panel ->
                        performHaptic()
                        onPanelChange(panel)
                    },
                    onOpenSettings = onOpenSettings
                )

            // Status / Feedback banner when AI, Gradual Paste, or prompt runs
            AnimatedVisibility(visible = state.statusMessage != null) {
                val msg = state.statusMessage.orEmpty()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (state.isErrorStatus) Color(0xFFEF4444).copy(alpha = 0.16f)
                            else palette.accent.copy(alpha = 0.16f)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = msg,
                        color = if (state.isErrorStatus) Color(0xFFFCA5A5) else palette.text,
                        fontSize = 11.sp,
                        fontFamily = CairoFontFamily,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.isGradualPasting) {
                        Surface(
                            onClick = onStopGradualPaste,
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEF4444)
                        ) {
                            Text(
                                text = "إيقاف",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    } else if (state.gradualPasteDisabledPrompt) {
                        Surface(
                            onClick = onOpenGradualPasteSettings,
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2563EB)
                        ) {
                            Text(
                                text = "تفعيل ⚡",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 2. Suggestion Strip (RTL for Arabic, plus Auto-Dismissing Recent Copied Text chip)
            if (state.panel == KeyboardPanel.KEYS) {
                SuggestionStrip(
                    activeLanguage = state.activeLanguage,
                    suggestions = state.suggestions,
                    recentClipText = state.recentClipSuggestion,
                    isIncognito = sizeConfig.incognitoMode,
                    palette = palette,
                    isPreviewMode = isPreviewMode,
                    onSuggestionClick = {
                        performHaptic()
                        onSuggestionClick(it)
                    },
                    onLatestClipTap = { text ->
                        performHaptic()
                        onClipboardPaste(text)
                    },
                    onLatestClipLongPress = { text ->
                        performHaptic()
                        onGradualPaste(text)
                    }
                )
            }

            // 3. Main Keyboard Body OR Clipboard / Quick Tools / Passwords / Emoji / Translate Target Panel
            when (state.panel) {
                KeyboardPanel.KEYS -> {
                    val rows = remember(
                        state.activeLanguage,
                        state.layer,
                        sizeConfig.showNumberRow,
                        sizeConfig.numberRowStyle
                    ) {
                        KeyboardLayouts.getRowsFor(
                            language = state.activeLanguage,
                            layer = state.layer,
                            showNumberRow = sizeConfig.showNumberRow,
                            numberRowStyle = sizeConfig.numberRowStyle
                        )
                    }
                    val oneHanded = sizeConfig.oneHandedMode != OneHandedMode.OFF &&
                        state.layer != KeyboardLayer.NUMPAD &&
                        !isPreviewMode

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (oneHanded && sizeConfig.oneHandedMode == OneHandedMode.RIGHT) {
                            OneHandedSideStrip(
                                currentMode = sizeConfig.oneHandedMode,
                                palette = palette,
                                height = (keyHeight * rows.size) + (6.dp * (rows.size - 1)),
                                onExpand = {
                                    performHaptic()
                                    onUpdateSizeConfig { it.copy(oneHandedMode = OneHandedMode.OFF) }
                                },
                                onFlip = {
                                    performHaptic()
                                    onUpdateSizeConfig { it.copy(oneHandedMode = OneHandedMode.LEFT) }
                                }
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(if (isPreviewMode) 4.dp else 6.dp)
                        ) {
                            val splitActive = sizeConfig.splitKeyboard &&
                                state.layer != KeyboardLayer.NUMPAD &&
                                !oneHanded
                            rows.forEachIndexed { rowIndex, row ->
                                val isBottomRow = rowIndex == rows.lastIndex
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(if (isPreviewMode) 3.dp else 4.dp)
                                ) {
                                    val splitIndex = if (splitActive && !isBottomRow && row.size >= 6) row.size / 2 else -1
                                    row.forEachIndexed { keyIdx, spec ->
                                        if (splitIndex > 0 && keyIdx == splitIndex) {
                                            Spacer(modifier = Modifier.weight(0.95f))
                                        }
                                        KeyboardKeyButton(
                                            spec = spec,
                                            isShifted = state.isShifted || state.isCapsLocked,
                                            isCapsLocked = state.isCapsLocked,
                                            palette = palette,
                                            keyHeight = keyHeight,
                                            fontSp = if (oneHanded) keyFontSp * 0.9f else keyFontSp,
                                            isPreviewMode = isPreviewMode,
                                            modifier = Modifier.weight(spec.weight),
                                            onTap = {
                                                performHaptic()
                                                when (spec.kind) {
                                                    KeyKind.CHARACTER -> {
                                                        val out = if (state.isShifted || state.isCapsLocked) spec.shiftOutput else spec.output
                                                        onKeyText(out)
                                                    }
                                                    KeyKind.SPACE -> onKeyText(" ")
                                                    KeyKind.ENTER -> onEnter()
                                                    KeyKind.BACKSPACE -> onBackspace()
                                                    KeyKind.SHIFT -> onShiftToggle()
                                                    KeyKind.SYMBOLS_TOGGLE -> onSymbolsToggle()
                                                    KeyKind.LANGUAGE_SWITCH -> onSwitchLanguage(1)
                                                    KeyKind.EMOJI -> onPanelChange(KeyboardPanel.EMOJI)
                                                }
                                            },
                                            onLongPressChar = { ch ->
                                                performHaptic()
                                                onKeyText(ch)
                                            },
                                            onBackspaceRepeat = {
                                                onBackspace()
                                            },
                                            onSpaceSwipe = { dir ->
                                                performHaptic()
                                                onSwitchLanguage(dir)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (oneHanded && sizeConfig.oneHandedMode == OneHandedMode.LEFT) {
                            OneHandedSideStrip(
                                currentMode = sizeConfig.oneHandedMode,
                                palette = palette,
                                height = (keyHeight * rows.size) + (6.dp * (rows.size - 1)),
                                onExpand = {
                                    performHaptic()
                                    onUpdateSizeConfig { it.copy(oneHandedMode = OneHandedMode.OFF) }
                                },
                                onFlip = {
                                    performHaptic()
                                    onUpdateSizeConfig { it.copy(oneHandedMode = OneHandedMode.RIGHT) }
                                }
                            )
                        }
                    }
                }

                KeyboardPanel.BOTS -> {
                    KeyboardBotsPanel(
                        customBots = customBots,
                        isAiBusy = state.isAiBusy,
                        palette = palette,
                        height = panelHeight + 14.dp,
                        onRunCustomBot = { bot ->
                            performHaptic()
                            onRunCustomBot(bot)
                        },
                        onRunBuiltinAction = { action ->
                            performHaptic()
                            onRunBuiltinBotAction(action)
                        },
                        onCreateBot = {
                            performHaptic()
                            onOpenCreateBot()
                        },
                        onManageBots = {
                            performHaptic()
                            onOpenBotsManager()
                        },
                        onClose = { onPanelChange(KeyboardPanel.KEYS) }
                    )
                }

                KeyboardPanel.CLIPBOARD -> {
                    KeyboardClipboardPanel(
                        items = clipboardItems,
                        gradualEnabled = gradualPasteConfig.enabled,
                        palette = palette,
                        height = panelHeight,
                        onPaste = {
                            performHaptic()
                            onClipboardPaste(it)
                        },
                        onGradualPaste = {
                            performHaptic()
                            onGradualPaste(it)
                        },
                        onPinToggle = {
                            performHaptic()
                            onClipboardPinToggle(it)
                        },
                        onDelete = {
                            performHaptic()
                            onClipboardDelete(it)
                        },
                        onClose = { onPanelChange(KeyboardPanel.KEYS) }
                    )
                }

                KeyboardPanel.QUICK_TOOLS -> {
                    KeyboardQuickToolsPanel(
                        state = state,
                        sizeConfig = sizeConfig,
                        palette = palette,
                        height = panelHeight + 14.dp,
                        onToolClick = { tool ->
                            performHaptic()
                            onQuickToolClick(tool)
                        },
                        onClose = { onPanelChange(KeyboardPanel.KEYS) }
                    )
                }

                KeyboardPanel.PASSWORDS -> {
                    KeyboardPasswordsPanel(
                        entries = passwordEntries,
                        palette = palette,
                        height = panelHeight,
                        onInsertCredential = { value ->
                            performHaptic()
                            onInsertPasswordCredential(value)
                        },
                        onOpenVaultManager = {
                            performHaptic()
                            onOpenPasswordVault()
                        },
                        onClose = { onPanelChange(KeyboardPanel.KEYS) }
                    )
                }

                KeyboardPanel.EMOJI -> {
                    KeyboardEmojiPanel(
                        palette = palette,
                        height = panelHeight,
                        onEmojiClick = { emoji ->
                            performHaptic()
                            onKeyText(emoji)
                        },
                        onBackspace = {
                            performHaptic()
                            onBackspace()
                        },
                        onClose = { onPanelChange(KeyboardPanel.KEYS) }
                    )
                }

                KeyboardPanel.TRANSLATE_TARGET -> {
                    KeyboardTranslateTargetPanel(
                        selected = state.targetTranslateLang,
                        palette = palette,
                        height = panelHeight,
                        onSelect = {
                            performHaptic()
                            onSelectTranslateTarget(it)
                        },
                        onClose = { onPanelChange(KeyboardPanel.KEYS) }
                    )
                }

                KeyboardPanel.TOOLBAR_CUSTOMIZE -> {
                    KeyboardToolbarCustomizePanel(
                        initialTab = state.customizeTabIndex,
                        config = toolbarConfig,
                        sizeConfig = sizeConfig,
                        themeConfig = themeConfig,
                        gradualPasteConfig = gradualPasteConfig,
                        palette = palette,
                        height = panelHeight + 12.dp,
                        onUpdateToolbar = onUpdateToolbarConfig,
                        onUpdateSizeConfig = onUpdateSizeConfig,
                        onSetKeyShape = onSetKeyShape,
                        onUpdateGradualPasteConfig = onUpdateGradualPasteConfig,
                        onClose = { onPanelChange(KeyboardPanel.KEYS) }
                    )
                }

                KeyboardPanel.PROOFREAD_DIFF -> {
                    val review = state.proofreadReview
                    if (review != null) {
                        KeyboardProofreadDiffPanel(
                            review = review,
                            palette = palette,
                            height = panelHeight,
                            onApply = {
                                performHaptic()
                                onApplyProofreadDiff()
                            },
                            onApplyGradually = {
                                if (sizeConfig.hapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onApplyProofreadDiffGradually()
                            },
                            onCopy = {
                                performHaptic()
                                onCopyProofreadDiff()
                            },
                            onClose = {
                                performHaptic()
                                onDismissProofreadDiff()
                            }
                        )
                    } else {
                        onPanelChange(KeyboardPanel.KEYS)
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun KeyboardToolbar(
    state: KeyboardUiState,
    toolbarConfig: ToolbarVisibilityConfig,
    sizeConfig: KeyboardSizeConfig,
    customBots: List<CustomBotItem>,
    palette: ResolvedKeyboardPalette,
    isPreviewMode: Boolean,
    onProofreadClick: () -> Unit,
    onRunCustomBot: (CustomBotItem) -> Unit,
    onVoiceClick: () -> Unit,
    onToggleTranslateTarget: () -> Unit,
    onUndoAiClick: () -> Unit,
    onPanelChange: (KeyboardPanel) -> Unit,
    onOpenSettings: () -> Unit
) {
    val visibleActions = remember(toolbarConfig) {
        toolbarConfig.order.filter { toolbarConfig.isVisible(it) }
    }
    val pinnedBots = remember(customBots) {
        customBots.filter { it.isPinnedToToolbar }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // User-reorderable toolbar actions (horizontal scroll if needed)
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            visibleActions.forEach { action ->
                when (action) {
                    ToolbarAction.PROOFREAD -> {
                        Surface(
                            onClick = onProofreadClick,
                            enabled = !state.isAiBusy,
                            shape = RoundedCornerShape(10.dp),
                            color = palette.accent,
                            contentColor = Color.White,
                            modifier = Modifier.height(if (isPreviewMode) 28.dp else 34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (state.isAiBusy) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoFixHigh,
                                        contentDescription = "AI Proofread",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (state.activeLanguage == KeyboardLanguage.ARABIC) "تصحيح" else "Fix AI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFontFamily
                                )
                            }
                        }
                    }

                    ToolbarAction.BOTS -> {
                        val btnSize = if (isPreviewMode) 28.dp else 34.dp
                        val active = state.panel == KeyboardPanel.BOTS
                        Box(
                            modifier = Modifier
                                .size(btnSize)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (active) palette.accent else palette.specialKey)
                                .clickable {
                                    onPanelChange(
                                        if (active) KeyboardPanel.KEYS else KeyboardPanel.BOTS
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SmartToy,
                                contentDescription = "AI Bots Assistant",
                                tint = if (active) Color.White else Color(0xFF4F8FF7),
                                modifier = Modifier.size(if (isPreviewMode) 15.dp else 18.dp)
                            )
                        }
                        // Each pinned user bot renders with its own dedicated icon & color right beside Bots
                        pinnedBots.forEach { bot ->
                            val botColor = Color(bot.accentArgb)
                            Box(
                                modifier = Modifier
                                    .size(btnSize)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(botColor.copy(alpha = 0.22f))
                                    .border(1.dp, botColor.copy(alpha = 0.7f), RoundedCornerShape(9.dp))
                                    .clickable(enabled = !state.isAiBusy) { onRunCustomBot(bot) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = botIconVector(bot.iconOption),
                                    contentDescription = bot.name,
                                    tint = botColor,
                                    modifier = Modifier.size(if (isPreviewMode) 15.dp else 18.dp)
                                )
                            }
                        }
                    }

                    ToolbarAction.CLIPBOARD -> {
                        ToolbarIconButton(
                            icon = Icons.Rounded.ContentPaste,
                            contentDescription = "Clipboard",
                            active = state.panel == KeyboardPanel.CLIPBOARD,
                            palette = palette,
                            isPreviewMode = isPreviewMode,
                            onClick = {
                                onPanelChange(
                                    if (state.panel == KeyboardPanel.CLIPBOARD) KeyboardPanel.KEYS
                                    else KeyboardPanel.CLIPBOARD
                                )
                            }
                        )
                    }

                    ToolbarAction.QUICK_TOOLS -> {
                        ToolbarIconButton(
                            icon = Icons.Rounded.GridView,
                            contentDescription = "Input Tools Grid",
                            active = state.panel == KeyboardPanel.QUICK_TOOLS,
                            palette = palette,
                            isPreviewMode = isPreviewMode,
                            onClick = {
                                onPanelChange(
                                    if (state.panel == KeyboardPanel.QUICK_TOOLS) KeyboardPanel.KEYS
                                    else KeyboardPanel.QUICK_TOOLS
                                )
                            }
                        )
                    }

                    ToolbarAction.VOICE -> {
                        val micActive = state.isVoiceListening
                        val btnSize = if (isPreviewMode) 28.dp else 34.dp
                        Box(
                            modifier = Modifier
                                .size(btnSize)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (micActive) Color(0xFFEF4444) else palette.specialKey)
                                .clickable(onClick = onVoiceClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = "Voice Typing",
                                tint = if (micActive) Color.White else palette.text.copy(alpha = 0.85f),
                                modifier = Modifier.size(if (isPreviewMode) 15.dp else 18.dp)
                            )
                        }
                    }

                    ToolbarAction.PASSWORDS -> {
                        ToolbarIconButton(
                            icon = Icons.Rounded.VpnKey,
                            contentDescription = "Password Manager",
                            active = state.panel == KeyboardPanel.PASSWORDS,
                            palette = palette,
                            isPreviewMode = isPreviewMode,
                            onClick = {
                                onPanelChange(
                                    if (state.panel == KeyboardPanel.PASSWORDS) KeyboardPanel.KEYS
                                    else KeyboardPanel.PASSWORDS
                                )
                            }
                        )
                    }

                    ToolbarAction.TRANSLATE -> {
                        Surface(
                            onClick = onToggleTranslateTarget,
                            enabled = !state.isAiBusy,
                            shape = RoundedCornerShape(10.dp),
                            color = if (state.panel == KeyboardPanel.TRANSLATE_TARGET) palette.accent else palette.specialKey,
                            contentColor = if (state.panel == KeyboardPanel.TRANSLATE_TARGET) Color.White else palette.text,
                            modifier = Modifier.height(if (isPreviewMode) 28.dp else 34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Translate,
                                    contentDescription = "Translate",
                                    tint = if (state.panel == KeyboardPanel.TRANSLATE_TARGET) Color.White else palette.accent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = state.targetTranslateLang.flagEmoji,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    ToolbarAction.EMOJI -> {
                        ToolbarIconButton(
                            icon = Icons.Rounded.EmojiEmotions,
                            contentDescription = "Emoji",
                            active = state.panel == KeyboardPanel.EMOJI,
                            palette = palette,
                            isPreviewMode = isPreviewMode,
                            onClick = {
                                onPanelChange(
                                    if (state.panel == KeyboardPanel.EMOJI) KeyboardPanel.KEYS
                                    else KeyboardPanel.EMOJI
                                )
                            }
                        )
                    }

                    ToolbarAction.SETTINGS -> {
                        ToolbarIconButton(
                            icon = Icons.Rounded.Settings,
                            contentDescription = "Keyboard Settings",
                            active = false,
                            palette = palette,
                            isPreviewMode = isPreviewMode,
                            onClick = onOpenSettings
                        )
                    }
                }
            }
        }

        // Incognito active badge if forced
        if (sizeConfig.incognitoMode) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF8B5CF6).copy(alpha = 0.25f),
                modifier = Modifier.height(if (isPreviewMode) 26.dp else 30.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = "Incognito Mode Active",
                        tint = Color(0xFFC4B5FD),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Undo AI chip when available
        if (state.canUndoAi) {
            Surface(
                onClick = onUndoAiClick,
                shape = RoundedCornerShape(10.dp),
                color = palette.accent.copy(alpha = 0.2f),
                contentColor = palette.text,
                modifier = Modifier.height(if (isPreviewMode) 28.dp else 34.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Undo,
                        contentDescription = "Undo AI edit",
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (state.activeLanguage == KeyboardLanguage.ARABIC) "تراجع" else "Undo",
                        fontSize = 10.sp,
                        fontFamily = CairoFontFamily
                    )
                }
            }
        }

        // Always-visible Customize Toolbar & Number Row Button
        ToolbarIconButton(
            icon = Icons.Rounded.Tune,
            contentDescription = "Customize Toolbar Icons & Number Row",
            active = state.panel == KeyboardPanel.TOOLBAR_CUSTOMIZE,
            palette = palette,
            isPreviewMode = isPreviewMode,
            onClick = {
                onPanelChange(
                    if (state.panel == KeyboardPanel.TOOLBAR_CUSTOMIZE) KeyboardPanel.KEYS
                    else KeyboardPanel.TOOLBAR_CUSTOMIZE
                )
            }
        )
    }
}

@Composable
private fun ToolbarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean,
    palette: ResolvedKeyboardPalette,
    isPreviewMode: Boolean,
    onClick: () -> Unit
) {
    val size = if (isPreviewMode) 28.dp else 34.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) palette.accent.copy(alpha = 0.25f) else palette.specialKey)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) palette.accent else palette.text.copy(alpha = 0.85f),
            modifier = Modifier.size(if (isPreviewMode) 15.dp else 18.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuggestionStrip(
    activeLanguage: KeyboardLanguage,
    suggestions: List<SuggestionCandidate>,
    recentClipText: String?,
    isIncognito: Boolean,
    palette: ResolvedKeyboardPalette,
    isPreviewMode: Boolean,
    onSuggestionClick: (SuggestionCandidate) -> Unit,
    onLatestClipTap: (String) -> Unit,
    onLatestClipLongPress: (String) -> Unit
) {
    val height = if (isPreviewMode) 26.dp else 34.dp
    if (suggestions.isEmpty() && recentClipText.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isIncognito) "وضع التصفح المتخفي • Incognito" else "Bayan AI • بيان",
                color = if (isIncognito) Color(0xFFA78BFA).copy(alpha = 0.7f) else palette.text.copy(alpha = 0.35f),
                fontSize = 11.sp,
                fontFamily = CairoFontFamily
            )
        }
        return
    }
    val stripDirection = if (activeLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides stripDirection) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!recentClipText.isNullOrBlank()) {
                item(key = "latest_clip_suggestion") {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 175.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.key)
                            .border(
                                width = 0.8.dp,
                                color = palette.accent.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .combinedClickable(
                                onClick = { onLatestClipTap(recentClipText) },
                                onLongClick = { onLatestClipLongPress(recentClipText) }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentPaste,
                            contentDescription = "Last Copied Text",
                            tint = palette.accent,
                            modifier = Modifier.size(if (isPreviewMode) 11.dp else 13.dp)
                        )
                        Text(
                            text = recentClipText.replace('\n', ' '),
                            color = palette.text,
                            fontSize = if (isPreviewMode) 10.5.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = CairoFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            items(suggestions) { item ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (item.isSpellCorrection || item.targetLanguageSwitch != null)
                                palette.accent.copy(alpha = 0.16f)
                            else palette.specialKey.copy(alpha = 0.75f)
                        )
                        .clickable { onSuggestionClick(item) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (item.targetLanguageSwitch != null) {
                        Text(
                            text = item.targetLanguageSwitch.shortBadge,
                            color = palette.accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = item.text,
                        color = palette.text,
                        fontSize = if (isPreviewMode) 11.sp else 13.sp,
                        fontWeight = if (item.isSpellCorrection) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = CairoFontFamily,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyboardKeyButton(
    spec: KeySpec,
    isShifted: Boolean,
    isCapsLocked: Boolean,
    palette: ResolvedKeyboardPalette,
    keyHeight: Dp,
    fontSp: Float,
    isPreviewMode: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onLongPressChar: (String) -> Unit,
    onBackspaceRepeat: () -> Unit,
    onSpaceSwipe: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showLongPressOptions by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val noBg = palette.keyShape == KeyButtonShape.NO_BACKGROUND
    val bgColor = when (spec.kind) {
        KeyKind.ENTER -> palette.accent
        KeyKind.SHIFT -> when {
            isShifted || isCapsLocked -> palette.accent.copy(alpha = 0.35f)
            noBg -> Color.Transparent
            else -> palette.specialKey
        }
        KeyKind.BACKSPACE, KeyKind.SYMBOLS_TOGGLE, KeyKind.LANGUAGE_SWITCH, KeyKind.EMOJI ->
            if (noBg) Color.Transparent else palette.specialKey
        KeyKind.CHARACTER, KeyKind.SPACE ->
            if (noBg) Color.Transparent else palette.key
    }.let { base ->
        if (isPressed) palette.accent.copy(alpha = 0.35f) else base
    }

    val fgColor = if (spec.kind == KeyKind.ENTER) Color.White else palette.text
    val shape = when (palette.keyShape) {
        KeyButtonShape.SHARP_SQUARE -> RoundedCornerShape(3.dp)
        KeyButtonShape.ROUNDED -> RoundedCornerShape(palette.cornerRadiusDp.dp)
        KeyButtonShape.NO_BACKGROUND -> RoundedCornerShape(8.dp)
        KeyButtonShape.SOFT_CIRCLE -> RoundedCornerShape(50)
    }

    val displayLabel = when {
        spec.kind == KeyKind.CHARACTER && isShifted -> spec.shiftLabel
        else -> spec.label
    }

    Box(
        modifier = modifier
            .height(keyHeight)
            .clip(shape)
            .background(bgColor)
            .then(
                if (palette.showBorders && (!noBg || spec.kind == KeyKind.SPACE)) {
                    Modifier.border(
                        width = 0.7.dp,
                        color = palette.text.copy(alpha = if (palette.isDark) 0.08f else 0.12f),
                        shape = shape
                    )
                } else Modifier
            )
            .pointerInput(spec, isShifted, isCapsLocked) {
                if (spec.kind == KeyKind.SPACE) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                        onDragEnd = {
                            if (totalDrag > 45f) onSpaceSwipe(1)
                            else if (totalDrag < -45f) onSpaceSwipe(-1)
                        }
                    )
                }
            }
            .pointerInput(spec, isShifted, isCapsLocked) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        var repeatJob: kotlinx.coroutines.Job? = null
                        if (spec.kind == KeyKind.BACKSPACE) {
                            repeatJob = scope.launch {
                                delay(380)
                                while (true) {
                                    onBackspaceRepeat()
                                    delay(65)
                                }
                            }
                        }
                        val released = tryAwaitRelease()
                        repeatJob?.cancel()
                        isPressed = false
                        if (released && !showLongPressOptions) {
                            onTap()
                        }
                    },
                    onLongPress = {
                        if (spec.longPressChars.isNotEmpty()) {
                            showLongPressOptions = true
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        when (spec.kind) {
            KeyKind.BACKSPACE -> Icon(
                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                contentDescription = "Backspace",
                tint = fgColor,
                modifier = Modifier.size(if (isPreviewMode) 15.dp else 19.dp)
            )
            KeyKind.ENTER -> Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardReturn,
                contentDescription = "Enter",
                tint = fgColor,
                modifier = Modifier.size(if (isPreviewMode) 16.dp else 20.dp)
            )
            KeyKind.LANGUAGE_SWITCH -> Icon(
                imageVector = Icons.Rounded.Language,
                contentDescription = "Switch Language",
                tint = fgColor,
                modifier = Modifier.size(if (isPreviewMode) 15.dp else 19.dp)
            )
            KeyKind.SHIFT -> {
                if (spec.label == "⇧") {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Shift",
                        tint = if (isCapsLocked) palette.accent else fgColor,
                        modifier = Modifier.size(if (isPreviewMode) 18.dp else 22.dp)
                    )
                } else {
                    Text(
                        text = displayLabel,
                        color = fgColor,
                        fontSize = (fontSp * 0.6f).sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = CairoFontFamily,
                        maxLines = 1
                    )
                }
            }
            KeyKind.SPACE -> {
                Text(
                    text = spec.label,
                    color = fgColor.copy(alpha = 0.65f),
                    fontSize = (fontSp * 0.68f).sp,
                    fontFamily = CairoFontFamily,
                    maxLines = 1
                )
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = displayLabel,
                        color = fgColor,
                        fontSize = if (spec.kind == KeyKind.SYMBOLS_TOGGLE) (fontSp * 0.66f).sp else fontSp.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = CairoFontFamily,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    if (spec.longPressChars.isNotEmpty() && !isPreviewMode) {
                        Text(
                            text = spec.longPressChars.first(),
                            color = fgColor.copy(alpha = 0.4f),
                            fontSize = 8.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 3.dp, top = 1.dp)
                        )
                    }
                }
            }
        }

        // Long-press secondary variants popup bar inside key overlay
        if (showLongPressOptions && spec.longPressChars.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.accent,
                shadowElevation = 6.dp,
                modifier = Modifier.padding(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    spec.longPressChars.take(4).forEach { variant ->
                        Text(
                            text = variant,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    onLongPressChar(variant)
                                    showLongPressOptions = false
                                }
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyboardClipboardPanel(
    items: List<ClipboardEntity>,
    gradualEnabled: Boolean,
    palette: ResolvedKeyboardPalette,
    height: Dp,
    onPaste: (String) -> Unit,
    onGradualPaste: (String) -> Unit,
    onPinToggle: (ClipboardEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onClose: () -> Unit
) {
    val unpinned = remember(items) { items.filter { !it.isPinned } }
    val pinned = remember(items) { items.filter { it.isPinned } }
    val pinnedRows = remember(pinned) { pinned.chunked(2) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "الحافظة والمثبتة • Clipboard",
                color = palette.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (gradualEnabled) "⚡ مطوّل = لصق تدريجي" else "مطوّل = لصق تدريجي (غير مفعل)",
                color = if (gradualEnabled) Color(0xFFF59E0B) else palette.text.copy(alpha = 0.55f),
                fontSize = 9.5.sp,
                fontFamily = CairoFontFamily
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(palette.specialKey)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Clipboard",
                    tint = palette.text,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "لا توجد نصوص منسوخة بعد",
                    color = palette.text.copy(alpha = 0.55f),
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Horizontal Strip for Unpinned items ABOVE the 2-column Pinned Grid
                if (unpinned.isNotEmpty()) {
                    item(key = "unpinned_strip") {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "حديثة (غير مثبتة) • Recent",
                                color = palette.text.copy(alpha = 0.65f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = CairoFontFamily
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(unpinned, key = { "u_${it.id}" }) { clip ->
                                    Row(
                                        modifier = Modifier
                                            .widthIn(min = 120.dp, max = 210.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(palette.specialKey)
                                            .border(
                                                width = 0.6.dp,
                                                color = palette.text.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .combinedClickable(
                                                onClick = { onPaste(clip.text) },
                                                onLongClick = { onGradualPaste(clip.text) }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = clip.text,
                                            color = palette.text,
                                            fontSize = 11.sp,
                                            fontFamily = CairoFontFamily,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable { onPinToggle(clip) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PushPin,
                                                contentDescription = "Pin",
                                                tint = palette.text.copy(alpha = 0.5f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable { onDelete(clip.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = "Delete Clip",
                                                tint = palette.text.copy(alpha = 0.5f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Two-Column Grid for Pinned items
                if (pinnedRows.isNotEmpty()) {
                    item(key = "pinned_header") {
                        Text(
                            text = "النصوص المثبتة • Pinned",
                            color = palette.accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFontFamily
                        )
                    }
                    items(pinnedRows.size, key = { "prow_$it" }) { rowIdx ->
                        val pair = pinnedRows[rowIdx]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            pair.forEach { clip ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(palette.key)
                                        .border(
                                            width = 1.dp,
                                            color = palette.accent.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .combinedClickable(
                                            onClick = { onPaste(clip.text) },
                                            onLongClick = { onGradualPaste(clip.text) }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = clip.text,
                                        color = palette.text,
                                        fontSize = 11.5.sp,
                                        fontFamily = CairoFontFamily,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable { onPinToggle(clip) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PushPin,
                                                contentDescription = "Unpin",
                                                tint = palette.accent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable { onDelete(clip.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = "Delete Clip",
                                                tint = palette.text.copy(alpha = 0.5f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardPasswordsPanel(
    entries: List<PasswordEntryItem>,
    palette: ResolvedKeyboardPalette,
    height: Dp,
    onInsertCredential: (String) -> Unit,
    onOpenVaultManager: () -> Unit,
    onClose: () -> Unit
) {
    var selectedEntry by remember { mutableStateOf<PasswordEntryItem?>(null) }
    var revealPassword by remember { mutableStateOf(false) }
    var usernameInsertedBanner by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header matching screenshot: Blue Vault icon on left, "مدير كلمات المرور" + Back/Close on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                        .clickable(onClick = onOpenVaultManager),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VpnKey,
                        contentDescription = "Manage Passwords",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Text(
                    text = "مدير كلمات المرور",
                    color = palette.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFontFamily,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(palette.specialKey)
                        .clickable(onClick = onOpenVaultManager),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Password",
                        tint = palette.text,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(palette.specialKey)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Passwords",
                        tint = palette.text,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (entries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.key)
                        .clickable(onClick = onOpenVaultManager)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VpnKey,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "لا توجد كلمات مرور محفوظة بعد",
                        color = palette.text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily
                    )
                    Text(
                        text = "اضغط هنا لإضافة حساباتك بأمان",
                        color = palette.text.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        fontFamily = CairoFontFamily
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries, key = { it.id }) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.key)
                                .border(
                                    width = 0.7.dp,
                                    color = palette.text.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    revealPassword = false
                                    usernameInsertedBanner = false
                                    selectedEntry = entry
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = entry.siteName.ifBlank { entry.username },
                                color = palette.text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = CairoFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            ServiceBrandIcon(
                                brand = entry.brand,
                                size = 30.dp
                            )
                        }
                    }
                }
            }
        }

        // Floating Credential Popup Overlay (Username + Masked Password rows) matching screenshot
        val activeEntry = selectedEntry
        if (activeEntry != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.48f))
                    .clickable { selectedEntry = null },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (palette.isDark) Color(0xFF1F222A) else palette.key,
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .border(1.dp, palette.text.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header row with brand + site name + close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ServiceBrandIcon(brand = activeEntry.brand, size = 26.dp)
                            Text(
                                text = activeEntry.siteName,
                                color = palette.text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedEntry = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close popup",
                                    tint = palette.text.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        if (usernameInsertedBanner) {
                            Text(
                                text = "✓ تم لصق البريد/اسم المستخدم — يمكنك الآن لصق كلمة المرور أو الانتقال لحقلها",
                                color = Color(0xFF10B981),
                                fontSize = 10.5.sp,
                                fontFamily = CairoFontFamily
                            )
                        }

                        // Username / Email row (Tap to insert AND keep Password Manager + popup open for password step!)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.specialKey.copy(alpha = 0.6f))
                                .clickable {
                                    onInsertCredential(activeEntry.username)
                                    usernameInsertedBanner = true
                                }
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = activeEntry.username,
                                color = palette.text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Insert Username",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Password row (Tap to insert and stay in Password Manager panel, eye icon to reveal/hide)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.specialKey.copy(alpha = 0.6f))
                                .clickable {
                                    onInsertCredential(activeEntry.password)
                                    selectedEntry = null
                                }
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .clickable { revealPassword = !revealPassword },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (revealPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = palette.text.copy(alpha = 0.65f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (revealPassword) activeEntry.password else "•".repeat(activeEntry.password.length.coerceIn(8, 14)),
                                color = palette.text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Insert Password",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardEmojiPanel(
    palette: ResolvedKeyboardPalette,
    height: Dp,
    onEmojiClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClose: () -> Unit
) {
    val allEmojis = remember { KeyboardLayouts.emojiCategories.flatMap { it.second } }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الرموز التعبيرية • Emoji",
                color = palette.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(palette.specialKey)
                    .clickable(onClick = onBackspace),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = "Backspace",
                    tint = palette.text,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(palette.specialKey)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Emoji",
                    tint = palette.text,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(40.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(allEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEmojiClick(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
private fun KeyboardTranslateTargetPanel(
    selected: TargetTranslateLanguage,
    palette: ResolvedKeyboardPalette,
    height: Dp,
    onSelect: (TargetTranslateLanguage) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "اختر لغة الترجمة الهدف • Target Language",
                color = palette.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(palette.specialKey)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = palette.text,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(TargetTranslateLanguage.entries) { lang ->
                val isSelected = lang == selected
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) palette.accent else palette.key)
                        .border(
                            width = 0.7.dp,
                            color = if (isSelected) palette.accent else palette.text.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(lang) }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = lang.flagEmoji,
                        fontSize = 18.sp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lang.labelAr,
                            color = if (isSelected) Color.White else palette.text,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = lang.labelEn,
                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else palette.text.copy(alpha = 0.65f),
                            fontSize = 10.sp,
                            fontFamily = CairoFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardToolbarCustomizePanel(
    initialTab: Int,
    config: ToolbarVisibilityConfig,
    sizeConfig: KeyboardSizeConfig,
    themeConfig: KeyboardThemeConfig,
    gradualPasteConfig: GradualPasteConfig,
    palette: ResolvedKeyboardPalette,
    height: Dp,
    onUpdateToolbar: ((ToolbarVisibilityConfig) -> ToolbarVisibilityConfig) -> Unit,
    onUpdateSizeConfig: ((KeyboardSizeConfig) -> KeyboardSizeConfig) -> Unit,
    onSetKeyShape: (KeyButtonShape) -> Unit,
    onUpdateGradualPasteConfig: ((GradualPasteConfig) -> GradualPasteConfig) -> Unit,
    onClose: () -> Unit
) {
    var activeTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val toolbarRows = remember(config.order) { config.order.chunked(2) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 2 Quick Tabs: Toolbar Grid & Number Row | Key Shape & Gradual Paste ⚡
            listOf(
                0 to "الشريط والأرقام",
                1 to "الشكل واللصق ⚡"
            ).forEach { (tabIdx, title) ->
                val selected = activeTab == tabIdx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selected) palette.accent else palette.specialKey)
                        .clickable { activeTab = tabIdx }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selected) Color.White else palette.text,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(palette.specialKey)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = palette.text,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (activeTab == 0) {
                // Number Row Show/Hide + Arabic/English Numeral Selector
                item(key = "number_row_config") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.key)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "صف الأرقام بالأعلى • Number Row",
                                color = palette.text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFontFamily,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = sizeConfig.showNumberRow,
                                onCheckedChange = { checked ->
                                    onUpdateSizeConfig { it.copy(showNumberRow = checked) }
                                }
                            )
                        }
                        if (sizeConfig.showNumberRow) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                NumberRowStyle.entries.forEach { style ->
                                    val selected = sizeConfig.numberRowStyle == style
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) palette.accent else palette.specialKey)
                                            .clickable {
                                                onUpdateSizeConfig { it.copy(numberRowStyle = style) }
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = style.labelAr,
                                            color = if (selected) Color.White else palette.text,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = CairoFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Grid (2 columns) for Toolbar Actions instead of stacked list!
                items(toolbarRows.size, key = { "tb_grid_row_$it" }) { rowIdx ->
                    val pair = toolbarRows[rowIdx]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pair.forEach { action ->
                            val idx = config.order.indexOf(action)
                            val visible = config.isVisible(action)
                            val canMoveLeft = idx > 0
                            val canMoveRight = idx < config.order.lastIndex

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (visible) palette.key else palette.specialKey.copy(alpha = 0.55f))
                                    .border(
                                        width = 1.dp,
                                        color = if (visible) palette.accent.copy(alpha = 0.65f) else palette.text.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onUpdateToolbar { it.withVisibility(action, !visible) }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = action.labelAr,
                                        color = palette.text,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (visible) palette.accent else palette.text.copy(alpha = 0.18f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (visible) "مفعّل" else "مخفي",
                                            color = if (visible) Color.White else palette.text.copy(alpha = 0.7f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = CairoFontFamily
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(if (canMoveLeft) palette.specialKey else palette.specialKey.copy(alpha = 0.25f))
                                            .clickable(enabled = canMoveLeft) {
                                                onUpdateToolbar { it.moveAction(action, -1) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ChevronLeft,
                                            contentDescription = "Move Left",
                                            tint = if (canMoveLeft) palette.text else palette.text.copy(alpha = 0.3f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Text(
                                        text = "#${idx + 1}",
                                        color = palette.text.copy(alpha = 0.55f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(if (canMoveRight) palette.specialKey else palette.specialKey.copy(alpha = 0.25f))
                                            .clickable(enabled = canMoveRight) {
                                                onUpdateToolbar { it.moveAction(action, +1) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ChevronRight,
                                            contentDescription = "Move Right",
                                            tint = if (canMoveRight) palette.text else palette.text.copy(alpha = 0.3f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // Tab 1: Key Button Shape Grid + Gradual Paste 1..50 Settings
                item(key = "key_shape_grid") {
                    KeyButtonShapeGrid(
                        selectedShape = themeConfig.keyShape,
                        isAr = true,
                        accentColor = Color(0xFF38BDF8),
                        surfaceColor = palette.key,
                        textColor = palette.text,
                        compact = true,
                        onSelectShape = onSetKeyShape
                    )
                }
                item(key = "gradual_paste_settings") {
                    GradualPasteSettingsSection(
                        config = gradualPasteConfig,
                        isAr = true,
                        accentColor = Color(0xFF38BDF8),
                        cardColor = palette.key,
                        pillColor = palette.specialKey,
                        textColor = palette.text,
                        compact = true,
                        onUpdateConfig = onUpdateGradualPasteConfig
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyboardProofreadDiffPanel(
    review: ProofreadDiffReview,
    palette: ResolvedKeyboardPalette,
    height: Dp,
    onApply: () -> Unit,
    onApplyGradually: () -> Unit,
    onCopy: () -> Unit,
    onClose: () -> Unit
) {
    val diffAnnotated = remember(review.originalText, review.correctedText, palette.text) {
        WordDiffUtil.buildDiffAnnotatedString(
            original = review.originalText,
            corrected = review.correctedText,
            unchangedColor = palette.text
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header + Color Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "نتيجة التصحيح",
                color = palette.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                modifier = Modifier.weight(1f)
            )
            // Legend: Red = Wrong, Green = Corrected
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.18f)
            ) {
                Text(
                    text = "خطأ",
                    color = Color(0xFFEF4444),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFontFamily,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF10B981).copy(alpha = 0.18f)
            ) {
                Text(
                    text = "مصحّح",
                    color = Color(0xFF10B981),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFontFamily,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(palette.specialKey)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Diff",
                    tint = palette.text,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Scrollable Diff Preview Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.key)
                .border(1.dp, palette.text.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            if (review.originalText.isBlank()) {
                Text(
                    text = review.correctedText,
                    color = palette.text,
                    fontSize = 14.sp,
                    fontFamily = CairoFontFamily,
                    lineHeight = 22.sp
                )
            } else if (review.originalText.trim() == review.correctedText.trim()) {
                Text(
                    text = "✓ النص سليم ولا توجد أخطاء:\n${review.correctedText}",
                    color = Color(0xFF10B981),
                    fontSize = 14.sp,
                    fontFamily = CairoFontFamily,
                    lineHeight = 22.sp
                )
            } else {
                Text(
                    text = diffAnnotated,
                    fontSize = 14.sp,
                    fontFamily = CairoFontFamily,
                    lineHeight = 22.sp
                )
            }
        }

        // Bottom Action Buttons: Copy & Apply (Tap = instant replace, Long Press = gradual paste)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = onCopy,
                shape = RoundedCornerShape(10.dp),
                color = palette.specialKey,
                contentColor = palette.text,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("proofread_copy_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "نسخ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.accent,
                contentColor = Color.White,
                modifier = Modifier
                    .weight(1.25f)
                    .height(42.dp)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(10.dp))
                    .combinedClickable(
                        onClick = onApply,
                        onLongClick = onApplyGradually
                    )
                    .testTag("proofread_apply_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = "Apply", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "تطبيق",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "⚡ مطوّل",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = CairoFontFamily,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OneHandedSideStrip(
    currentMode: OneHandedMode,
    palette: ResolvedKeyboardPalette,
    height: Dp,
    onExpand: () -> Unit,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(40.dp)
            .height(height)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(palette.specialKey)
                .clickable(onClick = onExpand),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.OpenInFull,
                contentDescription = "Full Keyboard Width",
                tint = palette.accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(palette.specialKey)
                .clickable(onClick = onFlip),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SwapHoriz,
                contentDescription = if (currentMode == OneHandedMode.LEFT) "Move Right" else "Move Left",
                tint = palette.text,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun KeyboardQuickToolsPanel(
    state: KeyboardUiState,
    sizeConfig: KeyboardSizeConfig,
    palette: ResolvedKeyboardPalette,
    height: Dp,
    onToolClick: (QuickInputTool) -> Unit,
    onClose: () -> Unit
) {
    val isAr = state.activeLanguage.isRtl || state.activeLanguage == KeyboardLanguage.ARABIC
    val categories = remember { QuickToolCategory.entries }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.GridView,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = if (isAr) "شبكة أدوات الإدخال والتحرير" else "Quick Input & Edit Tools",
                color = palette.text,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (sizeConfig.incognitoMode) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.24f)
                ) {
                    Text(
                        text = if (isAr) "متخفي" else "Incognito",
                        color = Color(0xFFC4B5FD),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(palette.specialKey)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Quick Tools",
                    tint = palette.text,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            categories.forEach { cat ->
                val catTools = QuickInputTool.entries.filter { it.category == cat }
                item(
                    key = "cat_${cat.name}",
                    span = { GridItemSpan(4) }
                ) {
                    Text(
                        text = if (isAr) cat.labelAr else cat.labelEn,
                        color = palette.accent,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily,
                        textAlign = if (isAr) TextAlign.End else TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 1.dp, start = 2.dp, end = 2.dp)
                    )
                }

                items(catTools, key = { it.id }) { tool ->
                    val isActiveToggle = when (tool) {
                        QuickInputTool.NUMPAD -> state.layer == KeyboardLayer.NUMPAD
                        QuickInputTool.ONE_HANDED -> sizeConfig.oneHandedMode != OneHandedMode.OFF
                        QuickInputTool.SPLIT_KEYBOARD -> sizeConfig.splitKeyboard
                        QuickInputTool.INCOGNITO -> sizeConfig.incognitoMode
                        QuickInputTool.AUTO_CORRECT -> sizeConfig.autoCorrectEnabled
                        QuickInputTool.VOICE_INPUT -> state.isVoiceListening
                        else -> false
                    }
                    val tileTint = when {
                        tool == QuickInputTool.INCOGNITO && isActiveToggle -> Color(0xFF8B5CF6)
                        tool == QuickInputTool.CLEAR_CLIPBOARD -> Color(0xFFEF4444)
                        isActiveToggle -> palette.accent
                        else -> palette.accent
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isActiveToggle) tileTint.copy(alpha = 0.22f)
                                else palette.key
                            )
                            .border(
                                width = if (isActiveToggle) 1.2.dp else 0.7.dp,
                                color = if (isActiveToggle) tileTint else palette.text.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onToolClick(tool) }
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isActiveToggle) tileTint.copy(alpha = 0.26f)
                                    else palette.specialKey
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = quickToolIcon(tool),
                                contentDescription = if (isAr) tool.labelAr else tool.labelEn,
                                tint = if (isActiveToggle || tool == QuickInputTool.CLEAR_CLIPBOARD) tileTint else palette.text,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (isAr) tool.labelAr else tool.labelEn,
                            color = palette.text,
                            fontSize = 9.5.sp,
                            fontWeight = if (isActiveToggle) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = CairoFontFamily,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun quickToolIcon(tool: QuickInputTool): ImageVector = when (tool) {
    QuickInputTool.SELECT_ALL -> Icons.Rounded.SelectAll
    QuickInputTool.SELECT_WORD -> Icons.Rounded.TextFields
    QuickInputTool.COPY -> Icons.Rounded.ContentCopy
    QuickInputTool.CUT -> Icons.Rounded.ContentCut
    QuickInputTool.PASTE -> Icons.Rounded.ContentPaste
    QuickInputTool.UNDO -> Icons.AutoMirrored.Rounded.Undo
    QuickInputTool.REDO -> Icons.AutoMirrored.Rounded.Redo
    QuickInputTool.CLEAR_CLIPBOARD -> Icons.Rounded.DeleteSweep
    QuickInputTool.CURSOR_LEFT -> Icons.Rounded.ChevronLeft
    QuickInputTool.CURSOR_RIGHT -> Icons.Rounded.ChevronRight
    QuickInputTool.CURSOR_UP -> Icons.Rounded.KeyboardArrowUp
    QuickInputTool.CURSOR_DOWN -> Icons.Rounded.KeyboardArrowDown
    QuickInputTool.WORD_LEFT -> Icons.Rounded.KeyboardDoubleArrowLeft
    QuickInputTool.WORD_RIGHT -> Icons.Rounded.KeyboardDoubleArrowRight
    QuickInputTool.LINE_START -> Icons.Rounded.FirstPage
    QuickInputTool.LINE_END -> Icons.AutoMirrored.Rounded.LastPage
    QuickInputTool.PAGE_UP -> Icons.Rounded.KeyboardDoubleArrowUp
    QuickInputTool.PAGE_DOWN -> Icons.Rounded.KeyboardDoubleArrowDown
    QuickInputTool.DOC_START -> Icons.Rounded.VerticalAlignTop
    QuickInputTool.DOC_END -> Icons.Rounded.VerticalAlignBottom
    QuickInputTool.NUMPAD -> Icons.Rounded.Dialpad
    QuickInputTool.ONE_HANDED -> Icons.Rounded.BackHand
    QuickInputTool.SPLIT_KEYBOARD -> Icons.Rounded.ViewWeek
    QuickInputTool.INCOGNITO -> Icons.Rounded.Shield
    QuickInputTool.AUTO_CORRECT -> Icons.Rounded.Spellcheck
    QuickInputTool.VOICE_INPUT -> Icons.Rounded.Mic
    QuickInputTool.CLIPBOARD -> Icons.AutoMirrored.Rounded.Assignment
    QuickInputTool.EMOJI -> Icons.Rounded.EmojiEmotions
    QuickInputTool.SETTINGS -> Icons.Rounded.Settings
}

@Composable
private fun KeyboardBotsPanel(
    customBots: List<CustomBotItem>,
    isAiBusy: Boolean,
    palette: ResolvedKeyboardPalette,
    height: Dp,
    onRunCustomBot: (CustomBotItem) -> Unit,
    onRunBuiltinAction: (BuiltinBotAction) -> Unit,
    onCreateBot: () -> Unit,
    onManageBots: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header: مساعد روبوت + إنشاء بوت + إدارة + إغلاق
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4F8FF7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "مساعد روبوت",
                color = palette.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                modifier = Modifier.weight(1f)
            )
            Surface(
                onClick = onCreateBot,
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4F8FF7),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                    Text(
                        text = "إنشاء بوت",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily
                    )
                }
            }
            if (customBots.isNotEmpty()) {
                Surface(
                    onClick = onManageBots,
                    shape = RoundedCornerShape(8.dp),
                    color = palette.specialKey,
                    contentColor = palette.text
                ) {
                    Text(
                        text = "إدارة",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(palette.specialKey)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Bots Panel",
                    tint = palette.text,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            if (customBots.isEmpty()) {
                item(span = { GridItemSpan(2) }, key = "bot_promo_banner") {
                    Surface(
                        onClick = onCreateBot,
                        shape = RoundedCornerShape(14.dp),
                        color = palette.key,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, palette.text.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4F8FF7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SmartToy,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "مساعد روبوت",
                                    color = palette.text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFontFamily
                                )
                                Text(
                                    text = "أنشئ مساعدك الخاص للتعامل مع جميع متطلباتك الشخصية",
                                    color = palette.text.copy(alpha = 0.72f),
                                    fontSize = 11.sp,
                                    fontFamily = CairoFontFamily,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            } else {
                item(span = { GridItemSpan(2) }, key = "my_bots_header") {
                    Text(
                        text = "البوتات الخاصة بي (${customBots.size})",
                        color = palette.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                items(customBots, key = { "custom_bot_${it.id}" }) { bot ->
                    val botColor = Color(bot.accentArgb)
                    Surface(
                        onClick = { onRunCustomBot(bot) },
                        enabled = !isAiBusy,
                        shape = RoundedCornerShape(12.dp),
                        color = palette.key,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, botColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(botColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = botIconVector(bot.iconOption),
                                    contentDescription = bot.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bot.name,
                                    color = palette.text,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (bot.knowledgeSources.isNotEmpty()) {
                                    Text(
                                        text = "${bot.knowledgeSources.size} مصدر معرفة",
                                        color = palette.text.copy(alpha = 0.6f),
                                        fontSize = 9.sp,
                                        fontFamily = CairoFontFamily,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(2) }, key = "builtin_ai_header") {
                Text(
                    text = "أدوات الذكاء السريعة",
                    color = palette.text.copy(alpha = 0.65f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFontFamily,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            items(BuiltinBotAction.entries, key = { "builtin_${it.id}" }) { action ->
                Surface(
                    onClick = { onRunBuiltinAction(action) },
                    enabled = !isAiBusy,
                    shape = RoundedCornerShape(12.dp),
                    color = palette.key,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(0.8.dp, palette.text.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = builtinBotActionIcon(action),
                            contentDescription = action.labelAr,
                            tint = palette.text.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = action.labelAr,
                            color = palette.text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = CairoFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

