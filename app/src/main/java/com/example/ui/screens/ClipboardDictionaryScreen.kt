package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RestartAlt
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ClipboardEntity
import com.example.data.local.LearnedWordEntity
import com.example.data.model.KeyboardLanguage
import com.example.ui.AppStrings.pick

@OptIn(ExperimentalLayoutApi::class)
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
    modifier: Modifier = Modifier
) {
    val localClipboard = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val textToolbar = LocalTextToolbar.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Clipboard, 1 = Dictionary
    var searchQuery by remember { mutableStateOf("") }
    var newClipText by remember { mutableStateOf("") }
    var newWordText by remember { mutableStateOf("") }
    var newWordLang by remember { mutableStateOf(KeyboardLanguage.ARABIC) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(pick(isAr, "إعادة ضبط القاموس؟", "Reset Learned Dictionary?")) },
            text = {
                Text(
                    pick(
                        isAr,
                        "سيتم مسح الكلمات التي تعلمتها وإعادة القاموس الأساسي الافتراضي.",
                        "This clears all custom learned words and restores the starter bilingual dictionary."
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onResetDictionary()
                    showResetDialog = false
                }) {
                    Text(pick(isAr, "إعادة ضبط", "Reset"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(pick(isAr, "إلغاء", "Cancel"))
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = {
                            textToolbar.hide()
                            focusManager.clearFocus(force = true)
                            selectedTab = 0
                            searchQuery = ""
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = { Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    ) {
                        Text(pick(isAr, "الحافظة والمثبتة (${clipboardItems.size})", "Clipboard (${clipboardItems.size})"))
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = {
                            textToolbar.hide()
                            focusManager.clearFocus(force = true)
                            selectedTab = 1
                            searchQuery = ""
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = { Icon(Icons.Rounded.Book, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    ) {
                        Text(pick(isAr, "القاموس (${learnedWords.size})", "Dictionary (${learnedWords.size})"))
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = {
                        Text(
                            if (selectedTab == 0) pick(isAr, "بحث في الحافظة...", "Search clipboard...")
                            else pick(isAr, "بحث في الكلمات المتعلمة...", "Search learned words...")
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (selectedTab == 0) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = pick(isAr, "إضافة نص ثابت للوصول السريع", "Add Pinned Quick-Snippet"),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newClipText,
                                onValueChange = { newClipText = it },
                                placeholder = { Text(pick(isAr, "مثال: البريد الإلكتروني أو العنوان...", "e.g., Email, greeting, address...")) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newClipText.isNotBlank()) {
                                        onAddManualClip(newClipText, true)
                                        newClipText = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(pick(isAr, "تثبيت", "Pin"))
                            }
                        }
                        if (clipboardItems.any { !it.isPinned }) {
                            TextButton(onClick = onClearUnpinned) {
                                Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(pick(isAr, "مسح السجل غير المثبت", "Clear Unpinned History"))
                            }
                        }
                    }
                }
            }

            val filteredClips = clipboardItems.filter {
                searchQuery.isBlank() || it.text.contains(searchQuery, ignoreCase = true)
            }
            val unpinnedClips = filteredClips.filter { !it.isPinned }
            val pinnedClips = filteredClips.filter { it.isPinned }
            val pinnedRows = pinnedClips.chunked(2)

            if (filteredClips.isEmpty()) {
                item {
                    EmptyStateCard(
                        text = pick(isAr, "لا توجد نصوص مطابقة في الحافظة.", "No matching clipboard items.")
                    )
                }
            } else {
                // Horizontal scrolling strip for unpinned clipboard items ABOVE the 2-column pinned grid
                if (unpinnedClips.isNotEmpty()) {
                    item(key = "unpinned_horizontal_strip") {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 680.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = pick(
                                    isAr,
                                    "النصوص غير المثبتة (${unpinnedClips.size})",
                                    "Unpinned Clips (${unpinnedClips.size})"
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(unpinnedClips, key = { "unpinned_${it.id}" }) { clip ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.widthIn(min = 170.dp, max = 250.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = clip.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                IconButton(
                                                    onClick = { onTogglePinClip(clip) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.PushPin,
                                                        contentDescription = "Pin",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { localClipboard.setText(AnnotatedString(clip.text)) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.ContentCopy,
                                                        contentDescription = "Copy",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onDeleteClip(clip.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.DeleteOutline,
                                                        contentDescription = "Delete",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
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

                // Two-column grid for pinned clipboard items
                if (pinnedRows.isNotEmpty()) {
                    item(key = "pinned_grid_header") {
                        Text(
                            text = pick(
                                isAr,
                                "النصوص المثبتة (${pinnedClips.size})",
                                "Pinned Clips (${pinnedClips.size})"
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .widthIn(max = 680.dp)
                                .fillMaxWidth()
                        )
                    }
                    items(pinnedRows.size, key = { "pinned_row_$it" }) { rowIdx ->
                        val pair = pinnedRows[rowIdx]
                        Row(
                            modifier = Modifier
                                .widthIn(max = 680.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { clip ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = pick(isAr, "مثبت", "Pinned"),
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = clip.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            IconButton(
                                                onClick = { onTogglePinClip(clip) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.PushPin,
                                                    contentDescription = "Unpin",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { localClipboard.setText(AnnotatedString(clip.text)) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.ContentCopy,
                                                    contentDescription = "Copy",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteClip(clip.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.DeleteOutline,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
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
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = pick(isAr, "إضافة كلمة مخصصة لشريط الاقترحات", "Add Custom Word to Suggestions"),
                            style = MaterialTheme.typography.titleSmall
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            KeyboardLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = newWordLang == lang,
                                    onClick = { newWordLang = lang },
                                    label = { Text(if (isAr) lang.labelAr else lang.labelEn, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newWordText,
                                onValueChange = { newWordText = it },
                                placeholder = { Text(pick(isAr, "أدخل كلمة أو اسماً...", "Enter word or name...")) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newWordText.isNotBlank()) {
                                        onAddCustomWord(newWordText, newWordLang)
                                        newWordText = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(pick(isAr, "إضافة", "Add"))
                            }
                        }
                        TextButton(onClick = { showResetDialog = true }) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(pick(isAr, "إعادة ضبط القاموس المتعلم", "Reset Learned Words"))
                        }
                    }
                }
            }

            val filteredWords = learnedWords
                .filter { searchQuery.isBlank() || it.word.contains(searchQuery, ignoreCase = true) }
                .take(150)

            if (filteredWords.isEmpty()) {
                item {
                    EmptyStateCard(
                        text = pick(isAr, "لا توجد كلمات مطابقة.", "No matching words.")
                    )
                }
            } else {
                items(filteredWords, key = { "${it.languageCode}_${it.word}" }) { word ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = word.languageCode.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "×${word.frequency}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(onClick = { onDeleteLearnedWord(word.word, word.languageCode) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Remove word",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(text: String) {
    Box(
        modifier = Modifier
            .widthIn(max = 680.dp)
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
