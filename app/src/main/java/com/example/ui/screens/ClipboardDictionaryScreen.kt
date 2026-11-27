package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.local.ClipboardEntity
import com.example.data.local.LearnedWordEntity
import com.example.data.model.KeyboardLanguage
import com.example.ui.AppStrings.pick

@Composable
fun ClipboardDictionaryScreen(
    isAr: Boolean,
    clipboardItems: List<ClipboardEntity>,
    learnedWords: List<LearnedWordEntity>,
    onAddManualClip: (String, Boolean) -> Unit,
    onTogglePinClip: (ClipboardEntity) -> Unit,
    onDeleteClip: (Long) -> Unit,
    onClearUnpinned: () -> Unit,
    onAddCustomWord: (String, KeyboardLanguage) -> Unit,
    onDeleteLearnedWord: (String, String) -> Unit,
    onResetDictionary: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val clipboard = LocalClipboardManager.current
    var dialog by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var newClipText by remember { mutableStateOf("") }
    var newWordText by remember { mutableStateOf("") }
    var newWordLang by remember { mutableStateOf(KeyboardLanguage.ARABIC) }

    if (dialog == "search") {
        AlertDialog(
            onDismissRequest = { dialog = "" },
            title = { Text(pick(isAr, "فتح البحث", "Search")) },
            text = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = { Text(pick(isAr, "بحث في الحافظة...", "Search clipboard...")) },
                    singleLine = true
                )
            },
            confirmButton = { TextButton(onClick = { dialog = "" }) { Text(pick(isAr, "تم", "Done")) } },
            dismissButton = { TextButton(onClick = { searchQuery = ""; dialog = "" }) { Text(pick(isAr, "مسح", "Clear")) } }
        )
    }

    if (dialog == "addClip") {
        AlertDialog(
            onDismissRequest = { dialog = "" },
            title = { Text(pick(isAr, "إضافة نص ثابت", "Add a saved snippet")) },
            text = {
                OutlinedTextField(
                    value = newClipText,
                    onValueChange = { newClipText = it },
                    placeholder = { Text(pick(isAr, "أدخل النص...", "Enter text...")) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newClipText.isNotBlank()) onAddManualClip(newClipText, true)
                    newClipText = ""
                    dialog = ""
                }) { Text(pick(isAr, "تثبيت", "Pin")) }
            },
            dismissButton = { TextButton(onClick = { dialog = "" }) { Text(pick(isAr, "إلغاء", "Cancel")) } }
        )
    }

    if (dialog == "dictionary") {
        AlertDialog(
            onDismissRequest = { dialog = "" },
            title = { Text(pick(isAr, "القاموس", "Dictionary")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newWordText,
                        onValueChange = { newWordText = it },
                        placeholder = { Text(pick(isAr, "أضف كلمة أو اسماً...", "Add a word or name...")) },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        KeyboardLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = newWordLang == lang,
                                onClick = { newWordLang = lang },
                                label = { Text(if (isAr) lang.labelAr else lang.labelEn) }
                            )
                        }
                    }
                    Button(onClick = {
                        if (newWordText.isNotBlank()) onAddCustomWord(newWordText, newWordLang)
                        newWordText = ""
                    }) { Icon(Icons.Rounded.Add, contentDescription = null); Text(pick(isAr, "إضافة كلمة", "Add word")) }
                    TextButton(onClick = { dialog = "reset" }) { Text(pick(isAr, "إعادة ضبط القاموس", "Reset dictionary")) }
                    learnedWords.take(20).forEach { word ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${word.word}  ·  ${word.languageCode}", modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDeleteLearnedWord(word.word, word.languageCode) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = pick(isAr, "حذف", "Delete"))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { dialog = "" }) { Text(pick(isAr, "إغلاق", "Close")) } }
        )
    }

    if (dialog == "reset") {
        AlertDialog(
            onDismissRequest = { dialog = "dictionary" },
            title = { Text(pick(isAr, "إعادة ضبط القاموس؟", "Reset dictionary?")) },
            text = { Text(pick(isAr, "سيتم مسح الكلمات المتعلمة.", "Learned words will be cleared.")) },
            confirmButton = {
                TextButton(onClick = { onResetDictionary(); dialog = "dictionary" }) {
                    Text(pick(isAr, "إعادة ضبط", "Reset"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { dialog = "dictionary" }) { Text(pick(isAr, "إلغاء", "Cancel")) } }
        )
    }

    val clips = clipboardItems.filter { searchQuery.isBlank() || it.text.contains(searchQuery, ignoreCase = true) }
    val current = clips.filterNot { it.isPinned }
    val pinned = clips.filter { it.isPinned }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isAr) {
                        ClipboardHeaderActions(
                            isAr = isAr,
                            showClear = clipboardItems.any { !it.isPinned },
                            onDictionary = { dialog = "dictionary" },
                            onSearch = { dialog = "search" },
                            onClear = onClearUnpinned
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pick(isAr, "الحافظة", "Clipboard"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            IconButton(onClick = onBack) {
                                Icon(Icons.Rounded.ChevronRight, contentDescription = pick(isAr, "رجوع", "Back"))
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Rounded.ArrowBack, contentDescription = pick(isAr, "رجوع", "Back"))
                            }
                            Text(pick(isAr, "الحافظة", "Clipboard"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        ClipboardHeaderActions(
                            isAr = isAr,
                            showClear = clipboardItems.any { !it.isPinned },
                            onDictionary = { dialog = "dictionary" },
                            onSearch = { dialog = "search" },
                            onClear = onClearUnpinned
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(pick(isAr, "الحالي (${current.size})", "Current (${current.size})"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { current.forEach(onTogglePinClip) }, enabled = current.isNotEmpty()) {
                    Icon(Icons.Rounded.PushPin, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(pick(isAr, "تثبيت الكل", "Pin all"))
                }
            }
        }
        item {
            if (current.isEmpty()) EmptyStateCard(pick(isAr, "لا توجد عناصر حالية.", "No current items."))
            else LazyRow(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(current, key = { "current_${it.id}" }) { clip ->
                    ClipCard(
                        clip = clip,
                        isAr = isAr,
                        pinned = false,
                        onPin = { onTogglePinClip(clip) },
                        onCopy = { clipboard.setText(AnnotatedString(clip.text)) },
                        onDelete = { onDeleteClip(clip.id) },
                        modifier = Modifier.widthIn(min = 250.dp, max = 320.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(pick(isAr, "المثبتة (${pinned.size})", "Pinned (${pinned.size})"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { dialog = "addClip" }) {
                    Icon(Icons.Rounded.Add, contentDescription = pick(isAr, "إضافة نص ثابت", "Add snippet"))
                }
            }
        }
        if (pinned.isEmpty()) {
            item { EmptyStateCard(pick(isAr, "لا توجد نصوص مثبتة.", "No pinned items.")) }
        } else {
            items(pinned.chunked(2).size, key = { "pinned_row_$it" }) { index ->
                val pair = pinned.chunked(2)[index]
                Row(
                    modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { clip ->
                        ClipCard(
                            clip = clip,
                            isAr = isAr,
                            pinned = true,
                            onPin = { onTogglePinClip(clip) },
                            onCopy = { clipboard.setText(AnnotatedString(clip.text)) },
                            onDelete = { onDeleteClip(clip.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ClipboardHeaderActions(
    isAr: Boolean,
    showClear: Boolean,
    onDictionary: () -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onDictionary) {
            Icon(Icons.Rounded.Book, contentDescription = pick(isAr, "القاموس", "Dictionary"))
        }
        IconButton(onClick = onSearch) {
            Icon(Icons.Rounded.Search, contentDescription = pick(isAr, "فتح البحث", "Search"))
        }
        if (showClear) {
            IconButton(onClick = onClear) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = pick(isAr, "مسح السجل", "Clear history"), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ClipCard(
    clip: ClipboardEntity,
    isAr: Boolean,
    pinned: Boolean,
    onPin: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (pinned) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(if (pinned) 1.5.dp else 1.dp, if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (pinned) Text(pick(isAr, "مثبت", "Pinned"), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(clip.text, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onPin, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Rounded.PushPin, contentDescription = pick(isAr, if (pinned) "إلغاء التثبيت" else "تثبيت", if (pinned) "Unpin" else "Pin"), tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = pick(isAr, "نسخ", "Copy"), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = pick(isAr, "حذف", "Delete"), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(text: String) {
    Box(
        modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
