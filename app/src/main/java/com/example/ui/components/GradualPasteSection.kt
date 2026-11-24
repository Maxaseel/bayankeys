package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GradualPasteConfig
import com.example.data.model.GradualPasteMode
import com.example.data.model.KeyButtonShape
import com.example.ui.theme.CairoFontFamily

@Composable
fun KeyButtonShapeGrid(
    selectedShape: KeyButtonShape,
    isAr: Boolean,
    accentColor: Color,
    surfaceColor: Color,
    textColor: Color,
    compact: Boolean = false,
    onSelectShape: (KeyButtonShape) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf(KeyButtonShape.ROUNDED, KeyButtonShape.SHARP_SQUARE),
        listOf(KeyButtonShape.SOFT_CIRCLE, KeyButtonShape.NO_BACKGROUND)
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
    ) {
        Text(
            text = if (isAr) "شكل أزرار المفاتيح" else "Key Button Shape",
            color = accentColor,
            fontSize = if (compact) 12.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CairoFontFamily
        )
        rows.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
            ) {
                pair.forEach { shapeOption ->
                    val selected = shapeOption == selectedShape
                    Surface(
                        onClick = { onSelectShape(shapeOption) },
                        shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
                        color = if (selected) Color(0xFF2563EB) else surfaceColor,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) Color(0xFF3B82F6) else textColor.copy(alpha = 0.14f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("key_shape_${shapeOption.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = if (compact) 8.dp else 11.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isAr) shapeOption.labelAr else shapeOption.labelEn,
                                color = if (selected) Color.White else textColor,
                                fontSize = if (compact) 11.sp else 13.sp,
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
}

@Composable
fun GradualPasteSettingsSection(
    config: GradualPasteConfig,
    isAr: Boolean,
    accentColor: Color,
    cardColor: Color,
    pillColor: Color,
    textColor: Color,
    compact: Boolean = false,
    onUpdateConfig: ((GradualPasteConfig) -> GradualPasteConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)
    ) {
        // Header + Enable Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardColor)
                .border(
                    width = 1.dp,
                    color = if (config.enabled) accentColor.copy(alpha = 0.55f) else textColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = if (compact) 6.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(if (compact) 18.dp else 22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAr) "إعدادات اللصق والإدخال التدريجي (المقياس 1 إلى 50)"
                    else "Gradual Paste & Typing Settings (1 to 50)",
                    color = accentColor,
                    fontSize = if (compact) 11.5.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isAr) {
                        if (config.enabled) "مفعّل — اضغط مطولاً على أي نص بالحافظة أو شريط الاقتراحات"
                        else "غير مفعّل — فعّله لتطبيق اللصق التدريجي عند الضغط المطوّل"
                    } else {
                        if (config.enabled) "Enabled — Long-press any clip or suggestion chip to paste gradually"
                        else "Disabled — Enable to apply gradual paste on long press"
                    },
                    color = textColor.copy(alpha = 0.65f),
                    fontSize = if (compact) 9.5.sp else 11.5.sp,
                    fontFamily = CairoFontFamily
                )
            }
            Switch(
                checked = config.enabled,
                onCheckedChange = { checked ->
                    onUpdateConfig { it.copy(enabled = checked) }
                },
                modifier = Modifier.testTag("gradual_paste_enable_switch")
            )
        }

        // Mode Pills: Word-by-Word | Char-by-Char
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
        ) {
            listOf(GradualPasteMode.WORD_BY_WORD, GradualPasteMode.CHAR_BY_CHAR).forEach { modeOption ->
                val selected = config.mode == modeOption
                Surface(
                    onClick = { onUpdateConfig { it.copy(mode = modeOption) } },
                    shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
                    color = if (selected) Color(0xFF2563EB) else pillColor,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selected) Color(0xFF3B82F6) else textColor.copy(alpha = 0.14f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gradual_mode_${modeOption.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = if (compact) 8.dp else 11.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAr) modeOption.labelAr else modeOption.labelEn,
                            color = if (selected) Color.White else textColor,
                            fontSize = if (compact) 11.sp else 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = CairoFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 9 Scale Parameters (1..50) matching screenshot
        GradualScaleRow(
            title = if (isAr) "سرعة الحروف" else "Character Speed",
            subtitle = if (isAr) "كلما زاد الرقم زادت السرعة (${config.charDelayMs()} ms)"
            else "Higher number = faster (${config.charDelayMs()} ms)",
            value = config.charSpeed,
            cardColor = cardColor,
            textColor = textColor,
            compact = compact,
            onDecrement = { onUpdateConfig { it.copy(charSpeed = (it.charSpeed - 1).coerceAtLeast(1)) } },
            onIncrement = { onUpdateConfig { it.copy(charSpeed = (it.charSpeed + 1).coerceAtMost(50)) } }
        )

        GradualScaleRow(
            title = if (isAr) "سرعة الكلمات" else "Word Speed",
            subtitle = if (isAr) "سرعة إدخال الكلمات الكاملة (${config.wordDelayMs()} ms)"
            else "Full word insertion speed (${config.wordDelayMs()} ms)",
            value = config.wordSpeed,
            cardColor = cardColor,
            textColor = textColor,
            compact = compact,
            onDecrement = { onUpdateConfig { it.copy(wordSpeed = (it.wordSpeed - 1).coerceAtLeast(1)) } },
            onIncrement = { onUpdateConfig { it.copy(wordSpeed = (it.wordSpeed + 1).coerceAtMost(50)) } }
        )

        GradualScaleRow(
            title = if (isAr) "إيقاع المسافات والأسطر" else "Space & Newline Rhythm",
            subtitle = if (isAr) "تأخير الفواصل (${config.spaceDelayMs()} ms)"
            else "Separator delay (${config.spaceDelayMs()} ms)",
            value = config.spaceNewlineRhythm,
            cardColor = cardColor,
            textColor = textColor,
            compact = compact,
            onDecrement = { onUpdateConfig { it.copy(spaceNewlineRhythm = (it.spaceNewlineRhythm - 1).coerceAtLeast(1)) } },
            onIncrement = { onUpdateConfig { it.copy(spaceNewlineRhythm = (it.spaceNewlineRhythm + 1).coerceAtMost(50)) } }
        )

        GradualScaleRow(
            title = if (isAr) "سرعة التنقل بالأسهم" else "Arrow Navigation Speed",
            subtitle = if (isAr) "فاصل حركة المؤشر (${config.arrowDelayMs()} ms)"
            else "Cursor step interval (${config.arrowDelayMs()} ms)",
            value = config.arrowNavSpeed,
            cardColor = cardColor,
            textColor = textColor,
            compact = compact,
            onDecrement = { onUpdateConfig { it.copy(arrowNavSpeed = (it.arrowNavSpeed - 1).coerceAtLeast(1)) } },
            onIncrement = { onUpdateConfig { it.copy(arrowNavSpeed = (it.arrowNavSpeed + 1).coerceAtMost(50)) } }
        )

        GradualScaleRow(
            title = if (isAr) "النبض الإيقاعي الطبيعي" else "Natural Rhythm Pulse",
            subtitle = if (isAr) "تفاوت طبيعي في الإيقاع" else "Natural pacing variation",
            value = config.naturalRhythmPulse,
            cardColor = cardColor,
            textColor = textColor,
            compact = compact,
            onDecrement = { onUpdateConfig { it.copy(naturalRhythmPulse = (it.naturalRhythmPulse - 1).coerceAtLeast(1)) } },
            onIncrement = { onUpdateConfig { it.copy(naturalRhythmPulse = (it.naturalRhythmPulse + 1).coerceAtMost(50)) } }
        )

        GradualScaleRow(
            title = if (isAr) "شدة الأنفاس" else "Breath Intensity",
            subtitle = if (isAr) "قوة وقفات التنفس أثناء الكتابة" else "Strength of typing breath pauses",
            value = config.breathIntensity,
            cardColor = cardColor,
            textColor = textColor,
            compact = compact,
            onDecrement = { onUpdateConfig { it.copy(breathIntensity = (it.breathIntensity - 1).coerceAtLeast(1)) } },
            onIncrement = { onUpdateConfig { it.copy(breathIntensity = (it.breathIntensity + 1).coerceAtMost(50)) } }
        )

        GradualScaleRow(
            title = if (isAr) "عدد الحروف قبل النفس" else "Chars Before Breath",
            subtitle = if (isAr) "توقف مؤقت كل (${config.charsBeforeBreath}) حرف/وحدة"
            else "Pause every (${config.charsBeforeBreath}) chars/units",
            value = config.charsBeforeBreath,
            cardColor = cardColor,
            textColor = textColor,
            compact = compact,
            onDecrement = { onUpdateConfig { it.copy(charsBeforeBreath = (it.charsBeforeBreath - 1).coerceAtLeast(1)) } },
            onIncrement = { onUpdateConfig { it.copy(charsBeforeBreath = (it.charsBeforeBreath + 1).coerceAtMost(50)) } }
        )

        GradualScaleRow(
            title = if (isAr) "مدة وقفة النفس" else "Breath Pause Duration",
            subtitle = if (isAr) "زمن التوقف للتنفس (${config.breathPauseMs()} ms)"
            else "Breath pause time (${config.breathPauseMs()} ms)",
            value = config.breathPauseDuration,
            cardColor = cardColor,
            textColor = textColor,
            compact = compact,
            onDecrement = { onUpdateConfig { it.copy(breathPauseDuration = (it.breathPauseDuration - 1).coerceAtLeast(1)) } },
            onIncrement = { onUpdateConfig { it.copy(breathPauseDuration = (it.breathPauseDuration + 1).coerceAtMost(50)) } }
        )

        GradualScaleRow(
            title = if (isAr) "التأخير قبل البدء" else "Start Delay",
            subtitle = if (isAr) "مهلة البدء (${config.startDelayMs()} ms)"
            else "Initial delay (${config.startDelayMs()} ms)",
            value = config.startDelay,
            cardColor = cardColor,
            textColor = textColor,
            compact = compact,
            onDecrement = { onUpdateConfig { it.copy(startDelay = (it.startDelay - 1).coerceAtLeast(1)) } },
            onIncrement = { onUpdateConfig { it.copy(startDelay = (it.startDelay + 1).coerceAtMost(50)) } }
        )
    }
}

@Composable
private fun GradualScaleRow(
    title: String,
    subtitle: String,
    value: Int,
    cardColor: Color,
    textColor: Color,
    compact: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (compact) 12.dp else 14.dp))
            .background(cardColor)
            .border(
                width = 0.7.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(if (compact) 12.dp else 14.dp)
            )
            .padding(horizontal = 10.dp, vertical = if (compact) 7.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // + green button (left in LTR or symmetric controls)
        Box(
            modifier = Modifier
                .size(if (compact) 30.dp else 36.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981))
                .clickable(enabled = value < 50, onClick = onIncrement),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Increase $title",
                tint = Color.White,
                modifier = Modifier.size(if (compact) 16.dp else 20.dp)
            )
        }

        // Value/50 readout
        Text(
            text = "$value/50",
            color = Color(0xFF38BDF8),
            fontSize = if (compact) 12.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(if (compact) 40.dp else 46.dp)
        )

        // - dark pill button
        Box(
            modifier = Modifier
                .size(if (compact) 30.dp else 36.dp)
                .clip(CircleShape)
                .background(textColor.copy(alpha = 0.14f))
                .clickable(enabled = value > 1, onClick = onDecrement),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Decrease $title",
                tint = textColor,
                modifier = Modifier.size(if (compact) 16.dp else 20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = title,
                color = textColor,
                fontSize = if (compact) 11.5.sp else 13.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = textColor.copy(alpha = 0.65f),
                fontSize = if (compact) 9.5.sp else 11.sp,
                fontFamily = CairoFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
