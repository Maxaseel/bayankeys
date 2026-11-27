package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.AppStrings.pick
import kotlinx.coroutines.launch

@Composable
fun BackupScreen(
    isAr: Boolean,
    onExportBackup: suspend () -> String,
    onImportBackup: suspend (String) -> Result<Int>,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null) scope.launch {
            message = try {
                val json = onExportBackup()
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                    ?: error("Unable to open file")
                pick(isAr, "تم تصدير النسخة الاحتياطية بنجاح", "Backup exported successfully")
            } catch (_: Throwable) {
                pick(isAr, "تعذر تصدير النسخة الاحتياطية", "Could not export backup")
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) scope.launch {
            message = try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Unable to read file")
                val count = onImportBackup(json).getOrThrow()
                pick(isAr, "تمت الاستعادة بنجاح ($count كلمة)", "Restore completed ($count words)")
            } catch (_: Throwable) {
                pick(isAr, "ملف النسخة الاحتياطية غير صالح", "Invalid backup file")
            }
        }
    }
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.CloudUpload, contentDescription = null)
                Text(pick(isAr, "النسخ الاحتياطي والاستعادة", "Backup and restore"), style = MaterialTheme.typography.titleLarge)
                Text(pick(isAr, "يحفظ كلمات المرور والنصوص المثبتة وترتيبها والكلمات المتعلمة وإعدادات التطبيق وشريط لوحة المفاتيح.", "Saves passwords, pinned clipboard text and order, learned words, app settings, and keyboard toolbar settings."))
            }
        }
        Button(onClick = { exportLauncher.launch("bayan-keys-backup.json") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.CloudUpload, contentDescription = null)
            Text(pick(isAr, "تصدير نسخة احتياطية", "Export backup"), modifier = Modifier.padding(start = 8.dp))
        }
        Button(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.CloudDownload, contentDescription = null)
            Text(pick(isAr, "استعادة نسخة احتياطية", "Restore backup"), modifier = Modifier.padding(start = 8.dp))
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.Warning, contentDescription = null)
                Text(pick(isAr, "تنبيه أمني", "Security warning"), style = MaterialTheme.typography.titleMedium)
                Text(pick(isAr, "ملف النسخة يحتوي على كلمات المرور. احفظه في مكان آمن ولا تشاركه.", "The backup contains passwords. Keep it secure and do not share it."))
            }
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    }
}
