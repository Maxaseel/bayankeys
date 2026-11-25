package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.CustomBotItem
import com.example.ui.AppStrings.pick
import com.example.ui.BayanViewModel
import com.example.ui.components.ProvideSafeTextToolbar
import com.example.ui.screens.AiSettingsScreen
import com.example.ui.screens.BotAssistantHubScreen
import com.example.ui.screens.ClipboardDictionaryScreen
import com.example.ui.screens.CreateOrEditBotScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PasswordManagerScreen
import com.example.ui.screens.ThemeStudioScreen
import com.example.ui.theme.BayanTheme

private object Routes {
    const val HOME = "home"
    const val AI_API = "ai_api"
    const val THEME = "theme"
    const val CLIPBOARD = "clipboard"
    const val PASSWORDS = "passwords"
    const val BOTS = "bots"
    const val CREATE_BOT = "create_bot"
}

private data class NavDestination(
    val route: String,
    val labelAr: String,
    val labelEn: String,
    val icon: ImageVector
)

private val destinations = listOf(
    NavDestination(Routes.HOME, "الرئيسية", "Home", Icons.Rounded.Home),
    NavDestination(Routes.AI_API, "الذكاء", "AI & API", Icons.Rounded.AutoAwesome),
    NavDestination(Routes.THEME, "المظهر", "Themes", Icons.Rounded.Palette),
    NavDestination(Routes.CLIPBOARD, "الحافظة", "Clipboard", Icons.Rounded.ContentPaste),
    NavDestination(Routes.PASSWORDS, "المرور", "Passwords", Icons.Rounded.VpnKey)
)

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_REQUEST_MIC = "extra_request_mic"
        const val EXTRA_OPEN_PASSWORDS = "extra_open_passwords"
        const val EXTRA_OPEN_BOTS = "extra_open_bots"
        const val EXTRA_CREATE_BOT = "extra_create_bot"
    }

    private val viewModel: BayanViewModel by viewModels {
        BayanViewModel.Factory(application as BayanApplication)
    }

    private val pendingMicRequest = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val pendingOpenPasswords = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val pendingOpenBots = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val pendingCreateBot = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val micGrantedState = kotlinx.coroutines.flow.MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.refreshImeStatus(this)
        refreshMicStatus()
        consumeIntentExtras(intent)

        setContent {
            val appUiLang by viewModel.appUiLanguage.collectAsStateWithLifecycle()
            val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
            val pendingMic by pendingMicRequest.collectAsStateWithLifecycle()
            val openPasswords by pendingOpenPasswords.collectAsStateWithLifecycle()
            val openBots by pendingOpenBots.collectAsStateWithLifecycle()
            val createBot by pendingCreateBot.collectAsStateWithLifecycle()
            val micGranted by micGrantedState.collectAsStateWithLifecycle()
            val isAr = appUiLang == "ar"

            BayanTheme(
                themeMode = themeConfig.themeMode,
                customAccentColor = Color(themeConfig.accentArgb),
                fontStyle = themeConfig.fontStyle
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    ProvideSafeTextToolbar {
                        BayanAppShell(
                            isAr = isAr,
                            viewModel = viewModel,
                            micPermissionGranted = micGranted,
                            pendingMicRequest = pendingMic,
                            pendingOpenPasswords = openPasswords,
                            pendingOpenBots = openBots,
                            pendingCreateBot = createBot,
                            onMicRequestHandled = { granted ->
                                pendingMicRequest.value = false
                                micGrantedState.value = granted
                            },
                            onOpenPasswordsHandled = {
                                pendingOpenPasswords.value = false
                            },
                            onOpenBotsHandled = {
                                pendingOpenBots.value = false
                            },
                            onCreateBotHandled = {
                                pendingCreateBot.value = false
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeIntentExtras(intent)
    }

    private fun consumeIntentExtras(intent: Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra(EXTRA_REQUEST_MIC, false)) {
            pendingMicRequest.value = true
            intent.removeExtra(EXTRA_REQUEST_MIC)
        }
        if (intent.getBooleanExtra(EXTRA_OPEN_PASSWORDS, false)) {
            pendingOpenPasswords.value = true
            intent.removeExtra(EXTRA_OPEN_PASSWORDS)
        }
        if (intent.getBooleanExtra(EXTRA_OPEN_BOTS, false)) {
            pendingOpenBots.value = true
            intent.removeExtra(EXTRA_OPEN_BOTS)
        }
        if (intent.getBooleanExtra(EXTRA_CREATE_BOT, false)) {
            pendingCreateBot.value = true
            intent.removeExtra(EXTRA_CREATE_BOT)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshImeStatus(this)
        refreshMicStatus()
    }

    private fun refreshMicStatus() {
        micGrantedState.value = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BayanAppShell(
    isAr: Boolean,
    viewModel: BayanViewModel,
    micPermissionGranted: Boolean,
    pendingMicRequest: Boolean,
    pendingOpenPasswords: Boolean,
    pendingOpenBots: Boolean,
    pendingCreateBot: Boolean,
    onMicRequestHandled: (Boolean) -> Unit,
    onOpenPasswordsHandled: () -> Unit,
    onOpenBotsHandled: () -> Unit,
    onCreateBotHandled: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME
    var editingBot by remember { mutableStateOf<CustomBotItem?>(null) }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onMicRequestHandled(granted)
    }

    LaunchedEffect(pendingMicRequest) {
        if (pendingMicRequest) {
            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val imeStatus by viewModel.imeStatus.collectAsStateWithLifecycle()
    val aiConfig by viewModel.aiConfig.collectAsStateWithLifecycle()
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val sizeConfig by viewModel.sizeConfig.collectAsStateWithLifecycle()
    val toolbarConfig by viewModel.toolbarVisibility.collectAsStateWithLifecycle()
    val gradualPasteConfig by viewModel.gradualPasteConfig.collectAsStateWithLifecycle()
    val enabledLanguages by viewModel.enabledLanguages.collectAsStateWithLifecycle()
    val clipboardItems by viewModel.clipboardItems.collectAsStateWithLifecycle()
    val passwordEntries by viewModel.passwordEntries.collectAsStateWithLifecycle()
    val customBots by viewModel.customBots.collectAsStateWithLifecycle()
    val botTestBusy by viewModel.botTestBusy.collectAsStateWithLifecycle()
    val botTestResult by viewModel.botTestResult.collectAsStateWithLifecycle()
    val learnedWords by viewModel.learnedWords.collectAsStateWithLifecycle()
    val learnedWordsCount by viewModel.learnedWordsCount.collectAsStateWithLifecycle()
    val playgroundText by viewModel.playgroundText.collectAsStateWithLifecycle()
    val playgroundDiffReview by viewModel.playgroundDiffReview.collectAsStateWithLifecycle()
    val playgroundUndo by viewModel.playgroundUndoText.collectAsStateWithLifecycle()
    val playgroundBusy by viewModel.playgroundBusy.collectAsStateWithLifecycle()
    val playgroundStatus by viewModel.playgroundStatus.collectAsStateWithLifecycle()
    val aiTestState by viewModel.aiTestState.collectAsStateWithLifecycle()
    val translationTestState by viewModel.translationTestState.collectAsStateWithLifecycle()
    val customProviders by viewModel.customProviders.collectAsStateWithLifecycle()
    val fetchedModelsState by viewModel.fetchedModelsState.collectAsStateWithLifecycle()
    val previewState by viewModel.previewKeyboardState.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val textToolbar = LocalTextToolbar.current

    val navigateTo: (String) -> Unit = { route ->
        textToolbar.hide()
        focusManager.clearFocus(force = true)
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(pendingOpenPasswords) {
        if (pendingOpenPasswords) {
            navigateTo(Routes.PASSWORDS)
            onOpenPasswordsHandled()
        }
    }
    LaunchedEffect(pendingOpenBots) {
        if (pendingOpenBots) {
            navController.navigate(Routes.BOTS) { launchSingleTop = true }
            onOpenBotsHandled()
        }
    }
    LaunchedEffect(pendingCreateBot) {
        if (pendingCreateBot) {
            editingBot = null
            navController.navigate(Routes.CREATE_BOT) { launchSingleTop = true }
            onCreateBotHandled()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 700.dp
        val density = LocalDensity.current
        val isKeyboardOpen = WindowInsets.ime.getBottom(density) > WindowInsets.navigationBars.getBottom(density)

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = pick(isAr, "لوحة بيان الذكية", "Bayan Keys"),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        Surface(
                            onClick = {
                                textToolbar.hide()
                                focusManager.clearFocus(force = true)
                                viewModel.toggleAppUiLanguage()
                            },
                            shape = RoundedCornerShape(99.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .testTag("ui_language_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Translate,
                                    contentDescription = "Switch UI Language",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(0.dp)
                                )
                                Text(
                                    text = if (isAr) "EN" else "عربي",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (!useNavRail && !isKeyboardOpen) {
                    NavigationBar {
                        destinations.forEach { dest ->
                            NavigationBarItem(
                                selected = currentRoute == dest.route,
                                onClick = { navigateTo(dest.route) },
                                icon = { Icon(dest.icon, contentDescription = null) },
                                label = {
                                    Text(
                                        text = if (isAr) dest.labelAr else dest.labelEn,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
            ) {
                if (useNavRail) {
                    NavigationRail {
                        destinations.forEach { dest ->
                            NavigationRailItem(
                                selected = currentRoute == dest.route,
                                onClick = { navigateTo(dest.route) },
                                icon = { Icon(dest.icon, contentDescription = null) },
                                label = {
                                    Text(
                                        text = if (isAr) dest.labelAr else dest.labelEn,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("rail_${dest.route}")
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.weight(1f).fillMaxSize()
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            isAr = isAr,
                            imeStatus = imeStatus,
                            micPermissionGranted = micPermissionGranted,
                            aiConfig = aiConfig,
                            customBots = customBots,
                            learnedWordsCount = learnedWordsCount,
                            pinnedClipsCount = clipboardItems.count { it.isPinned },
                            playgroundText = playgroundText,
                            playgroundDiffReview = playgroundDiffReview,
                            canUndoPlayground = playgroundUndo != null,
                            playgroundBusy = playgroundBusy,
                            playgroundStatus = playgroundStatus,
                            onPlaygroundTextChange = viewModel::updatePlaygroundText,
                            onProofreadPlayground = viewModel::proofreadPlayground,
                            onApplyPlaygroundDiff = viewModel::applyPlaygroundDiff,
                            onApplyPlaygroundDiffGradually = viewModel::applyPlaygroundDiffGradually,
                            onCopyPlaygroundDiff = viewModel::onPlaygroundDiffCopied,
                            onDismissPlaygroundDiff = viewModel::dismissPlaygroundDiff,
                            onTranslatePlayground = viewModel::translatePlayground,
                            onUndoPlayground = viewModel::undoPlayground,
                            onRunCustomBotPlayground = viewModel::runCustomBotPlayground,
                            onOpenBotsHub = { navController.navigate(Routes.BOTS) },
                            onRequestMicPermission = {
                                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                            onOpenAiSettings = { navigateTo(Routes.AI_API) }
                        )
                    }
                    composable(Routes.AI_API) {
                        AiSettingsScreen(
                            isAr = isAr,
                            aiConfig = aiConfig,
                            customProviders = customProviders,
                            aiTestState = aiTestState,
                            translationTestState = translationTestState,
                            fetchedModelsState = fetchedModelsState,
                            onSelectAiProvider = viewModel::selectAiProvider,
                            onSaveAiCredentials = viewModel::saveAiCredentials,
                            onTestAiConnection = viewModel::testAiConnection,
                            onFetchAvailableModels = viewModel::fetchModels,
                            onResetFetchedModels = viewModel::resetFetchedModelsState,
                            onSaveAndActivateCustomProvider = viewModel::saveAndActivateCustomProvider,
                            onActivateSavedCustomProvider = viewModel::activateSavedCustomProvider,
                            onDeleteCustomProvider = viewModel::deleteCustomProvider,
                            onSaveTranslationConfig = viewModel::saveTranslationConfig,
                            onTestTranslationConnection = viewModel::testTranslationConnection
                        )
                    }
                    composable(Routes.THEME) {
                        ThemeStudioScreen(
                            isAr = isAr,
                            themeConfig = themeConfig,
                            sizeConfig = sizeConfig,
                            toolbarConfig = toolbarConfig,
                            gradualPasteConfig = gradualPasteConfig,
                            enabledLanguages = enabledLanguages,
                            previewState = previewState,
                            clipboardItems = clipboardItems,
                            passwordEntries = passwordEntries,
                            onSetThemeMode = viewModel::setThemeMode,
                            onApplyPreset = viewModel::applyThemePreset,
                            onUpdateCustomColors = viewModel::updateCustomColors,
                            onUpdateSizeConfig = viewModel::updateSizeConfig,
                            onUpdateToolbarConfig = viewModel::updateToolbarVisibility,
                            onSetKeyShape = viewModel::setKeyShape,
                            onSetFontStyle = viewModel::setKeyboardFontStyle,
                            onUpdateGradualPasteConfig = viewModel::updateGradualPasteConfig,
                            onToggleLanguage = viewModel::toggleLanguageEnabled,
                            onPreviewSwitchLanguage = viewModel::onPreviewSwitchLanguage,
                            onPreviewShift = viewModel::onPreviewShift,
                            onPreviewSymbols = viewModel::onPreviewSymbols,
                            onPreviewPanel = viewModel::onPreviewPanel
                        )
                    }
                    composable(Routes.CLIPBOARD) {
                        ClipboardDictionaryScreen(
                            isAr = isAr,
                            clipboardItems = clipboardItems,
                            learnedWords = learnedWords,
                            onAddManualClip = viewModel::addManualClip,
                            onTogglePinClip = viewModel::togglePinClipboard,
                            onDeleteClip = viewModel::deleteClipboard,
                            onClearUnpinned = viewModel::clearUnpinnedClipboard,
                            onAddCustomWord = viewModel::addCustomWord,
                            onDeleteLearnedWord = viewModel::deleteLearnedWord,
                            onResetDictionary = viewModel::resetLearnedDictionary
                        )
                    }
                    composable(Routes.PASSWORDS) {
                        PasswordManagerScreen(
                            isAr = isAr,
                            entries = passwordEntries,
                            onSaveEntry = { id, site, url, user, pass, brand ->
                                viewModel.savePasswordEntry(id, site, url, user, pass, brand)
                            },
                            onSaveEntryWithIcon = { id, site, url, user, pass, brand, localIconPath ->
                                viewModel.savePasswordEntry(id, site, url, user, pass, brand, localIconPath)
                            },
                            onDeleteEntry = viewModel::deletePasswordEntry
                        )
                    }
                    composable(Routes.BOTS) {
                        BotAssistantHubScreen(
                            isAr = isAr,
                            customBots = customBots,
                            botTestBusy = botTestBusy,
                            botTestResult = botTestResult,
                            onBack = { navController.popBackStack() },
                            onCreateBotClick = {
                                editingBot = null
                                navController.navigate(Routes.CREATE_BOT)
                            },
                            onEditBotClick = { bot ->
                                editingBot = bot
                                navController.navigate(Routes.CREATE_BOT)
                            },
                            onTogglePinBot = viewModel::toggleBotPinnedToToolbar,
                            onDeleteBot = viewModel::deleteCustomBot,
                            onTestBot = viewModel::testCustomBot,
                            onClearBotTestResult = viewModel::clearBotTestResult,
                            onRunBuiltinActionInPlayground = { action ->
                                viewModel.runBuiltinActionPlayground(action)
                                navController.popBackStack(Routes.HOME, inclusive = false)
                            }
                        )
                    }
                    composable(Routes.CREATE_BOT) {
                        CreateOrEditBotScreen(
                            isAr = isAr,
                            editingBot = editingBot,
                            onBack = { navController.popBackStack() },
                            onSaveBot = { id, name, prompt, iconOption, accentArgb, sources, pinned ->
                                viewModel.saveCustomBot(
                                    id = id,
                                    name = name,
                                    prompt = prompt,
                                    iconOption = iconOption,
                                    accentArgb = accentArgb,
                                    knowledgeSources = sources,
                                    isPinnedToToolbar = pinned
                                )
                                editingBot = null
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
