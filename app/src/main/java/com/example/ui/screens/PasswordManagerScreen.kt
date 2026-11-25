package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PasswordEntryItem
import com.example.data.model.PasswordServiceBrand
import com.example.ui.AppStrings.pick
import com.example.ui.components.ProvideSafeTextToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import com.example.ui.components.ServiceBrandIcon

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PasswordManagerScreen(
    isAr: Boolean,
    entries: List<PasswordEntryItem>,
    onSaveEntry: (
        id: Long,
        siteName: String,
        siteUrl: String,
        username: String,
        password: String,
        brand: PasswordServiceBrand
    ) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onSaveEntryWithIcon: ((Long, String, String, String, String, PasswordServiceBrand, String?) -> Unit)? = null
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val textToolbar = LocalTextToolbar.current
    var searchQuery by remember { mutableStateOf("") }
    var editingEntry by remember { mutableStateOf<PasswordEntryItem?>(null) }
    var showEditorDialog by remember { mutableStateOf(false) }
    var inspectingEntry by remember { mutableStateOf<PasswordEntryItem?>(null) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    val copyToSystemClipboard: (String, String) -> Unit = { label, secret ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, secret))
        copiedNotice = if (isAr) "تم نسخ $label" else "$label copied"
    }

    if (showEditorDialog) {
        PasswordEntryEditorDialog(
            isAr = isAr,
            initial = editingEntry,
            onDismiss = {
                showEditorDialog = false
                editingEntry = null
            },
            onSave = { siteName, siteUrl, username, password, brand, localIconPath ->
                onSaveEntryWithIcon?.invoke(
                    editingEntry?.id ?: 0L, siteName, siteUrl, username, password, brand, localIconPath
                ) ?: onSaveEntry(
                    editingEntry?.id ?: 0L,
                    siteName,
                    siteUrl,
                    username,
                    password,
                    brand
                )
                showEditorDialog = false
                editingEntry = null
            }
        )
    }

    inspectingEntry?.let { active ->
        CredentialPopupDialog(
            isAr = isAr,
            entry = active,
            onCopyUsername = { copyToSystemClipboard(pick(isAr, "اسم المستخدم", "Username"), active.username) },
            onCopyPassword = { copyToSystemClipboard(pick(isAr, "كلمة المرور", "Password"), active.password) },
            onEdit = {
                inspectingEntry = null
                editingEntry = active
                showEditorDialog = true
            },
            onDelete = {
                inspectingEntry = null
                onDeleteEntry(active.id)
            },
            onDismiss = { inspectingEntry = null }
        )
    }

    val filtered = remember(entries, searchQuery) {
        if (searchQuery.isBlank()) entries
        else entries.filter {
            it.siteName.contains(searchQuery, ignoreCase = true) ||
                it.username.contains(searchQuery, ignoreCase = true) ||
                it.siteUrl.contains(searchQuery, ignoreCase = true)
        }
    }
    val rows = remember(filtered) { filtered.chunked(2) }

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
                // Header card matching the screenshot's aesthetic
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VpnKey,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pick(isAr, "مدير كلمات المرور", "Password Manager"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = pick(
                                    isAr,
                                    "محفوظة ومشفرة محلياً للوصول السريع من شريط لوحة المفاتيح",
                                    "Encrypted locally on-device for instant access from the keyboard bar"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                textToolbar.hide()
                                focusManager.clearFocus(force = true)
                                editingEntry = null
                                showEditorDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_password_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(pick(isAr, "إضافة", "Add"))
                        }
                    }
                }

                if (copiedNotice != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.14f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = copiedNotice.orEmpty(),
                            color = Color(0xFF10B981),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = { Text(pick(isAr, "بحث في المواقع أو الحسابات...", "Search sites or accounts...")) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (rows.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VpnKey,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = pick(isAr, "خزنة كلمات المرور فارغة", "Your Password Vault is Empty"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = pick(
                                isAr,
                                "احفظ مواقعك وحساباتك هنا لإدراج اسم المستخدم وكلمة المرور بضغطة واحدة من لوحة المفاتيح.",
                                "Save your accounts here to insert usernames and passwords in one tap from the keyboard."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                editingEntry = null
                                showEditorDialog = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(pick(isAr, "إضافة أول حساب", "Add First Account"))
                        }
                    }
                }
            }
        } else {
            items(rows.size, key = { "pw_row_$it" }) { rowIdx ->
                val pair = rows[rowIdx]
                Row(
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { entry ->
                        Surface(
                            onClick = {
                                textToolbar.hide()
                                focusManager.clearFocus(force = true)
                                inspectingEntry = entry
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("password_card_${entry.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ServiceBrandIcon(brand = entry.brand, localIconPath = entry.localIconPath, size = 38.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.siteName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (entry.username.isNotBlank() && entry.username != entry.siteName) {
                                        Text(
                                            text = entry.username,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Inspect credentials",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(20.dp)
                                )
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

@Composable
private fun CredentialPopupDialog(
    isAr: Boolean,
    entry: PasswordEntryItem,
    onCopyUsername: () -> Unit,
    onCopyPassword: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var reveal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { ServiceBrandIcon(brand = entry.brand, localIconPath = entry.localIconPath, size = 44.dp) },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(entry.siteName, fontWeight = FontWeight.Bold)
                if (entry.siteUrl.isNotBlank()) {
                    Text(
                        text = entry.siteUrl,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    onClick = onCopyUsername,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = Color(0xFF3B82F6))
                        Text(
                            text = entry.username,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy username", modifier = Modifier.size(16.dp))
                    }
                }

                Surface(
                    onClick = onCopyPassword,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFF3B82F6))
                        Text(
                            text = if (reveal) entry.password else "•".repeat(entry.password.length.coerceIn(8, 14)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { reveal = !reveal }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (reveal) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Toggle visibility",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy password", modifier = Modifier.size(16.dp))
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDelete) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(pick(isAr, "حذف", "Delete"), color = MaterialTheme.colorScheme.error)
                }
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(pick(isAr, "تعديل", "Edit"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(pick(isAr, "إغلاق", "Close"))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PasswordEntryEditorDialog(
    isAr: Boolean,
    initial: PasswordEntryItem?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, PasswordServiceBrand, String?) -> Unit
) {
    var siteName by remember(initial) { mutableStateOf(initial?.siteName.orEmpty()) }
    var siteUrl by remember(initial) { mutableStateOf(initial?.siteUrl.orEmpty()) }
    var username by remember(initial) { mutableStateOf(initial?.username.orEmpty()) }
    var password by remember(initial) { mutableStateOf(initial?.password.orEmpty()) }
    var selectedBrand by remember(initial) { mutableStateOf(initial?.brand ?: PasswordServiceBrand.GOOGLE) }
    var localIconPath by remember(initial) { mutableStateOf(initial?.localIconPath) }
    var iconBusy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            iconBusy = true
            scope.launch {
                localIconPath = withContext(Dispatchers.IO) {
                    runCatching {
                        val directory = File(context.filesDir, "password_icons").apply { mkdirs() }
                        val destination = File(directory, "${UUID.randomUUID()}.img")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            destination.outputStream().use(input::copyTo)
                        } ?: error("Unable to read image")
                        destination.absolutePath
                    }.getOrNull()
                }
                iconBusy = false
            }
        }
    }
    var showPassword by remember { mutableStateOf(false) }
    var dialogToolbarRef by remember { mutableStateOf<androidx.compose.ui.platform.TextToolbar?>(null) }

    AlertDialog(
        onDismissRequest = {
            dialogToolbarRef?.hide()
            onDismiss()
        },
        title = {
            Text(
                if (initial == null) pick(isAr, "إضافة كلمة مرور جديدة", "Add New Password")
                else pick(isAr, "تعديل الحساب", "Edit Account")
            )
        },
        text = {
            ProvideSafeTextToolbar {
                val dialogToolbar = LocalTextToolbar.current
                dialogToolbarRef = dialogToolbar
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = pick(isAr, "أيقونة الموقع / التطبيق", "Site / App Icon"),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ServiceBrandIcon(brand = selectedBrand, localIconPath = localIconPath, size = 42.dp)
                        OutlinedButton(onClick = { dialogToolbar.hide(); imagePicker.launch(arrayOf("image/*")) }) {
                            Text(pick(isAr, "اختيار صورة", "Choose image"))
                        }
                        if (localIconPath != null) TextButton(onClick = { localIconPath = null }) {
                            Text(pick(isAr, "إزالة", "Remove"))
                        }
                        if (iconBusy) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PasswordServiceBrand.entries.forEach { brand ->
                            FilterChip(
                                selected = selectedBrand == brand,
                                onClick = {
                                    dialogToolbar.hide()
                                    selectedBrand = brand
                                    if (siteName.isBlank() && brand != PasswordServiceBrand.CUSTOM) {
                                        siteName = brand.labelEn
                                    }
                                },
                                leadingIcon = { ServiceBrandIcon(brand = brand, size = 18.dp) },
                                label = { Text(if (isAr) brand.labelAr else brand.labelEn, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = siteName,
                        onValueChange = {
                            siteName = it
                            if (initial == null) {
                                val detected = PasswordServiceBrand.detectFrom(it, siteUrl)
                                if (detected != PasswordServiceBrand.CUSTOM) selectedBrand = detected
                            }
                        },
                        label = { Text(pick(isAr, "اسم الموقع أو الخدمة (مثال: Google, X)", "Site / App Name")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("pw_site_input")
                    )
                    OutlinedTextField(
                        value = siteUrl,
                        onValueChange = { siteUrl = it },
                        label = { Text(pick(isAr, "الرابط (اختياري)", "Website URL (optional)")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(pick(isAr, "اسم المستخدم أو البريد", "Username / Email")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("pw_user_input")
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(pick(isAr, "كلمة المرور", "Password")) },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                dialogToolbar.hide()
                                showPassword = !showPassword
                            }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("pw_secret_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    dialogToolbarRef?.hide()
                    onSave(siteName, siteUrl, username, password, selectedBrand, localIconPath)
                },
                enabled = username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.testTag("pw_save_button")
            ) {
                Text(pick(isAr, "حفظ", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                dialogToolbarRef?.hide()
                onDismiss()
            }) {
                Text(pick(isAr, "إلغاء", "Cancel"))
            }
        }
    )
}
