package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BayanDatabase
import com.example.data.local.SettingsRepository
import com.example.data.model.BotIconOption
import com.example.data.model.BotKnowledgeSource
import com.example.data.model.BuiltinBotAction
import com.example.data.model.GradualPasteConfig
import com.example.data.model.GradualPasteMode
import com.example.data.model.KeyButtonShape
import com.example.data.model.KeyboardLanguage
import com.example.data.model.KeyboardThemeConfig
import com.example.data.model.NumberRowStyle
import com.example.data.model.OneHandedMode
import com.example.data.model.PasswordServiceBrand
import com.example.data.model.QuickInputTool
import com.example.data.model.ThemeMode
import com.example.data.model.ToolbarAction
import com.example.data.repository.BayanRepository
import com.example.ime.GradualPasteEngine
import com.example.ime.KeyboardLayer
import com.example.ime.KeyboardLayouts
import com.example.util.DiffKind
import com.example.util.WordDiffUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: BayanDatabase
    private lateinit var repository: BayanRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, BayanDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val settings = SettingsRepository(context)
        repository = BayanRepository(db.bayanDao(), settings)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `app name matches Bayan Keys`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Bayan Keys", appName)
    }

    @Test
    fun `learns typed words and surfaces them in suggestions`() = runBlocking {
        repository.ensureSeeded()
        repository.learnCommittedWord(
            rawWord = "بياناتي",
            previousWord = null,
            activeLanguage = KeyboardLanguage.ARABIC,
            enabledLanguages = listOf(KeyboardLanguage.ARABIC, KeyboardLanguage.ENGLISH)
        )
        val suggestions = repository.getSuggestions(
            composingWord = "بيان",
            previousWord = null,
            activeLanguage = KeyboardLanguage.ARABIC,
            enabledLanguages = listOf(KeyboardLanguage.ARABIC, KeyboardLanguage.ENGLISH),
            autoSwitchEnabled = true
        )
        assertTrue(suggestions.any { it.text == "بياناتي" })
    }

    @Test
    fun `pinned clipboard items appear before unpinned items`() = runBlocking {
        repository.addManualClip("Regular clip", pinned = false)
        repository.addManualClip("Important pinned", pinned = true)
        val items = repository.clipboardItems.first()
        assertEquals("Important pinned", items.first().text)
        assertTrue(items.first().isPinned)
    }

    @Test
    fun `cross layout mistype suggests language switch`() = runBlocking {
        repository.ensureSeeded()
        // "اثممخ" corresponds to English keys "hello" on Arabic keyboard
        val suggestions = repository.getSuggestions(
            composingWord = "اثممخ",
            previousWord = null,
            activeLanguage = KeyboardLanguage.ARABIC,
            enabledLanguages = listOf(KeyboardLanguage.ARABIC, KeyboardLanguage.ENGLISH),
            autoSwitchEnabled = true
        )
        assertTrue(suggestions.any { it.text.equals("hello", ignoreCase = true) && it.targetLanguageSwitch == KeyboardLanguage.ENGLISH })
    }

    @Test
    fun `theme config resolves dark and light palettes properly`() {
        val cfg = KeyboardThemeConfig(themeMode = ThemeMode.SYSTEM)
        val dark = cfg.resolvePalette(isSystemDark = true)
        val light = cfg.resolvePalette(isSystemDark = false)
        assertTrue(dark.isDark)
        assertTrue(!light.isDark)
    }

    @Test
    fun `custom AI providers can be saved with fetched models and recalled anytime`() = runBlocking {
        val saved = repository.saveCustomProvider(
            name = "DeepSeek",
            baseUrl = "https://api.deepseek.com/v1",
            apiKey = "sk-test",
            selectedModel = "deepseek-chat",
            fetchedModels = listOf("deepseek-chat", "deepseek-reasoner")
        )
        val list = repository.customProviders.first()
        assertEquals(1, list.size)
        assertEquals("DeepSeek", list.first().name)
        assertEquals(listOf("deepseek-chat", "deepseek-reasoner"), list.first().fetchedModelsList())
        repository.deleteCustomProvider(saved.id)
        assertTrue(repository.customProviders.first().isEmpty())
    }

    @Test
    fun `every target translate language has a country flag emoji`() {
        com.example.data.model.TargetTranslateLanguage.entries.forEach { lang ->
            assertTrue(lang.flagEmoji.isNotBlank())
        }
    }

    @Test
    fun `word diff detects wrong and corrected words accurately`() {
        val diff = WordDiffUtil.computeWordDiff(
            original = "اهلا بكم في لوحه المفاتيح",
            corrected = "أهلاً بكم في لوحة المفاتيح"
        )
        assertTrue(diff.any { it.kind == DiffKind.WRONG_REMOVED && it.text == "اهلا" })
        assertTrue(diff.any { it.kind == DiffKind.WRONG_REMOVED && it.text == "لوحه" })
        assertTrue(diff.any { it.kind == DiffKind.CORRECTED_ADDED && it.text == "أهلاً" })
        assertTrue(diff.any { it.kind == DiffKind.CORRECTED_ADDED && it.text == "لوحة" })
        assertTrue(diff.any { it.kind == DiffKind.UNCHANGED && it.text == "بكم" })
    }

    @Test
    fun `toolbar visibility and order persist across updates`() = runBlocking {
        val settings = SettingsRepository(context)
        settings.updateToolbarVisibility {
            it.withVisibility(ToolbarAction.VOICE, false)
                .withVisibility(ToolbarAction.CLIPBOARD, true)
                .withVisibility(ToolbarAction.PROOFREAD, false)
                .moveAction(ToolbarAction.PROOFREAD, 2)
        }
        val updated = settings.toolbarVisibilityFlow.first()
        assertEquals(false, updated.showVoice)
        assertEquals(true, updated.showClipboard)
        assertEquals(false, updated.showProofread)
        assertEquals(2, updated.order.indexOf(ToolbarAction.PROOFREAD))
        assertTrue(updated.order.contains(ToolbarAction.PASSWORDS))
    }

    @Test
    fun `number row style switches between Arabic and English numerals and hides when disabled`() {
        val arabicRows = KeyboardLayouts.getRowsFor(
            language = KeyboardLanguage.ARABIC,
            layer = KeyboardLayer.LETTERS,
            showNumberRow = true,
            numberRowStyle = NumberRowStyle.ARABIC_EASTERN
        )
        assertEquals("١", arabicRows.first().first().label)
        assertEquals("٠", arabicRows.first().last().label)

        val westernRows = KeyboardLayouts.getRowsFor(
            language = KeyboardLanguage.ARABIC,
            layer = KeyboardLayer.LETTERS,
            showNumberRow = true,
            numberRowStyle = NumberRowStyle.WESTERN
        )
        assertEquals("1", westernRows.first().first().label)
        assertEquals("0", westernRows.first().last().label)

        val hiddenRows = KeyboardLayouts.getRowsFor(
            language = KeyboardLanguage.ARABIC,
            layer = KeyboardLayer.LETTERS,
            showNumberRow = false,
            numberRowStyle = NumberRowStyle.ARABIC_EASTERN
        )
        assertEquals("ض", hiddenRows.first().first().label)
    }

    @Test
    fun `password vault encrypts secret at rest and auto-detects brand`() = runBlocking {
        val savedId = repository.savePasswordEntry(
            siteName = "GitHub Login",
            siteUrl = "https://github.com",
            username = "dev@github.com",
            password = "SuperSecret#Password2026"
        )
        val rawEntities = db.bayanDao().observePasswordEntries().first()
        assertEquals(1, rawEntities.size)
        assertNotEquals("SuperSecret#Password2026", rawEntities.first().encryptedPassword)

        val decrypted = repository.passwordEntries.first()
        assertEquals(1, decrypted.size)
        assertEquals(savedId, decrypted.first().id)
        assertEquals("SuperSecret#Password2026", decrypted.first().password)
        assertEquals(PasswordServiceBrand.GITHUB, decrypted.first().brand)
        assertEquals(
            PasswordServiceBrand.GOOGLE,
            PasswordServiceBrand.detectFrom("weekasset@gmail.com", "accounts.google.com")
        )
        assertEquals(
            PasswordServiceBrand.X_TWITTER,
            PasswordServiceBrand.detectFrom("313_2abdulhaq", "x.com")
        )
    }

    @Test
    fun `safe text toolbar ignores detached view and prevents duplicate ActionMode finish`() {
        var startCount = 0
        var finishCount = 0
        var attached = false
        var focused = false
        var capturedCallback: android.view.ActionMode.Callback? = null

        val fakeMode = object : android.view.ActionMode() {
            override fun setTitle(title: CharSequence?) {}
            override fun setTitle(resId: Int) {}
            override fun setSubtitle(subtitle: CharSequence?) {}
            override fun setSubtitle(resId: Int) {}
            override fun setCustomView(view: android.view.View?) {}
            override fun invalidate() {}
            override fun finish() {
                finishCount++
                capturedCallback?.onDestroyActionMode(this)
            }
            override fun getMenu(): android.view.Menu? = null
            override fun getTitle(): CharSequence? = null
            override fun getSubtitle(): CharSequence? = null
            override fun getCustomView(): android.view.View? = null
            override fun getMenuInflater(): android.view.MenuInflater? = null
        }

        val testView = object : android.view.View(context) {
            override fun isAttachedToWindow(): Boolean = attached
            override fun hasWindowFocus(): Boolean = focused
            override fun startActionMode(callback: android.view.ActionMode.Callback?, type: Int): android.view.ActionMode {
                startCount++
                capturedCallback = callback
                return fakeMode
            }
        }

        val toolbar = com.example.ui.components.SafeAndroidTextToolbar(testView)
        // Detached: should not start floating action mode
        toolbar.showMenu(androidx.compose.ui.geometry.Rect(0f, 0f, 10f, 10f))
        assertEquals(0, startCount)
        assertEquals(androidx.compose.ui.platform.TextToolbarStatus.Hidden, toolbar.status)

        // Attached & focused: starts once, and repeated hide() only calls finish() once
        attached = true
        focused = true
        toolbar.showMenu(androidx.compose.ui.geometry.Rect(0f, 0f, 10f, 10f))
        assertEquals(1, startCount)
        assertEquals(androidx.compose.ui.platform.TextToolbarStatus.Shown, toolbar.status)

        toolbar.hide()
        toolbar.hide()
        assertEquals(1, finishCount)
        assertEquals(androidx.compose.ui.platform.TextToolbarStatus.Hidden, toolbar.status)

        // If DecorView already destroyed the mode, subsequent hide() must not call finish() again
        toolbar.showMenu(androidx.compose.ui.geometry.Rect(0f, 0f, 10f, 10f))
        capturedCallback?.onDestroyActionMode(fakeMode)
        toolbar.hide()
        assertEquals(1, finishCount)
    }

    @Test
    fun `gradual paste timing formulas match expected defaults and scale bounds`() {
        val defaults = GradualPasteConfig()
        assertEquals(false, defaults.enabled)
        assertEquals(GradualPasteMode.CHAR_BY_CHAR, defaults.mode)
        assertEquals(89, defaults.charDelayMs())
        assertEquals(89, defaults.spaceDelayMs())
        assertEquals(79, defaults.arrowDelayMs())
        assertEquals(436, defaults.breathPauseMs())
        assertEquals(126, defaults.startDelayMs())

        val fastest = GradualPasteConfig(charSpeed = 50, wordSpeed = 50, startDelay = 1)
        assertTrue(fastest.charDelayMs() < defaults.charDelayMs())
        assertTrue(fastest.wordDelayMs() < defaults.wordDelayMs())
        assertTrue(fastest.startDelayMs() < defaults.startDelayMs())
    }

    @Test
    fun `key button shape and gradual paste config persist in DataStore`() = runBlocking {
        val settings = SettingsRepository(context)
        settings.setKeyShape(KeyButtonShape.SOFT_CIRCLE)
        settings.updateGradualPasteConfig {
            it.copy(
                enabled = true,
                mode = GradualPasteMode.WORD_BY_WORD,
                charSpeed = 40,
                wordSpeed = 35,
                charsBeforeBreath = 12
            )
        }

        val theme = settings.themeConfigFlow.first()
        val gradual = settings.gradualPasteConfigFlow.first()
        assertEquals(KeyButtonShape.SOFT_CIRCLE, theme.keyShape)
        assertEquals(true, gradual.enabled)
        assertEquals(GradualPasteMode.WORD_BY_WORD, gradual.mode)
        assertEquals(40, gradual.charSpeed)
        assertEquals(35, gradual.wordSpeed)
        assertEquals(12, gradual.charsBeforeBreath)
    }

    @Test
    fun `quick tools toolbar action, numpad layer, and modes persist properly`() = runBlocking {
        val settings = SettingsRepository(context)
        assertTrue(ToolbarAction.defaultOrder().contains(ToolbarAction.QUICK_TOOLS))
        assertEquals(29, QuickInputTool.entries.size)

        val numpadWestern = KeyboardLayouts.getRowsFor(
            language = KeyboardLanguage.ENGLISH,
            layer = KeyboardLayer.NUMPAD,
            showNumberRow = true,
            numberRowStyle = NumberRowStyle.WESTERN
        )
        assertEquals("1", numpadWestern.first().first().label)
        assertEquals(4, numpadWestern.size)

        val numpadArabic = KeyboardLayouts.getRowsFor(
            language = KeyboardLanguage.ARABIC,
            layer = KeyboardLayer.NUMPAD,
            showNumberRow = true,
            numberRowStyle = NumberRowStyle.ARABIC_EASTERN
        )
        assertEquals("١", numpadArabic.first().first().label)

        settings.updateSizeConfig {
            it.copy(
                oneHandedMode = OneHandedMode.LEFT,
                splitKeyboard = true,
                incognitoMode = true,
                autoCorrectEnabled = true
            )
        }
        val size = settings.sizeConfigFlow.first()
        assertEquals(OneHandedMode.LEFT, size.oneHandedMode)
        assertTrue(size.splitKeyboard)
        assertTrue(size.incognitoMode)
        assertTrue(size.autoCorrectEnabled)
    }

    @Test
    fun `gradual paste engine streams exact text in char and word modes`() = runBlocking {
        val sample = "مرحباً بكم في بيان"
        val charCfg = GradualPasteConfig(
            enabled = true,
            mode = GradualPasteMode.CHAR_BY_CHAR,
            charSpeed = 50,
            spaceNewlineRhythm = 50,
            startDelay = 1,
            naturalRhythmPulse = 1
        )
        val charChunks = mutableListOf<String>()
        val charDone = GradualPasteEngine.stream(sample, charCfg) { chunk ->
            charChunks += chunk
            true
        }
        assertTrue(charDone)
        assertEquals(sample, charChunks.joinToString(""))
        assertEquals(sample.length, charChunks.size)

        val wordCfg = charCfg.copy(mode = GradualPasteMode.WORD_BY_WORD, wordSpeed = 50)
        val wordChunks = mutableListOf<String>()
        val wordDone = GradualPasteEngine.stream(sample, wordCfg) { chunk ->
            wordChunks += chunk
            true
        }
        assertTrue(wordDone)
        assertEquals(sample, wordChunks.joinToString(""))
        assertTrue(wordChunks.size < charChunks.size)
    }

    @Test
    fun `custom bots persist with icons, knowledge base, and toolbar pinning`() = runBlocking {
        assertTrue(ToolbarAction.defaultOrder().contains(ToolbarAction.BOTS))
        assertEquals(10, BuiltinBotAction.entries.size)

        val sources = listOf(
            BotKnowledgeSource(
                id = "s1",
                title = "دليل المنتجات",
                content = "لوحة بيان تدعم التصحيح بالذكاء الاصطناعي والترجمة الفورية.",
                sourceType = "text"
            )
        )
        val bot1Id = repository.saveCustomBot(
            name = "مُنشئ محتوى",
            prompt = "اكتب منشورات احترافية جذابة",
            iconOption = BotIconOption.CONTENT_CREATOR,
            accentArgb = BotIconOption.CONTENT_CREATOR.accentArgb,
            knowledgeSources = sources,
            isPinnedToToolbar = true
        )
        val bot2Id = repository.saveCustomBot(
            name = "خبير المبيعات",
            prompt = "اكتب ردوداً مقنعة للعملاء",
            iconOption = BotIconOption.SALES_PRO,
            accentArgb = BotIconOption.SALES_PRO.accentArgb,
            knowledgeSources = emptyList(),
            isPinnedToToolbar = false
        )

        val bots = repository.customBots.first()
        assertEquals(2, bots.size)
        val creator = bots.first { it.id == bot1Id }
        assertEquals("مُنشئ محتوى", creator.name)
        assertEquals(BotIconOption.CONTENT_CREATOR, creator.iconOption)
        assertEquals(1, creator.knowledgeSources.size)
        assertEquals("دليل المنتجات", creator.knowledgeSources.first().title)
        assertTrue(creator.isPinnedToToolbar)

        val sales = bots.first { it.id == bot2Id }
        assertEquals(false, sales.isPinnedToToolbar)
        repository.toggleBotPinnedToToolbar(sales)
        assertTrue(repository.customBots.first().first { it.id == bot2Id }.isPinnedToToolbar)

        repository.deleteCustomBot(bot1Id)
        assertEquals(1, repository.customBots.first().size)
    }
}
