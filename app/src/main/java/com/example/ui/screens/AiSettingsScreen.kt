package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.CustomProviderEntity
import com.example.data.model.AiProviderConfig
import com.example.data.model.AiProviderType
import com.example.data.model.TargetTranslateLanguage
import com.example.data.model.TranslationProviderType
import com.example.ui.AppStrings.pick
import com.example.ui.ConnectionTestState
import com.example.ui.FetchedModelsState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiSettingsScreen(
    isAr: Boolean,
    aiConfig: AiProviderConfig,
    customProviders: List<CustomProviderEntity>,
    aiTestState: ConnectionTestState,
    translationTestState: ConnectionTestState,
    fetchedModelsState: FetchedModelsState,
    onSelectAiProvider: (AiProviderType) -> Unit,
    onSaveAiCredentials: (AiProviderType, String, String, String?) -> Unit,
    onTestAiConnection: (AiProviderType, String, String, String?) -> Unit,
    onFetchAvailableModels: (AiProviderType, String, String) -> Unit,
    onResetFetchedModels: () -> Unit,
    onSaveAndActivateCustomProvider: (
        id: Long,
        name: String,
        baseUrl: String,
        apiKey: String,
        model: String,
        fetchedModels: List<String>
    ) -> Unit,
    onActivateSavedCustomProvider: (CustomProviderEntity) -> Unit,
    onDeleteCustomProvider: (CustomProviderEntity) -> Unit,
    onSaveTranslationConfig: (TranslationProviderType, String, String, TargetTranslateLanguage) -> Unit,
    onTestTranslationConnection: (TranslationProviderType, String, String, TargetTranslateLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    var editingCustomId by remember { mutableLongStateOf(aiConfig.activeCustomProviderId ?: 0L) }
    var customNameInput by remember { mutableStateOf(aiConfig.activeCustomProviderName ?: "") }
    var apiKeyInput by remember { mutableStateOf(aiConfig.aiApiKey) }
    var modelInput by remember { mutableStateOf(aiConfig.aiModel) }
    var customBaseUrlInput by remember { mutableStateOf(aiConfig.aiCustomBaseUrl) }
    var showAiKey by remember { mutableStateOf(false) }
    var aiSavedBanner by remember { mutableStateOf(false) }

    var transProvider by remember { mutableStateOf(aiConfig.translationProvider) }
    var transApiKey by remember { mutableStateOf(aiConfig.translationApiKey) }
    var transEndpoint by remember { mutableStateOf(aiConfig.translationEndpoint) }
    var transTargetLang by remember { mutableStateOf(aiConfig.defaultTargetLanguage) }
    var showTransKey by remember { mutableStateOf(false) }
    var transSavedBanner by remember { mutableStateOf(false) }
    val keyIsOptional = aiConfig.aiProvider.isApiKeyOptional(customBaseUrlInput)

    LaunchedEffect(
        aiConfig.aiProvider,
        aiConfig.activeCustomProviderId,
        aiConfig.activeCustomProviderName,
        aiConfig.aiApiKey,
        aiConfig.aiModel,
        aiConfig.aiCustomBaseUrl
    ) {
        editingCustomId = aiConfig.activeCustomProviderId ?: 0L
        customNameInput = aiConfig.activeCustomProviderName ?: ""
        apiKeyInput = aiConfig.aiApiKey
        modelInput = aiConfig.aiModel
        customBaseUrlInput = aiConfig.aiCustomBaseUrl
    }

    LaunchedEffect(
        aiConfig.translationProvider,
        aiConfig.translationApiKey,
        aiConfig.translationEndpoint,
        aiConfig.defaultTargetLanguage
    ) {
        transProvider = aiConfig.translationProvider
        transApiKey = aiConfig.translationApiKey
        transEndpoint = aiConfig.translationEndpoint
        transTargetLang = aiConfig.defaultTargetLanguage
    }

    val fetchedModels = (fetchedModelsState as? FetchedModelsState.Success)?.models.orEmpty()

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
                // Section 1: Built-in + Saved Custom AI Providers
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pick(isAr, "مزود الذكاء الاصطناعي والنماذج", "AI Provider & Custom Endpoints"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = pick(
                                        isAr,
                                        "اختر مزوداً جاهزاً أو أضف أي رابط مزود مخصص، اجلب نماذجه المتاحة، واحفظه للاستخدام في أي وقت.",
                                        "Select a provider or add any custom API URL, fetch its available models, test connection, and save it."
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Built-in Providers + "+ New Custom" card
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AiProviderType.entries.filter { it != AiProviderType.CUSTOM }) { provider ->
                                val selected = provider == aiConfig.aiProvider && aiConfig.activeCustomProviderId == null
                                Surface(
                                    onClick = {
                                        aiSavedBanner = false
                                        editingCustomId = 0L
                                        onSelectAiProvider(provider)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                    border = BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.testTag("provider_${provider.id}")
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                        Text(
                                            text = if (isAr) provider.displayNameAr else provider.displayNameEn,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = provider.defaultModel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Add New Custom Provider button card
                            item {
                                val isNewCustom = aiConfig.aiProvider == AiProviderType.CUSTOM && aiConfig.activeCustomProviderId == null
                                Surface(
                                    onClick = {
                                        aiSavedBanner = false
                                        editingCustomId = 0L
                                        customNameInput = ""
                                        apiKeyInput = ""
                                        customBaseUrlInput = AiProviderType.CUSTOM.defaultBaseUrl
                                        modelInput = AiProviderType.CUSTOM.defaultModel
                                        onResetFetchedModels()
                                        onSelectAiProvider(AiProviderType.CUSTOM)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isNewCustom) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                                    border = BorderStroke(
                                        width = if (isNewCustom) 1.5.dp else 1.dp,
                                        color = if (isNewCustom) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.testTag("provider_new_custom")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = pick(isAr, "إضافة مزود مخصص", "Add Custom"),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }

                        // Saved Custom Providers List (Always selectable anytime)
                        if (customProviders.isNotEmpty()) {
                            Text(
                                text = pick(isAr, "المزودات المخصصة المحفوظة:", "Saved Custom Providers:"),
                                style = MaterialTheme.typography.labelLarge
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(customProviders, key = { it.id }) { custom ->
                                    val isActive = aiConfig.aiProvider == AiProviderType.CUSTOM &&
                                        aiConfig.activeCustomProviderId == custom.id
                                    Surface(
                                        onClick = {
                                            aiSavedBanner = false
                                            editingCustomId = custom.id
                                            customNameInput = custom.name
                                            customBaseUrlInput = custom.baseUrl
                                            apiKeyInput = custom.apiKey
                                            modelInput = custom.selectedModel
                                            onActivateSavedCustomProvider(custom)
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        border = BorderStroke(
                                            width = if (isActive) 1.5.dp else 1.dp,
                                            color = if (isActive) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline
                                        ),
                                        modifier = Modifier.testTag("saved_custom_${custom.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = custom.name,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = custom.selectedModel.ifBlank { "Custom" },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteCustomProvider(custom) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.DeleteOutline,
                                                    contentDescription = "Delete Custom Provider",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Custom Provider Name & URL fields
                        if (aiConfig.aiProvider == AiProviderType.CUSTOM) {
                            OutlinedTextField(
                                value = customNameInput,
                                onValueChange = {
                                    customNameInput = it
                                    aiSavedBanner = false
                                },
                                label = { Text(pick(isAr, "اسم المزود (مثال: DeepSeek أو Ollama)", "Provider Name (e.g. DeepSeek, Ollama)")) },
                                leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_provider_name_input")
                            )

                            OutlinedTextField(
                                value = customBaseUrlInput,
                                onValueChange = {
                                    customBaseUrlInput = it
                                    aiSavedBanner = false
                                },
                                label = { Text(pick(isAr, "رابط المزود (Base URL أو Chat Completions)", "Custom Provider URL (e.g. https://api.deepseek.com/v1)")) },
                                leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_provider_url_input")
                            )
                        }

                        // API Key Field
                        if (keyIsOptional) {
                            Text(
                                text = pick(isAr, "هذا الخادم لا يحتاج مفتاح API؛ سيُرسل الطلب بدون Authorization.", "This provider does not need an API key; requests are sent without Authorization."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = {
                                apiKeyInput = it
                                aiSavedBanner = false
                            },
                            label = {
                                Text(
                                    pick(
                                        isAr,
                                        "مفتاح الـ API (${if (aiConfig.aiProvider == AiProviderType.CUSTOM) customNameInput.ifBlank { "Custom" } else aiConfig.aiProvider.displayNameEn})",
                                        "API Key (${if (aiConfig.aiProvider == AiProviderType.CUSTOM) customNameInput.ifBlank { "Custom" } else aiConfig.aiProvider.displayNameEn})"
                                    )
                                )
                            },
                            placeholder = { Text("sk-... / AIza...") },
                            leadingIcon = { Icon(Icons.Rounded.Key, contentDescription = null) },
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = {
                                        clipboardManager.getText()?.text?.let { pasted ->
                                            apiKeyInput = pasted.trim()
                                        }
                                    }) {
                                        Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste API Key")
                                    }
                                    IconButton(onClick = { showAiKey = !showAiKey }) {
                                        Icon(
                                            imageVector = if (showAiKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = "Toggle Key Visibility"
                                        )
                                    }
                                }
                            },
                            visualTransformation = if (showAiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_api_key_input")
                        )

                        // Model ID Field + Fetch Available Models Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = modelInput,
                                enabled = aiConfig.aiProvider != AiProviderType.EXAM1,
                                onValueChange = {
                                    modelInput = it
                                    aiSavedBanner = false
                                },
                                label = { Text(pick(isAr, "اسم النموذج (Model ID)", "Model ID")) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_model_input")
                            )

                            FilledTonalButton(
                                onClick = {
                                    when {
                                        aiConfig.aiProvider == AiProviderType.GROKCLOUD -> {
                                            modelInput = "grok-4.7"
                                            aiSavedBanner = false
                                        }
                                        aiConfig.aiProvider == AiProviderType.EXAM1 ||
                                            (aiConfig.aiProvider == AiProviderType.CUSTOM && keyIsOptional) -> {
                                            modelInput = AiProviderType.EXAM1.defaultModel
                                            aiSavedBanner = false
                                        }
                                    }
                                    onFetchAvailableModels(
                                        aiConfig.aiProvider,
                                        apiKeyInput,
                                        customBaseUrlInput
                                    )
                                },
                                enabled = fetchedModelsState !is FetchedModelsState.Loading,
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                                modifier = Modifier.testTag("fetch_models_button")
                            ) {
                                if (fetchedModelsState is FetchedModelsState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(pick(isAr, "جلب النماذج", "Fetch Models"), style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Fetched Models Result OR Suggested Models Chips
                        if (fetchedModelsState is FetchedModelsState.Error) {
                            StatusFeedbackPill(isSuccess = false, text = fetchedModelsState.message)
                        }
                        if (fetchedModelsState is FetchedModelsState.Info) {
                            StatusFeedbackPill(isSuccess = true, text = fetchedModelsState.message)
                        }

                        val displayModels = if (fetchedModels.isNotEmpty()) {
                            fetchedModels
                        } else {
                            aiConfig.aiProvider.suggestedModels
                        }

                        if (fetchedModels.isNotEmpty()) {
                            Text(
                                text = pick(
                                    isAr,
                                    "النماذج المتاحة من المزود (${fetchedModels.size}):",
                                    "Available Models from Provider (${fetchedModels.size}):"
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            displayModels.take(24).forEach { modelOption ->
                                FilterChip(
                                    selected = modelInput == modelOption,
                                    onClick = {
                                        modelInput = modelOption
                                        aiSavedBanner = false
                                    },
                                    label = { Text(modelOption, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        // Save (either built-in or Save into Custom Providers list) & Test Model Connection Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (aiConfig.aiProvider == AiProviderType.CUSTOM) {
                                        onSaveAndActivateCustomProvider(
                                            editingCustomId,
                                            customNameInput.ifBlank { "Custom API" },
                                            customBaseUrlInput,
                                            apiKeyInput,
                                            modelInput,
                                            fetchedModels
                                        )
                                    } else {
                                        onSaveAiCredentials(
                                            aiConfig.aiProvider,
                                            apiKeyInput,
                                            modelInput,
                                            null
                                        )
                                    }
                                    aiSavedBanner = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_ai_button")
                            ) {
                                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (aiConfig.aiProvider == AiProviderType.CUSTOM)
                                        pick(isAr, "حفظ كمزود بالقائمة", "Save Provider")
                                    else pick(isAr, "حفظ الإعدادات", "Save")
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    onTestAiConnection(
                                        aiConfig.aiProvider,
                                        apiKeyInput,
                                        modelInput,
                                        if (aiConfig.aiProvider == AiProviderType.CUSTOM) customBaseUrlInput else null
                                    )
                                },
                                enabled = aiTestState !is ConnectionTestState.Testing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_ai_button")
                            ) {
                                if (aiTestState is ConnectionTestState.Testing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(pick(isAr, "اختبار اتصال النموذج", "Test Model"))
                            }
                        }

                        if (aiSavedBanner) {
                            StatusFeedbackPill(
                                isSuccess = true,
                                text = if (aiConfig.aiProvider == AiProviderType.CUSTOM)
                                    pick(isAr, "تم حفظ المزود المخصص في قائمتك وتفعيله بنجاح.", "Custom provider saved to your list and activated.")
                                else pick(isAr, "تم حفظ بيانات المزود بنجاح على الجهاز.", "Provider credentials saved locally.")
                            )
                        }

                        ConnectionResultView(isAr = isAr, state = aiTestState)
                    }
                }

                // Section 2: Dedicated Translation API Integration
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Column {
                                Text(
                                    text = pick(isAr, "واجهات برمجة الترجمة (Translation APIs)", "Translation API Integration"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = pick(
                                        isAr,
                                        "استخدم مزود الذكاء الاصطناعي أو اربط Google Translate / DeepL / LibreTranslate.",
                                        "Use your active AI model or connect Google Cloud Translate, DeepL, or LibreTranslate."
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TranslationProviderType.entries.forEach { tp ->
                                FilterChip(
                                    selected = transProvider == tp,
                                    onClick = {
                                        transProvider = tp
                                        if (tp.defaultEndpoint.isNotBlank()) {
                                            transEndpoint = tp.defaultEndpoint
                                        }
                                        transSavedBanner = false
                                    },
                                    label = {
                                        Text(
                                            text = if (isAr) tp.displayNameAr else tp.displayNameEn,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                )
                            }
                        }

                        if (transProvider != TranslationProviderType.USE_AI) {
                            OutlinedTextField(
                                value = transApiKey,
                                onValueChange = {
                                    transApiKey = it
                                    transSavedBanner = false
                                },
                                label = {
                                    Text(
                                        if (transProvider.requiresApiKey)
                                            pick(isAr, "مفتاح API الترجمة", "Translation API Key")
                                        else pick(isAr, "مفتاح API (اختياري)", "API Key (Optional)")
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showTransKey = !showTransKey }) {
                                        Icon(
                                            imageVector = if (showTransKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = "Toggle Key"
                                        )
                                    }
                                },
                                visualTransformation = if (showTransKey) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = transEndpoint,
                                onValueChange = {
                                    transEndpoint = it
                                    transSavedBanner = false
                                },
                                label = { Text(pick(isAr, "رابط خدمة الترجمة (Endpoint)", "Translation Endpoint URL")) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Text(
                            text = pick(isAr, "لغة الترجمة الافتراضية:", "Default Target Language:"),
                            style = MaterialTheme.typography.labelLarge
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TargetTranslateLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = transTargetLang == lang,
                                    onClick = {
                                        transTargetLang = lang
                                        transSavedBanner = false
                                    },
                                    label = {
                                        Text(
                                            text = "${lang.flagEmoji} ${if (isAr) lang.labelAr else lang.labelEn}",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSaveTranslationConfig(transProvider, transApiKey, transEndpoint, transTargetLang)
                                    transSavedBanner = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(pick(isAr, "حفظ الترجمة", "Save"))
                            }

                            OutlinedButton(
                                onClick = {
                                    onTestTranslationConnection(transProvider, transApiKey, transEndpoint, transTargetLang)
                                },
                                enabled = translationTestState !is ConnectionTestState.Testing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (translationTestState is ConnectionTestState.Testing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(pick(isAr, "اختبار الترجمة", "Test"))
                            }
                        }

                        if (transSavedBanner) {
                            StatusFeedbackPill(
                                isSuccess = true,
                                text = pick(isAr, "تم حفظ إعدادات الترجمة.", "Translation settings saved.")
                            )
                        }

                        ConnectionResultView(isAr = isAr, state = translationTestState)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionResultView(isAr: Boolean, state: ConnectionTestState) {
    when (state) {
        ConnectionTestState.Idle, ConnectionTestState.Testing -> Unit
        is ConnectionTestState.Success -> {
            StatusFeedbackPill(
                isSuccess = true,
                text = pick(
                    isAr,
                    "نجح الاتصال (${state.latencyMs} مللي ثانية): ${state.reply}",
                    "Connected (${state.latencyMs}ms): ${state.reply}"
                )
            )
        }
        is ConnectionTestState.Error -> {
            StatusFeedbackPill(isSuccess = false, text = state.message)
        }
    }
}

@Composable
private fun StatusFeedbackPill(isSuccess: Boolean, text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSuccess) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error
            )
        }
    }
}
