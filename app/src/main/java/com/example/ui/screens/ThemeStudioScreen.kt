package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.rounded.Bolt
import com.example.data.local.ClipboardEntity
import com.example.data.model.GradualPasteConfig
import com.example.data.model.KeyButtonShape
import com.example.data.model.KeyboardLanguage
import com.example.data.model.KeyboardFontStyle
import com.example.data.model.KeyboardSizeConfig
import com.example.data.model.KeyboardThemeConfig
import com.example.data.model.KeyboardThemePreset
import com.example.data.model.KeyboardThemePresets
import com.example.data.model.NumberRowStyle
import com.example.data.model.PasswordEntryItem
import com.example.data.model.ThemeMode
import com.example.data.model.ToolbarVisibilityConfig
import com.example.ime.BayanKeyboardView
import com.example.ime.KeyboardPanel
import com.example.ime.KeyboardUiState
import com.example.ui.AppStrings.pick
import com.example.ui.theme.fontFamilyFor
import com.example.ui.components.GradualPasteSettingsSection
import com.example.ui.components.KeyButtonShapeGrid
import kotlin.math.roundToInt

private val customColorSwatches: List<Long> = listOf(
    0xFF0B1120, 0xFF1E293B, 0xFF09090B, 0xFF18181B, 0xFF082032, 0xFF1A374D,
    0xFFFFFFFF, 0xFFF4F6FA, 0xFFEAEFF5, 0xFFF2ECE1, 0xFFF9ECEF, 0xFFD8E1EC,
    0xFF10B981, 0xFF0D9488, 0xFF3B82F6, 0xFF6366F1, 0xFF8B5CF6, 0xFFE11D48,
    0xFFF59E0B, 0xFFD97706, 0xFF0F172A, 0xFFF8FAFC, 0xFF334155, 0xFFEC4899
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeStudioScreen(
    isAr: Boolean,
    themeConfig: KeyboardThemeConfig,
    sizeConfig: KeyboardSizeConfig,
    toolbarConfig: ToolbarVisibilityConfig,
    gradualPasteConfig: GradualPasteConfig,
    enabledLanguages: List<KeyboardLanguage>,
    previewState: KeyboardUiState,
    clipboardItems: List<ClipboardEntity>,
    passwordEntries: List<PasswordEntryItem>,
    onSetThemeMode: (ThemeMode) -> Unit,
    onApplyPreset: (KeyboardThemePreset) -> Unit,
    onUpdateCustomColors: (
        backgroundArgb: Long?,
        keyArgb: Long?,
        specialKeyArgb: Long?,
        accentArgb: Long?,
        textArgb: Long?,
        cornerRadiusDp: Float?,
        showBorders: Boolean?
    ) -> Unit,
    onUpdateSizeConfig: ((KeyboardSizeConfig) -> KeyboardSizeConfig) -> Unit,
    onUpdateToolbarConfig: ((ToolbarVisibilityConfig) -> ToolbarVisibilityConfig) -> Unit,
    onSetKeyShape: (KeyButtonShape) -> Unit,
    onSetFontStyle: (KeyboardFontStyle) -> Unit,
    onUpdateGradualPasteConfig: ((GradualPasteConfig) -> GradualPasteConfig) -> Unit,
    onToggleLanguage: (KeyboardLanguage) -> Unit,
    onPreviewSwitchLanguage: () -> Unit,
    onPreviewShift: () -> Unit,
    onPreviewSymbols: () -> Unit,
    onPreviewPanel: (KeyboardPanel) -> Unit,
    modifier: Modifier = Modifier
) {
    val resolved = themeConfig.resolvePalette(isSystemInDarkTheme())
    var selectedColorTarget by remember { mutableIntStateOf(0) } // 0=bg, 1=key, 2=special, 3=accent, 4=text

    val activeColorArgb = when (selectedColorTarget) {
        0 -> resolved.background.toArgb().toLong() and 0xFFFFFFFFL
        1 -> resolved.key.toArgb().toLong() and 0xFFFFFFFFL
        2 -> resolved.specialKey.toArgb().toLong() and 0xFFFFFFFFL
        3 -> resolved.accent.toArgb().toLong() and 0xFFFFFFFFL
        else -> resolved.text.toArgb().toLong() and 0xFFFFFFFFL
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Pinned Live Interactive Preview
        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pick(isAr, "معاينة حية للوحة المفاتيح", "Live Keyboard Preview"),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "${(sizeConfig.heightScale * 100).roundToInt()}% • ${previewState.activeLanguage.shortBadge}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    BayanKeyboardView(
                        state = previewState,
                        themeConfig = themeConfig,
                        sizeConfig = sizeConfig,
                        toolbarConfig = toolbarConfig,
                        gradualPasteConfig = gradualPasteConfig,
                        clipboardItems = clipboardItems,
                        passwordEntries = passwordEntries,
                        isPreviewMode = true,
                        onKeyText = {},
                        onBackspace = {},
                        onShiftToggle = onPreviewShift,
                        onSymbolsToggle = onPreviewSymbols,
                        onSwitchLanguage = { onPreviewSwitchLanguage() },
                        onEnter = {},
                        onSuggestionClick = {},
                        onProofreadClick = {},
                        onUpdateToolbarConfig = onUpdateToolbarConfig,
                        onUpdateSizeConfig = onUpdateSizeConfig,
                        onSetKeyShape = onSetKeyShape,
                        onUpdateGradualPasteConfig = onUpdateGradualPasteConfig,
                        onTranslateClick = {},
                        onSelectTranslateTarget = {},
                        onUndoAiClick = {},
                        onPanelChange = onPreviewPanel,
                        onClipboardPaste = {},
                        onClipboardPinToggle = {},
                        onClipboardDelete = {},
                        onOpenSettings = {}
                    )
                }
            }
        }

        // Scrollable Customization Controls
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Theme Mode & Presets
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = pick(isAr, "الوضع والثيمات الجاهزة", "Theme Mode & Presets"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ThemeMode.entries.forEach { mode ->
                                    FilterChip(
                                        selected = themeConfig.themeMode == mode,
                                        onClick = { onSetThemeMode(mode) },
                                        label = { Text(if (isAr) mode.labelAr else mode.labelEn) }
                                    )
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(KeyboardThemePresets.all) { preset ->
                                    val selected = themeConfig.presetId == preset.id && themeConfig.themeMode != ThemeMode.CUSTOM
                                    Surface(
                                        onClick = { onApplyPreset(preset) },
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(preset.backgroundArgb),
                                        border = BorderStroke(
                                            width = if (selected) 2.5.dp else 1.dp,
                                            color = if (selected) Color(preset.accentArgb) else MaterialTheme.colorScheme.outline
                                        ),
                                        modifier = Modifier.width(126.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(preset.keyArgb))
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(preset.accentArgb))
                                                )
                                            }
                                            Text(
                                                text = if (isAr) preset.nameAr else preset.nameEn,
                                                color = Color(preset.textArgb),
                                                style = MaterialTheme.typography.labelLarge,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Full Custom Color Editor
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Text(
                                    text = pick(isAr, "تخصيص ألوان الواجهة بالكامل", "Full Custom Color Studio"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            val colorTargets = listOf(
                                0 to pick(isAr, "الخلفية", "Background"),
                                1 to pick(isAr, "المفاتيح", "Keys"),
                                2 to pick(isAr, "الوظائف", "Special"),
                                3 to pick(isAr, "التمييز", "Accent"),
                                4 to pick(isAr, "النص", "Text")
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                colorTargets.forEach { (idx, label) ->
                                    val targetColor = when (idx) {
                                        0 -> resolved.background
                                        1 -> resolved.key
                                        2 -> resolved.specialKey
                                        3 -> resolved.accent
                                        else -> resolved.text
                                    }
                                    FilterChip(
                                        selected = selectedColorTarget == idx,
                                        onClick = { selectedColorTarget = idx },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(targetColor)
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                            )
                                        },
                                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                                    )
                                }
                            }

                            // Swatch Grid
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                customColorSwatches.forEach { argb ->
                                    val isChosen = (activeColorArgb and 0xFFFFFFL) == (argb and 0xFFFFFFL)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(argb))
                                            .border(
                                                width = if (isChosen) 2.5.dp else 1.dp,
                                                color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                applySingleColor(selectedColorTarget, argb, onUpdateCustomColors)
                                            }
                                    )
                                }
                            }

                            // Fine-grained RGB Sliders for full color freedom
                            val r = ((activeColorArgb shr 16) and 0xFF).toFloat()
                            val g = ((activeColorArgb shr 8) and 0xFF).toFloat()
                            val b = (activeColorArgb and 0xFF).toFloat()

                            ColorChannelSlider("R", r, Color(0xFFEF4444)) { newR ->
                                val next = (0xFFL shl 24) or (newR.roundToInt().toLong() shl 16) or (g.roundToInt().toLong() shl 8) or b.roundToInt().toLong()
                                applySingleColor(selectedColorTarget, next, onUpdateCustomColors)
                            }
                            ColorChannelSlider("G", g, Color(0xFF10B981)) { newG ->
                                val next = (0xFFL shl 24) or (r.roundToInt().toLong() shl 16) or (newG.roundToInt().toLong() shl 8) or b.roundToInt().toLong()
                                applySingleColor(selectedColorTarget, next, onUpdateCustomColors)
                            }
                            ColorChannelSlider("B", b, Color(0xFF3B82F6)) { newB ->
                                val next = (0xFFL shl 24) or (r.roundToInt().toLong() shl 16) or (g.roundToInt().toLong() shl 8) or newB.roundToInt().toLong()
                                applySingleColor(selectedColorTarget, next, onUpdateCustomColors)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(pick(isAr, "إظهار حدود المفاتيح", "Show Key Borders"), style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = themeConfig.showKeyBorders,
                                    onCheckedChange = {
                                        onUpdateCustomColors(null, null, null, null, null, null, it)
                                    }
                                )
                            }

                            Text(
                                text = "${pick(isAr, "استدارة حواف المفاتيح", "Key Corner Radius")}: ${themeConfig.keyCornerRadiusDp.roundToInt()}dp",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Slider(
                                value = themeConfig.keyCornerRadiusDp,
                                onValueChange = {
                                    onUpdateCustomColors(null, null, null, null, null, it, null)
                                },
                                valueRange = 2f..18f
                            )

                            KeyButtonShapeGrid(
                                selectedShape = themeConfig.keyShape,
                                isAr = isAr,
                                accentColor = MaterialTheme.colorScheme.primary,
                                surfaceColor = MaterialTheme.colorScheme.surfaceVariant,
                                textColor = MaterialTheme.colorScheme.onSurface,
                                compact = false,
                                onSelectShape = onSetKeyShape
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(pick(isAr, "خط التطبيق ولوحة المفاتيح", "App & Keyboard Font"), style = MaterialTheme.typography.titleMedium)
                            Text(pick(isAr, "اختر الخط الذي يناسب العربية والإنجليزية. المعاينة أدناه.", "Choose a font for Arabic and English. Preview below."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                KeyboardFontStyle.entries.forEach { font ->
                                    FilterChip(
                                        selected = themeConfig.fontStyle == font,
                                        onClick = { onSetFontStyle(font) },
                                        label = { Text(if (isAr) font.labelAr else font.labelEn) }
                                    )
                                }
                            }
                            Text("العربية • Bayan Keyboard", fontFamily = fontFamilyFor(themeConfig.fontStyle), style = MaterialTheme.typography.titleLarge)
                        }
                    }

                    // 3. Keyboard Size & Ergonomics
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.AspectRatio, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Text(
                                    text = pick(isAr, "حجم اللوحة والتخصيص", "Keyboard Size & Ergonomics"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Text(
                                text = "${pick(isAr, "ارتفاع لوحة المفاتيح", "Keyboard Height")}: ${(sizeConfig.heightScale * 100).roundToInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Slider(
                                value = sizeConfig.heightScale,
                                onValueChange = { v -> onUpdateSizeConfig { it.copy(heightScale = v) } },
                                valueRange = 0.78f..1.35f
                            )

                            Text(
                                text = "${pick(isAr, "حجم خط المفاتيح", "Key Font Size")}: ${(sizeConfig.fontScale * 100).roundToInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Slider(
                                value = sizeConfig.fontScale,
                                onValueChange = { v -> onUpdateSizeConfig { it.copy(fontScale = v) } },
                                valueRange = 0.85f..1.30f
                            )

                            Text(
                                text = "${pick(isAr, "المسافة السفلية", "Bottom Padding")}: ${sizeConfig.bottomPaddingDp.roundToInt()}dp",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Slider(
                                value = sizeConfig.bottomPaddingDp,
                                onValueChange = { v -> onUpdateSizeConfig { it.copy(bottomPaddingDp = v) } },
                                valueRange = 0f..28f
                            )

                            SettingToggleRow(
                                label = pick(isAr, "إظهار صف الأرقام بالأعلى", "Show Top Number Row"),
                                checked = sizeConfig.showNumberRow,
                                onCheckedChange = { c -> onUpdateSizeConfig { it.copy(showNumberRow = c) } }
                            )
                            if (sizeConfig.showNumberRow) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = pick(isAr, "نوع أرقام صف الأرقام", "Number Row Numerals"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        NumberRowStyle.entries.forEach { style ->
                                            FilterChip(
                                                selected = sizeConfig.numberRowStyle == style,
                                                onClick = {
                                                    onUpdateSizeConfig { it.copy(numberRowStyle = style) }
                                                },
                                                label = {
                                                    Text(if (isAr) style.labelAr else style.labelEn)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            SettingToggleRow(
                                label = pick(isAr, "اهتزاز اللمس عند الضغط", "Haptic Feedback"),
                                checked = sizeConfig.hapticFeedback,
                                onCheckedChange = { c -> onUpdateSizeConfig { it.copy(hapticFeedback = c) } }
                            )
                            SettingToggleRow(
                                label = pick(isAr, "الكتابة بحروف كبيرة تلقائياً", "Auto-Capitalize"),
                                checked = sizeConfig.autoCapitalize,
                                onCheckedChange = { c -> onUpdateSizeConfig { it.copy(autoCapitalize = c) } }
                            )
                        }
                    }

                    // 3b. Toolbar Icons Order & Visibility
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = pick(isAr, "ترتيب وإظهار أيقونات الشريط", "Toolbar Icon Order & Visibility"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                text = pick(
                                    isAr,
                                    "حرّك أي أيقونة يميناً أو يساراً (بما فيها أيقونة التصحيح) أو أظهرها وأخفِها.",
                                    "Move any icon left or right (including AI Fix) and toggle visibility."
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val toolbarRows = remember(toolbarConfig.order) { toolbarConfig.order.chunked(2) }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                toolbarRows.forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        pair.forEach { action ->
                                            val idx = toolbarConfig.order.indexOf(action)
                                            val visible = toolbarConfig.isVisible(action)
                                            Surface(
                                                onClick = {
                                                    onUpdateToolbarConfig { it.withVisibility(action, !visible) }
                                                },
                                                shape = RoundedCornerShape(14.dp),
                                                color = if (visible) {
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                                },
                                                border = BorderStroke(
                                                    width = 1.dp,
                                                    color = if (visible) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                                    else MaterialTheme.colorScheme.outline
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = if (isAr) action.labelAr else action.labelEn,
                                                            style = MaterialTheme.typography.labelLarge,
                                                            maxLines = 1,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Switch(
                                                            checked = visible,
                                                            onCheckedChange = { c ->
                                                                onUpdateToolbarConfig { it.withVisibility(action, c) }
                                                            }
                                                        )
                                                    }
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        IconButton(
                                                            onClick = { onUpdateToolbarConfig { it.moveAction(action, -1) } },
                                                            enabled = idx > 0,
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Move Left")
                                                        }
                                                        Text(
                                                            text = "#${idx + 1}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        IconButton(
                                                            onClick = { onUpdateToolbarConfig { it.moveAction(action, +1) } },
                                                            enabled = idx < toolbarConfig.order.lastIndex,
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.ChevronRight, contentDescription = "Move Right")
                                                        }
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

                    // 3c. Gradual Paste & Typing Settings
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GradualPasteSettingsSection(
                                config = gradualPasteConfig,
                                isAr = isAr,
                                accentColor = MaterialTheme.colorScheme.primary,
                                cardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                pillColor = MaterialTheme.colorScheme.surfaceVariant,
                                textColor = MaterialTheme.colorScheme.onSurface,
                                compact = false,
                                onUpdateConfig = onUpdateGradualPasteConfig
                            )
                        }
                    }

                    // 4. Multilingual Support & Smart Learning
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = pick(isAr, "اللغات والتبديل التلقائي والتعلم", "Languages, Auto-Switch & Learning"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                KeyboardLanguage.entries.forEach { lang ->
                                    val enabled = enabledLanguages.contains(lang)
                                    FilterChip(
                                        selected = enabled,
                                        onClick = { onToggleLanguage(lang) },
                                        label = {
                                            Text(if (isAr) "${lang.labelAr} (${lang.shortBadge})" else "${lang.labelEn} (${lang.shortBadge})")
                                        }
                                    )
                                }
                            }

                            SettingToggleRow(
                                label = pick(
                                    isAr,
                                    "التبديل التلقائي للغة أثناء الكتابة",
                                    "Auto-Switch Language While Typing"
                                ),
                                checked = sizeConfig.autoSwitchLanguage,
                                onCheckedChange = { c -> onUpdateSizeConfig { it.copy(autoSwitchLanguage = c) } }
                            )
                            SettingToggleRow(
                                label = pick(
                                    isAr,
                                    "التعلم المستمر من كتاباتك للاقتراحات",
                                    "Continuous Learning for Suggestions"
                                ),
                                checked = sizeConfig.learningEnabled,
                                onCheckedChange = { c -> onUpdateSizeConfig { it.copy(learningEnabled = c) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun applySingleColor(
    targetIdx: Int,
    argb: Long,
    onUpdateCustomColors: (Long?, Long?, Long?, Long?, Long?, Float?, Boolean?) -> Unit
) {
    when (targetIdx) {
        0 -> onUpdateCustomColors(argb, null, null, null, null, null, null)
        1 -> onUpdateCustomColors(null, argb, null, null, null, null, null)
        2 -> onUpdateCustomColors(null, null, argb, null, null, null, null)
        3 -> onUpdateCustomColors(null, null, null, argb, null, null, null)
        else -> onUpdateCustomColors(null, null, null, null, argb, null, null)
    }
}

@Composable
private fun ColorChannelSlider(
    channel: String,
    value: Float,
    tint: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = channel,
            color = tint,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(18.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${value.roundToInt()}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(28.dp)
        )
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
