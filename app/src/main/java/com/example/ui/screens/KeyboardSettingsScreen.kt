package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.KeyboardSizeConfig
import com.example.data.model.QuickInputTool
import com.example.data.model.ToolbarAction
import com.example.data.model.ToolbarVisibilityConfig
import com.example.ui.AppStrings.pick

@Composable
fun KeyboardSettingsScreen(
    isAr: Boolean,
    sizeConfig: KeyboardSizeConfig,
    toolbarConfig: ToolbarVisibilityConfig,
    onUpdateSizeConfig: ((KeyboardSizeConfig) -> KeyboardSizeConfig) -> Unit,
    onUpdateToolbarConfig: ((ToolbarVisibilityConfig) -> ToolbarVisibilityConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SettingsCard(title = pick(isAr, "حجم لوحة المفاتيح والشريط", "Keyboard and toolbar size")) {
                Text("${pick(isAr, "حجم الأيقونات", "Icon size")}: ${(sizeConfig.toolbarIconScale * 100).toInt()}%")
                Slider(
                    value = sizeConfig.toolbarIconScale,
                    onValueChange = { value -> onUpdateSizeConfig { it.copy(toolbarIconScale = value) } },
                    valueRange = 0.75f..1.35f,
                    steps = 11
                )
                Text("${pick(isAr, "ارتفاع اللوحة", "Keyboard height")}: ${(sizeConfig.heightScale * 100).toInt()}%")
                Slider(
                    value = sizeConfig.heightScale,
                    onValueChange = { value -> onUpdateSizeConfig { it.copy(heightScale = value) } },
                    valueRange = 0.78f..1.35f
                )
                SettingSwitch(
                    label = pick(isAr, "إظهار صف الأرقام", "Show number row"),
                    checked = sizeConfig.showNumberRow,
                    onCheckedChange = { value -> onUpdateSizeConfig { it.copy(showNumberRow = value) } }
                )
                SettingSwitch(
                    label = pick(isAr, "إظهار شريط الاقتراحات", "Show suggestions"),
                    checked = sizeConfig.showSuggestions,
                    onCheckedChange = { value -> onUpdateSizeConfig { it.copy(showSuggestions = value) } }
                )
            }
        }
        item {
            SettingsCard(title = pick(isAr, "ترتيب وإظهار أزرار الشريط", "Toolbar buttons")) {
                Text(pick(isAr, "اضغط على الزر لإظهاره أو إخفائه، واستخدم الأسهم لتغيير ترتيبه.", "Tap a button to show or hide it, then change its order."), style = MaterialTheme.typography.bodySmall)
            }
        }
        items(toolbarConfig.order, key = { it.id }) { action ->
            val visible = toolbarConfig.isVisible(action)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = visible,
                    onClick = { onUpdateToolbarConfig { it.withVisibility(action, !visible) } },
                    label = { Text(if (isAr) action.labelAr else action.labelEn) }
                )
                FilterChip(
                    selected = false,
                    enabled = toolbarConfig.order.indexOf(action) > 0,
                    onClick = { onUpdateToolbarConfig { it.moveAction(action, -1) } },
                    label = { Text("↑") }
                )
                FilterChip(
                    selected = false,
                    enabled = toolbarConfig.order.indexOf(action) < toolbarConfig.order.lastIndex,
                    onClick = { onUpdateToolbarConfig { it.moveAction(action, 1) } },
                    label = { Text("↓") }
                )
            }
        }
        item {
            SettingsCard(title = pick(isAr, "أدوات شبكة الإدخال", "Input grid tools")) {
                SettingSwitch(
                    label = pick(isAr, "وضع الأدوات في صف ثانٍ تحت الشريط الرئيسي", "Put tools in a second row below the main toolbar"),
                    checked = toolbarConfig.quickToolsSecondRow,
                    onCheckedChange = { value -> onUpdateToolbarConfig { it.copy(quickToolsSecondRow = value) } }
                )
                QuickInputTool.entries.forEach { tool ->
                    FilterChip(
                        selected = tool in toolbarConfig.quickTools,
                        onClick = {
                            onUpdateToolbarConfig { current ->
                                current.copy(quickTools = if (tool in current.quickTools) current.quickTools - tool else current.quickTools + tool)
                            }
                        },
                        label = { Text(if (isAr) tool.labelAr else tool.labelEn) },
                        modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Tune, contentDescription = null)
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
