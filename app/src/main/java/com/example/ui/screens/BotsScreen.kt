package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotIconOption
import com.example.data.model.BotKnowledgeSource
import com.example.data.model.BuiltinBotAction
import com.example.data.model.CustomBotItem
import com.example.ui.AppStrings.pick
import java.util.UUID

fun botIconVector(option: BotIconOption): ImageVector = when (option) {
    BotIconOption.BOT_CHAT -> Icons.Rounded.SmartToy
    BotIconOption.CONTENT_CREATOR -> Icons.Rounded.EditNote
    BotIconOption.CODE_DEV -> Icons.Rounded.Code
    BotIconOption.SALES_PRO -> Icons.AutoMirrored.Rounded.TrendingUp
    BotIconOption.TEACHER -> Icons.Rounded.School
    BotIconOption.TRANSLATOR -> Icons.Rounded.Translate
    BotIconOption.SUPPORT -> Icons.Rounded.SupportAgent
    BotIconOption.SOCIAL -> Icons.Rounded.Campaign
    BotIconOption.LEGAL -> Icons.Rounded.Gavel
    BotIconOption.IDEA_SPARK -> Icons.Rounded.Lightbulb
}

fun builtinBotActionIcon(action: BuiltinBotAction): ImageVector = when (action) {
    BuiltinBotAction.GRAMMAR -> Icons.Rounded.Spellcheck
    BuiltinBotAction.TRANSLATE -> Icons.Rounded.Translate
    BuiltinBotAction.REPHRASE -> Icons.AutoMirrored.Rounded.Article
    BuiltinBotAction.CHANGE_TONE -> Icons.Rounded.Sync
    BuiltinBotAction.ENHANCE_WORDS -> Icons.Rounded.AutoFixHigh
    BuiltinBotAction.ASK_AI -> Icons.AutoMirrored.Rounded.HelpOutline
    BuiltinBotAction.TO_EMOJI -> Icons.Rounded.EmojiEmotions
    BuiltinBotAction.REPLY -> Icons.AutoMirrored.Rounded.Reply
    BuiltinBotAction.PROOFREAD -> Icons.Rounded.SmartToy
    BuiltinBotAction.CONTINUE_WRITING -> Icons.Rounded.Edit
}

@Composable
fun BotAssistantHubScreen(
    isAr: Boolean,
    customBots: List<CustomBotItem>,
    botTestBusy: Boolean,
    botTestResult: Result<String>?,
    onBack: () -> Unit,
    onCreateBotClick: () -> Unit,
    onEditBotClick: (CustomBotItem) -> Unit,
    onTogglePinBot: (CustomBotItem) -> Unit,
    onDeleteBot: (Long) -> Unit,
    onTestBot: (CustomBotItem, String) -> Unit,
    onClearBotTestResult: () -> Unit,
    onRunBuiltinActionInPlayground: (BuiltinBotAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var testingBot by remember { mutableStateOf<CustomBotItem?>(null) }
    var testInput by remember { mutableStateOf("") }

    testingBot?.let { bot ->
        AlertDialog(
            onDismissRequest = {
                testingBot = null
                onClearBotTestResult()
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(bot.accentArgb)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = botIconVector(bot.iconOption),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(bot.name, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = testInput,
                        onValueChange = { testInput = it },
                        label = { Text(pick(isAr, "نص التجربة (اختياري)", "Test input (optional)")) },
                        placeholder = {
                            Text(pick(isAr, "أدخل نصاً أو اتركه فارغاً للتوليد المباشر", "Enter text or leave empty"))
                        },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (botTestBusy) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text(
                                pick(isAr, "جاري المعالجة بواسطة ${bot.name}...", "Running ${bot.name}..."),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    botTestResult?.let { res ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = res.getOrElse { it.message ?: "Error" },
                                color = if (res.isSuccess) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onTestBot(bot, testInput) },
                    enabled = !botTestBusy
                ) {
                    Text(pick(isAr, "تشغيل البوت", "Run Bot"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        testingBot = null
                        onClearBotTestResult()
                    }
                ) {
                    Text(pick(isAr, "إغلاق", "Close"))
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("bots_hub_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = pick(isAr, "رجوع", "Back")
                    )
                }
                Text(
                    text = pick(isAr, "مساعد روبوت", "Bot Assistant"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onCreateBotClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("create_bot_open_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(pick(isAr, "إنشاء بوت", "Create Bot"))
                }
            }
        }

        // Hero Banner Card matching Reference Image 2
        item {
            BotAssistantPromoCard(
                isAr = isAr,
                botCount = customBots.size,
                onClick = onCreateBotClick
            )
        }

        // User's Custom Bots Section
        item {
            Text(
                text = pick(isAr, "البوتات الخاصة بي (${customBots.size})", "My Bots (${customBots.size})"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (customBots.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = pick(
                                isAr,
                                "لم تقم بإنشاء أي بوت مخصص بعد",
                                "No custom bots created yet"
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = pick(
                                isAr,
                                "أنشئ بوتاً بدور مخصص وقاعدة معرفة ليظهر بأيقونته الخاصة في شريط لوحة المفاتيح.",
                                "Create a custom bot with tailored instructions and knowledge base to appear with its own icon on the keyboard."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onCreateBotClick) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(pick(isAr, "إضافة بوت الآن", "Create Your First Bot"))
                        }
                    }
                }
            }
        } else {
            items(customBots, key = { it.id }) { bot ->
                CustomBotManageCard(
                    isAr = isAr,
                    bot = bot,
                    onRunTest = {
                        testInput = ""
                        onClearBotTestResult()
                        testingBot = bot
                    },
                    onEdit = { onEditBotClick(bot) },
                    onTogglePin = { onTogglePinBot(bot) },
                    onDelete = { onDeleteBot(bot.id) }
                )
            }
        }

        // Built-in 2-Column Smart AI Tools Grid matching Reference Image 3
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                text = pick(isAr, "أدوات الذكاء السريعة في اللوحة", "Quick Keyboard AI Tools"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            val chunked = BuiltinBotAction.entries.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                chunked.forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { action ->
                            Surface(
                                onClick = { onRunBuiltinActionInPlayground(action) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("hub_builtin_${action.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = builtinBotActionIcon(action),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (isAr) action.labelAr else action.labelEn,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotAssistantPromoCard(
    isAr: Boolean,
    botCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("bot_assistant_promo_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4F8FF7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = pick(isAr, "مساعد روبوت", "Bot Assistant"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (botCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "$botCount",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = pick(
                        isAr,
                        "أنشئ مساعدك الخاص للتعامل مع جميع متطلباتك الشخصية",
                        "Create your own assistant to handle all your personal tasks"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CustomBotManageCard(
    isAr: Boolean,
    bot: CustomBotItem,
    onRunTest: () -> Unit,
    onEdit: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val botAccent = Color(bot.accentArgb)
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .testTag("custom_bot_card_${bot.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(botAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = botIconVector(bot.iconOption),
                        contentDescription = bot.name,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bot.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAr) bot.iconOption.labelAr else bot.iconOption.labelEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = botAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (bot.knowledgeSources.isNotEmpty()) {
                            Text(
                                text = "• ${bot.knowledgeSources.size} ${pick(isAr, "مصدر معرفة", "knowledge")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = pick(isAr, "تثبيت في الشريط", "Pin to toolbar"),
                        tint = if (bot.isPinnedToToolbar) botAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = bot.prompt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRunTest,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(pick(isAr, "تجربة", "Test"))
                }
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(pick(isAr, "تعديل", "Edit"))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = pick(isAr, "حذف", "Delete"),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CreateOrEditBotScreen(
    isAr: Boolean,
    editingBot: CustomBotItem?,
    onBack: () -> Unit,
    onSaveBot: (
        id: Long,
        name: String,
        prompt: String,
        iconOption: BotIconOption,
        accentArgb: Long,
        knowledgeSources: List<BotKnowledgeSource>,
        isPinnedToToolbar: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember(editingBot?.id) { mutableStateOf(editingBot?.name ?: "") }
    var prompt by remember(editingBot?.id) { mutableStateOf(editingBot?.prompt ?: "") }
    var selectedIcon by remember(editingBot?.id) {
        mutableStateOf(editingBot?.iconOption ?: BotIconOption.BOT_CHAT)
    }
    var selectedColorArgb by remember(editingBot?.id) {
        mutableStateOf(editingBot?.accentArgb ?: BotIconOption.BOT_CHAT.accentArgb)
    }
    var isPinnedToToolbar by remember(editingBot?.id) {
        mutableStateOf(editingBot?.isPinnedToToolbar ?: true)
    }
    val knowledgeSources = remember(editingBot?.id) {
        mutableStateListOf<BotKnowledgeSource>().apply {
            editingBot?.knowledgeSources?.let(::addAll)
        }
    }

    var showIconPicker by remember { mutableStateOf(false) }
    var showPromptHelp by remember { mutableStateOf(false) }
    var showKnowledgeChooser by remember { mutableStateOf(false) }
    var showAddTextKnowledgeDialog by remember { mutableStateOf(false) }
    var knowledgeTitle by remember { mutableStateOf("") }
    var knowledgeContent by remember { mutableStateOf("") }
    var knowledgeError by remember { mutableStateOf<String?>(null) }

    // Zero-permission SAF document picker for Knowledge Base files
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                    it.readText()
                }.orEmpty()
                if (content.isNotBlank()) {
                    val fileName = uri.lastPathSegment
                        ?.substringAfterLast('/')
                        ?.substringAfterLast(':')
                        ?.ifBlank { "Document" }
                        ?: "Document"
                    knowledgeSources.add(
                        BotKnowledgeSource(
                            id = UUID.randomUUID().toString(),
                            title = fileName,
                            content = content.take(12000),
                            sourceType = "file"
                        )
                    )
                    knowledgeError = null
                } else {
                    knowledgeError = pick(isAr, "الملف فارغ أو غير نصي", "File is empty or unsupported")
                }
            } catch (_: Exception) {
                knowledgeError = pick(isAr, "تعذر قراءة الملف المحدد", "Unable to read selected file")
            }
        }
    }

    if (showIconPicker) {
        AlertDialog(
            onDismissRequest = { showIconPicker = false },
            title = { Text(pick(isAr, "اختر أيقونة ولون البوت", "Choose Bot Icon & Color")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(124.dp)
                    ) {
                        items(BotIconOption.entries) { option ->
                            val isSelected = selectedIcon == option
                            val bg = Color(option.accentArgb)
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) bg else bg.copy(alpha = 0.22f))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else bg.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedIcon = option
                                        selectedColorArgb = option.accentArgb
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = botIconVector(option),
                                    contentDescription = if (isAr) option.labelAr else option.labelEn,
                                    tint = if (isSelected) Color.White else bg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isAr) selectedIcon.labelAr else selectedIcon.labelEn,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(selectedColorArgb),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showIconPicker = false }) {
                    Text(pick(isAr, "اعتماد الأيقونة", "Done"))
                }
            }
        )
    }

    if (showPromptHelp) {
        AlertDialog(
            onDismissRequest = { showPromptHelp = false },
            title = { Text(pick(isAr, "نصائح لكتابة مطالب البوت", "Bot Prompt Tips")) },
            text = {
                Text(
                    text = pick(
                        isAr,
                        "• حدد دور البوت بدقة (مثلاً: خبير تسويق، مساعد بريد رسمي، مبرمج كوتلن).\n• اذكر الأسلوب المطلوب (مختصر، احترافي، بالعامية أو الفصحى).\n• أضف مصادر معرفة عند الحاجة ليلتزم بها في كل إجابة.",
                        "• Define the bot's role clearly (e.g., Marketing Expert, Email Assistant, Kotlin Dev).\n• Specify tone & output structure.\n• Attach knowledge sources when the bot needs product or project context."
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showPromptHelp = false }) {
                    Text(pick(isAr, "فهمت", "Got it"))
                }
            }
        )
    }

    if (showKnowledgeChooser) {
        AlertDialog(
            onDismissRequest = { showKnowledgeChooser = false },
            title = { Text(pick(isAr, "إضافة مصدر للمعرفة", "Add Knowledge Source")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        onClick = {
                            showKnowledgeChooser = false
                            knowledgeTitle = ""
                            knowledgeContent = ""
                            showAddTextKnowledgeDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(pick(isAr, "كتابة أو لصق نص معرفي", "Write or Paste Notes"), fontWeight = FontWeight.Bold)
                                Text(
                                    pick(isAr, "وصف منتجات، تعليمات عمل، أمثلة ردود", "Product details, guidelines, FAQs"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            showKnowledgeChooser = false
                            documentLauncher.launch(arrayOf("text/*", "application/json"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.UploadFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(pick(isAr, "استيراد ملف نصي (TXT / MD / JSON)", "Import Text Document"), fontWeight = FontWeight.Bold)
                                Text(
                                    pick(isAr, "اختيار آمن عبر مدير ملفات النظام", "Secure system document picker"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showKnowledgeChooser = false }) {
                    Text(pick(isAr, "إلغاء", "Cancel"))
                }
            }
        )
    }

    if (showAddTextKnowledgeDialog) {
        AlertDialog(
            onDismissRequest = { showAddTextKnowledgeDialog = false },
            title = { Text(pick(isAr, "إضافة نص لقاعدة المعرفة", "Add Text Knowledge")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = knowledgeTitle,
                        onValueChange = { knowledgeTitle = it },
                        label = { Text(pick(isAr, "عنوان المصدر", "Source Title")) },
                        placeholder = { Text(pick(isAr, "مثال: سياسة المتجر", "e.g., Store Policy")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = knowledgeContent,
                        onValueChange = { knowledgeContent = it },
                        label = { Text(pick(isAr, "المحتوى المعرفي", "Knowledge Content")) },
                        placeholder = { Text(pick(isAr, "الصق المعلومات التي سيتذكرها البوت...", "Paste context for the bot...")) },
                        minLines = 4,
                        maxLines = 7,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (knowledgeTitle.isNotBlank() && knowledgeContent.isNotBlank()) {
                            knowledgeSources.add(
                                BotKnowledgeSource(
                                    id = UUID.randomUUID().toString(),
                                    title = knowledgeTitle.trim(),
                                    content = knowledgeContent.trim().take(12000),
                                    sourceType = "text"
                                )
                            )
                            knowledgeError = null
                            showAddTextKnowledgeDialog = false
                        }
                    },
                    enabled = knowledgeTitle.isNotBlank() && knowledgeContent.isNotBlank()
                ) {
                    Text(pick(isAr, "إضافة", "Add"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextKnowledgeDialog = false }) {
                    Text(pick(isAr, "إلغاء", "Cancel"))
                }
            }
        )
    }

    val canSave = name.isNotBlank() && prompt.isNotBlank()

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar matching Reference Image 1
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("create_bot_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = pick(isAr, "رجوع", "Back")
                        )
                    }
                    Text(
                        text = if (editingBot != null) {
                            pick(isAr, "تعديل البوت", "Edit Bot")
                        } else {
                            pick(isAr, "إنشاء بوت", "Create Bot")
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Name * + Bot Icon Picker (Image 1)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = pick(isAr, "الاسم", "Name"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "*",
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = {
                                Text(pick(isAr, "على سبيل المثال: مُنشئ محتوى", "e.g., Content Creator"))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_bot_name_input")
                        )

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(selectedColorArgb).copy(alpha = 0.16f))
                                .border(1.5.dp, Color(selectedColorArgb), CircleShape)
                                .clickable { showIconPicker = true }
                                .testTag("create_bot_icon_picker"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = botIconVector(selectedIcon),
                                contentDescription = pick(isAr, "اختيار أيقونة البوت", "Pick Bot Icon"),
                                tint = Color(selectedColorArgb),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Prompt * + Help icon + Explanation + Multiline field (Image 1)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = pick(isAr, "أدخل مطالبك", "Enter your prompt"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "*",
                                color = Color(0xFFEF4444),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { showPromptHelp = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                                contentDescription = pick(isAr, "مساعدة", "Help"),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = pick(
                            isAr,
                            "قل للروبوت ما هو دوره (مثل: خبير في المبيعات، مُنشئ محتوى..) وما المهام التي سيقوم بها لك. لاحظ أنه كلما كانت التعليمات أوضح وأكثر تفصيلاً، كان الروبوت أكثر دقة في معالجة طلباتك.",
                            "Tell the bot its role (e.g., Sales Expert, Content Creator) and what tasks it should do. Clearer, detailed instructions yield more accurate results."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = {
                            Text(
                                pick(
                                    isAr,
                                    "على سبيل المثال: أنت مُنشئ محتوى محترف، اكتب منشورًا اجتماعيًا بأسلوب احترافي، يشمل: افتتاح جذاب، محتوى رئيسي، واستنتاج بدعوة للعمل.",
                                    "Example: You are a professional content creator. Write an engaging social post with a hook, body, and call to action."
                                )
                            )
                        },
                        minLines = 6,
                        maxLines = 10,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_bot_prompt_input")
                    )
                }
            }

            // Knowledge Base (Optional) (Image 1)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = pick(isAr, "قاعدة المعرفة (اختياري)", "Knowledge Base (Optional)"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        onClick = { showKnowledgeChooser = true },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_knowledge_source_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color(0xFF4F8FF7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = pick(isAr, "إضافة مصدر للمعرفة", "Add knowledge source"),
                                color = Color(0xFF4F8FF7),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    knowledgeError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    knowledgeSources.forEach { src ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (src.sourceType == "file") Icons.Rounded.UploadFile else Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = src.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${src.content.length} ${pick(isAr, "حرف", "chars")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { knowledgeSources.remove(src) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = pick(isAr, "إزالة", "Remove"),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = pick(
                            isAr,
                            "قدّم المستندات التي تريد أن يتذكرها البوت — مثل وصف المنتجات، تفاصيل المشاريع، سير العمل، أو أهداف العمل. هذا يساعد البوت على الاستجابة بدقة والبقاء متوافقًا مع احتياجاتك الواقعية.",
                            "Provide documents you want the bot to remember — such as product descriptions, project specs, workflows, or business goals."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )
                }
            }

            // Pin to Keyboard Toolbar toggle
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pick(
                                    isAr,
                                    "إظهار أيقونة البوت في شريط لوحة المفاتيح",
                                    "Show bot icon directly in keyboard bar"
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = pick(
                                    isAr,
                                    "يتيح تشغيل هذا البوت بضغطة واحدة عبر أيقونته المخصصة",
                                    "Run this bot in one tap from its dedicated toolbar icon"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isPinnedToToolbar,
                            onCheckedChange = { isPinnedToToolbar = it },
                            modifier = Modifier.testTag("create_bot_pin_switch")
                        )
                    }
                }
            }
        }

        // Bottom CTA Button ("إضافة إلى الروبوت الخاص بي") matching Image 1
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    onSaveBot(
                        editingBot?.id ?: 0L,
                        name,
                        prompt,
                        selectedIcon,
                        selectedColorArgb,
                        knowledgeSources.toList(),
                        isPinnedToToolbar
                    )
                },
                enabled = canSave,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp)
                    .testTag("save_bot_button")
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (editingBot != null) {
                        pick(isAr, "حفظ التعديلات", "Save Bot Changes")
                    } else {
                        pick(isAr, "إضافة إلى الروبوت الخاص بي", "Add to My Bots")
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
