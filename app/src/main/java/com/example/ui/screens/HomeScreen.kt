package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.example.R
import com.example.data.model.AiProviderConfig
import com.example.data.model.CustomBotItem
import com.example.data.model.ProofreadDiffReview
import com.example.data.model.TargetTranslateLanguage
import com.example.ui.AppStrings.pick
import com.example.ui.ImeStatus
import com.example.util.WordDiffUtil

@Composable
fun HomeScreen(
    isAr: Boolean,
    imeStatus: ImeStatus,
    micPermissionGranted: Boolean,
    aiConfig: AiProviderConfig,
    customBots: List<CustomBotItem> = emptyList(),
    learnedWordsCount: Int,
    pinnedClipsCount: Int,
    playgroundText: String,
    playgroundDiffReview: ProofreadDiffReview?,
    canUndoPlayground: Boolean,
    playgroundBusy: Boolean,
    playgroundStatus: Pair<String, Boolean>?,
    onPlaygroundTextChange: (String) -> Unit,
    onProofreadPlayground: () -> Unit,
    onApplyPlaygroundDiff: () -> Unit,
    onApplyPlaygroundDiffGradually: () -> Unit = {},
    onCopyPlaygroundDiff: (String) -> Unit,
    onDismissPlaygroundDiff: () -> Unit,
    onTranslatePlayground: (TargetTranslateLanguage) -> Unit,
    onUndoPlayground: () -> Unit,
    onRunCustomBotPlayground: (CustomBotItem) -> Unit = {},
    onOpenBotsHub: () -> Unit = {},
    onRequestMicPermission: () -> Unit,
    onOpenAiSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val textToolbar = LocalTextToolbar.current
    val haptic = LocalHapticFeedback.current
    var showTranslatePicker by remember { mutableStateOf(false) }

    if (showTranslatePicker) {
        TargetLanguagePickerDialog(
            isAr = isAr,
            selected = aiConfig.defaultTargetLanguage,
            onSelect = { lang ->
                showTranslatePicker = false
                onTranslatePlayground(lang)
            },
            onDismiss = { showTranslatePicker = false }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Card with generated illustration
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_keyboard_hero_1795539755533),
                            contentDescription = pick(isAr, "لوحة بيان الذكية", "Bayan AI Keyboard"),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0B1120).copy(alpha = 0.25f),
                                            Color(0xFF0B1120).copy(alpha = 0.92f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = if (imeStatus.isEnabled && imeStatus.isDefault)
                                    Color(0xFF10B981).copy(alpha = 0.22f)
                                else Color(0xFFF59E0B).copy(alpha = 0.22f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (imeStatus.isEnabled && imeStatus.isDefault)
                                            Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        tint = if (imeStatus.isEnabled && imeStatus.isDefault)
                                            Color(0xFF34D399) else Color(0xFFFBBF24),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = when {
                                            imeStatus.isEnabled && imeStatus.isDefault ->
                                                pick(isAr, "لوحة بيان مفعّلة ونشطة الآن", "Bayan Keyboard Active")
                                            imeStatus.isEnabled ->
                                                pick(isAr, "مفعّلة — اخترها كلوحة افتراضية", "Enabled — Select as Default")
                                            else ->
                                                pick(isAr, "جاهزة للتفعيل في النظام", "Ready to Enable")
                                        },
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = pick(isAr, "لوحة بيان الذكية", "Bayan AI Keyboard"),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = pick(
                                    isAr,
                                    "تصحيح إملائي ونحوي فوري، ترجمة متعددة المزودين، وتعلم مستمر بالعربية والإنجليزية.",
                                    "Instant AI proofreading, multi-API translation, and adaptive bilingual learning."
                                ),
                                color = Color(0xFFCBD5E1),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // 2-Step Activation Card
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
                        Text(
                            text = pick(isAr, "تفعيل لوحة المفاتيح على جهازك", "Activate Keyboard on Your Device"),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    textToolbar.hide()
                                    focusManager.clearFocus(force = true)
                                    context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("enable_ime_button"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp)
                            ) {
                                Icon(Icons.Rounded.SettingsSuggest, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (imeStatus.isEnabled)
                                        pick(isAr, "1. مفعّلة ✓", "1. Enabled ✓")
                                    else pick(isAr, "1. تفعيل اللوحة", "1. Enable IME"),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    textToolbar.hide()
                                    focusManager.clearFocus(force = true)
                                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                    imm?.showInputMethodPicker()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pick_ime_button"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp)
                            ) {
                                Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (imeStatus.isDefault)
                                        pick(isAr, "2. مختارة ✓", "2. Selected ✓")
                                    else pick(isAr, "2. اختيار اللوحة", "2. Switch IME"),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        Surface(
                            onClick = { if (!micPermissionGranted) onRequestMicPermission() },
                            shape = RoundedCornerShape(12.dp),
                            color = if (micPermissionGranted) Color(0xFF10B981).copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mic_permission_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Microphone Permission",
                                    tint = if (micPermissionGranted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (micPermissionGranted)
                                        pick(isAr, "إذن الميكروفون للكتابة بالصوت مفعّل ✓", "Voice Typing Microphone Enabled ✓")
                                    else
                                        pick(isAr, "تفعيل إذن الميكروفون للكتابة بالصوت", "Enable Microphone for Voice Typing"),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Quick Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricPillCard(
                        icon = Icons.Rounded.SmartToy,
                        title = pick(isAr, "مزود الذكاء", "AI Provider"),
                        value = aiConfig.activeDisplayName,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = onOpenAiSettings,
                        modifier = Modifier.weight(1f)
                    )
                    MetricPillCard(
                        icon = Icons.Rounded.Book,
                        title = pick(isAr, "كلمات متعلمة", "Learned Words"),
                        value = "$learnedWordsCount",
                        accent = MaterialTheme.colorScheme.tertiary,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    MetricPillCard(
                        icon = Icons.Rounded.PushPin,
                        title = pick(isAr, "مثبتة بالحافظة", "Pinned Clips"),
                        value = "$pinnedClipsCount",
                        accent = MaterialTheme.colorScheme.secondary,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }

                // Bot Assistant Card (Image 2) + User's Custom Bots Strip
                BotAssistantPromoCard(
                    isAr = isAr,
                    botCount = customBots.size,
                    onClick = onOpenBotsHub
                )

                if (customBots.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(customBots, key = { it.id }) { bot ->
                            val botColor = Color(bot.accentArgb)
                            Surface(
                                onClick = { onRunCustomBotPlayground(bot) },
                                enabled = !playgroundBusy,
                                shape = RoundedCornerShape(14.dp),
                                color = botColor.copy(alpha = 0.14f),
                                modifier = Modifier.border(1.dp, botColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(botColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = botIconVector(bot.iconOption),
                                            contentDescription = bot.name,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = bot.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Test & AI Proofread / Translate Playground
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
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Keyboard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pick(isAr, "منطقة تجربة الكتابة والذكاء", "Typing & AI Playground"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = pick(
                                        isAr,
                                        "جرّب لوحة المفاتيح أو اختبر التصحيح والترجمة مباشرة هنا",
                                        "Tap below to type with Bayan or test AI proofreading & translation"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedTextField(
                            value = playgroundText,
                            onValueChange = onPlaygroundTextChange,
                            label = { Text(pick(isAr, "اكتب أو الصق نصاً للتجربة...", "Type or paste text to test...")) },
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("playground_text_field")
                        )

                        if (playgroundStatus != null) {
                            val (msg, isErr) = playgroundStatus
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isErr) MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = if (isErr) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        if (aiConfig.aiApiKey.isBlank()) {
                            Surface(
                                onClick = onOpenAiSettings,
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = pick(
                                        isAr,
                                        "أضف مفتاح الـ API الخاص بك من تبويب (الذكاء والترجمة) لتفعيل التصحيح والترجمة الفورية.",
                                        "Add your API key in the AI & API tab to activate instant proofreading & translation."
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    textToolbar.hide()
                                    focusManager.clearFocus(force = true)
                                    onProofreadPlayground()
                                },
                                enabled = !playgroundBusy && playgroundText.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("playground_proofread_btn")
                            ) {
                                if (playgroundBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(pick(isAr, "تصحيح النص", "Proofread"), style = MaterialTheme.typography.labelLarge)
                            }

                            FilledTonalButton(
                                onClick = {
                                    textToolbar.hide()
                                    focusManager.clearFocus(force = true)
                                    showTranslatePicker = true
                                },
                                enabled = !playgroundBusy && playgroundText.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("playground_translate_btn")
                            ) {
                                Icon(Icons.Rounded.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    pick(isAr, "ترجمة 🌐", "Translate 🌐"),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            if (canUndoPlayground) {
                                OutlinedButton(
                                    onClick = onUndoPlayground,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Undo", modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        if (playgroundDiffReview != null) {
                            val unchangedColor = MaterialTheme.colorScheme.onSurface
                            val diffText = remember(
                                playgroundDiffReview.originalText,
                                playgroundDiffReview.correctedText,
                                unchangedColor
                            ) {
                                WordDiffUtil.buildDiffAnnotatedString(
                                    original = playgroundDiffReview.originalText,
                                    corrected = playgroundDiffReview.correctedText,
                                    unchangedColor = unchangedColor
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .testTag("playground_diff_review_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = pick(isAr, "مقارنة التصحيح", "Proofread Diff"),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEF4444).copy(alpha = 0.16f)
                                        ) {
                                            Text(
                                                text = pick(isAr, "خطأ", "Wrong"),
                                                color = Color(0xFFEF4444),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.16f)
                                        ) {
                                            Text(
                                                text = pick(isAr, "مصحّح", "Fixed"),
                                                color = Color(0xFF10B981),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = onDismissPlaygroundDiff,
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Dismiss Diff",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    ) {
                                        if (playgroundDiffReview.originalText.trim() == playgroundDiffReview.correctedText.trim()) {
                                            Text(
                                                text = pick(
                                                    isAr,
                                                    "✓ النص سليم ولا توجد أخطاء:\n${playgroundDiffReview.correctedText}",
                                                    "✓ No issues found:\n${playgroundDiffReview.correctedText}"
                                                ),
                                                color = Color(0xFF10B981),
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        } else {
                                            Text(
                                                text = diffText,
                                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                clipboard?.setPrimaryClip(
                                                    ClipData.newPlainText("Bayan Proofread", playgroundDiffReview.correctedText)
                                                )
                                                onCopyPlaygroundDiff(playgroundDiffReview.correctedText)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("diff_copy_button")
                                        ) {
                                            Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(pick(isAr, "نسخ", "Copy"), style = MaterialTheme.typography.labelLarge)
                                        }
                                        @OptIn(ExperimentalFoundationApi::class)
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .combinedClickable(
                                                    onClick = onApplyPlaygroundDiff,
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        onApplyPlaygroundDiffGradually()
                                                    }
                                                )
                                                .testTag("diff_apply_button")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(pick(isAr, "تطبيق", "Apply"), style = MaterialTheme.typography.labelLarge)
                                                Spacer(Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ) {
                                                    Text(
                                                        text = pick(isAr, "⚡ مطوّل", "⚡ Hold"),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPillCard(
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TargetLanguagePickerDialog(
    isAr: Boolean,
    selected: TargetTranslateLanguage,
    onSelect: (TargetTranslateLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = pick(isAr, "اختر اللغة الهدف للترجمة", "Select Target Language"),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(340.dp)
            ) {
                items(TargetTranslateLanguage.entries) { lang ->
                    val isSelected = lang == selected
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelect(lang) }
                            .testTag("target_lang_${lang.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = lang.flagEmoji, fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lang.labelAr,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                                Text(
                                    text = lang.labelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(pick(isAr, "إغلاق", "Close"))
            }
        }
    )
}
