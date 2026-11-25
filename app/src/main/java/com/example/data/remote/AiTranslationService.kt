package com.example.data.remote

import com.example.data.model.AiProviderConfig
import com.example.data.model.AiProviderType
import com.example.data.model.BuiltinBotAction
import com.example.data.model.CustomBotItem
import com.example.data.model.TargetTranslateLanguage
import com.example.data.model.TranslationProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AiTranslationService {

    private companion object {
        const val MAX_RESPONSE_BYTES = 1_048_576L
        const val EXAM1_URL = "https://us-central1-conquer-apps-2ad61.cloudfunctions.net/prod/api.live"
        const val GROKCLOUD_URL = "https://multimodelhandler-746036526161.us-central1.run.app"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun proofreadAndCorrect(
        text: String,
        config: AiProviderConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("No text to proofread"))
        }
        val systemPrompt = """
            You are an expert Arabic, English, and multilingual copyeditor built into a keyboard.
            Fix all spelling, grammar, punctuation, and spacing errors in the user's text while keeping the original language(s), tone, and meaning intact.
            Return ONLY the corrected text itself — no explanations, no quotes, no markdown fences, and no extra commentary.
        """.trimIndent()
        callAiProvider(systemPrompt, text, config)
    }

    suspend fun translateText(
        text: String,
        targetLanguage: TargetTranslateLanguage,
        config: AiProviderConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("No text to translate"))
        }
        when (config.translationProvider) {
            TranslationProviderType.USE_AI -> {
                val systemPrompt = """
                    You are a precise real-time translator. Translate the user's text naturally and accurately into ${targetLanguage.labelEn} (${targetLanguage.labelAr}).
                    Return ONLY the translated text — no explanations, no quotes, and no extra commentary.
                """.trimIndent()
                callAiProvider(systemPrompt, text, config)
            }
            TranslationProviderType.GOOGLE_TRANSLATE -> callGoogleTranslate(text, targetLanguage, config)
            TranslationProviderType.DEEPL -> callDeepL(text, targetLanguage, config)
            TranslationProviderType.LIBRE_TRANSLATE -> callLibreTranslate(text, targetLanguage, config)
        }
    }

    suspend fun runCustomBot(
        text: String,
        bot: CustomBotItem,
        config: AiProviderConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        if (bot.prompt.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Bot prompt is empty"))
        }
        val knowledgeSection = if (bot.knowledgeSources.isNotEmpty()) {
            val joined = bot.knowledgeSources.joinToString("\n\n") { src ->
                val clipped = src.content.trim().take(6000)
                "### ${src.title}\n$clipped"
            }.take(16000)
            "\n\nKnowledge Base (always ground your response in these documents when relevant):\n$joined"
        } else {
            ""
        }
        val systemPrompt = """
            You are "${bot.name}", a custom AI assistant built into a smart keyboard.
            Follow the user's custom bot instructions precisely:
            ${bot.prompt.trim()}$knowledgeSection

            Output rules:
            - Respond directly with the ready-to-insert text only.
            - Match the language of the input or instructions naturally.
            - Do not wrap the output in markdown code fences or meta explanations unless asked.
        """.trimIndent()
        val userPrompt = if (text.isNotBlank()) {
            text
        } else {
            "Execute your role and instructions directly to generate the requested output."
        }
        callAiProvider(systemPrompt, userPrompt, config)
    }

    suspend fun runQuickBotAction(
        text: String,
        action: BuiltinBotAction,
        targetLanguage: TargetTranslateLanguage,
        config: AiProviderConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        if (action == BuiltinBotAction.TRANSLATE) {
            return@withContext translateText(text, targetLanguage, config)
        }
        if (action == BuiltinBotAction.PROOFREAD) {
            return@withContext proofreadAndCorrect(text, config)
        }
        if (text.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("اكتب نصاً أولاً لتنفيذ الأداة"))
        }
        val instruction = when (action) {
            BuiltinBotAction.GRAMMAR ->
                "Fix all grammatical, syntactic, and punctuation errors in the user's text while keeping the exact language, meaning, and wording intact where correct. Return ONLY the corrected text."
            BuiltinBotAction.REPHRASE ->
                "Rephrase and rewrite the user's text so it is clearer, smoother, and more natural while preserving its original language and core meaning. Return ONLY the rephrased text."
            BuiltinBotAction.CHANGE_TONE ->
                "Rewrite the user's text with a polished, courteous, and professional tone while preserving the original language and meaning. Return ONLY the rewritten text."
            BuiltinBotAction.ENHANCE_WORDS ->
                "Enhance the vocabulary, phrasing, and impact of the user's text in the same language without changing its intent. Return ONLY the enhanced text."
            BuiltinBotAction.ASK_AI ->
                "Answer the user's question or fulfill their request clearly, concisely, and directly in the same language so it can be pasted straight into a message or document. Return ONLY the response text."
            BuiltinBotAction.TO_EMOJI ->
                "Convert and enrich the user's text with expressive, relevant emojis while keeping the message clear and readable. Return ONLY the emoji-enriched text."
            BuiltinBotAction.REPLY ->
                "Write a helpful, polite, natural reply to the message provided by the user in the same language. Return ONLY the reply text."
            BuiltinBotAction.CONTINUE_WRITING ->
                "Seamlessly continue writing after the user's text in the exact same language, style, and flow. Return the FULL text starting with the exact original text followed immediately by your continuation."
            BuiltinBotAction.TRANSLATE, BuiltinBotAction.PROOFREAD -> ""
        }
        callAiProvider(instruction, text, config)
    }

    suspend fun testAiConnection(config: AiProviderConfig): Result<Pair<String, Long>> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val result = when {
            isExam1Config(config) -> testExam1Credential(resolveExam1Url(config))
            config.aiProvider == AiProviderType.GROKCLOUD -> testGrokCloudCredential()
            else -> callAiProvider("Reply briefly.", "Reply with only: OK", config)
        }
        val elapsed = System.currentTimeMillis() - start
        result.map { it.trim() to elapsed }
    }

    suspend fun testTranslationConnection(config: AiProviderConfig): Result<Pair<String, Long>> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val result = translateText("Hello, how are you?", TargetTranslateLanguage.ARABIC, config)
        val elapsed = System.currentTimeMillis() - start
        result.map { it.trim() to elapsed }
    }

    suspend fun fetchAvailableModels(
        provider: AiProviderType,
        apiKey: String,
        customBaseUrl: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank() && !provider.isApiKeyOptional(customBaseUrl)) {
            return@withContext Result.failure(IllegalStateException("Enter an API key first to fetch available models."))
        }
        return@withContext try {
            when (provider) {
                AiProviderType.EXAM1 -> Result.failure(IllegalStateException("exam1 لا يدعم جلب النماذج تلقائياً؛ سيُستخدم النموذج الافتراضي ${provider.defaultModel}."))
                AiProviderType.GROKCLOUD -> Result.success(provider.suggestedModels)
                AiProviderType.GEMINI -> fetchGeminiModels(cleanKey)
                AiProviderType.ANTHROPIC -> fetchAnthropicModels(cleanKey)
                AiProviderType.OPENAI,
                AiProviderType.GROQ,
                AiProviderType.OPENROUTER,
                AiProviderType.CUSTOM -> {
                    if (provider == AiProviderType.CUSTOM && provider.isApiKeyOptional(customBaseUrl)) {
                        return@withContext Result.failure(IllegalStateException("المزوّد المخصص api.live لا يدعم جلب النماذج تلقائياً؛ سيُستخدم النموذج الافتراضي ${AiProviderType.EXAM1.defaultModel}."))
                    }
                    val base = if (provider == AiProviderType.CUSTOM) {
                        customBaseUrl.trim().ifBlank { provider.defaultBaseUrl }
                    } else {
                        provider.defaultBaseUrl
                    }
                    fetchOpenAiCompatibleModels(base, cleanKey)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchGeminiModels(apiKey: String): Result<List<String>> {
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$encodedKey"
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException(extractErrorMessage(raw, response.code)))
            }
            val arr = JSONObject(raw).optJSONArray("models") ?: JSONArray()
            val models = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val methods = obj.optJSONArray("supportedGenerationMethods")
                val supportsGenerate = (0 until (methods?.length() ?: 0)).any {
                    methods?.optString(it) == "generateContent"
                }
                if (supportsGenerate) {
                    val name = obj.optString("name").removePrefix("models/").trim()
                    if (name.isNotBlank()) models.add(name)
                }
            }
            return if (models.isNotEmpty()) Result.success(models.distinct())
            else Result.failure(IllegalStateException("No compatible models found"))
        }
    }

    private fun fetchAnthropicModels(apiKey: String): Result<List<String>> {
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/models")
            .get()
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException(extractErrorMessage(raw, response.code)))
            }
            val arr = JSONObject(raw).optJSONArray("data") ?: JSONArray()
            val models = (0 until arr.length()).mapNotNull { idx ->
                arr.optJSONObject(idx)?.optString("id")?.trim()?.takeIf { it.isNotBlank() }
            }.distinct()
            return if (models.isNotEmpty()) Result.success(models)
            else Result.failure(IllegalStateException("No models returned"))
        }
    }

    private fun fetchOpenAiCompatibleModels(endpointOrBase: String, apiKey: String): Result<List<String>> {
        val modelsUrl = resolveOpenAiModelsUrl(endpointOrBase)
        val builder = Request.Builder()
            .url(modelsUrl)
            .get()
            .header("Accept", "application/json")
        if (apiKey.isNotBlank()) {
            builder.header("Authorization", "Bearer $apiKey")
        }
        client.newCall(builder.build()).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException(extractErrorMessage(raw, response.code)))
            }
            val json = JSONObject(raw)
            // Support OpenAI `{ data: [{ id }] }` as well as Ollama `{ models: [{ name }] }`
            val dataArr = json.optJSONArray("data")
            val modelsArr = json.optJSONArray("models")
            val models = buildList {
                if (dataArr != null) {
                    for (i in 0 until dataArr.length()) {
                        dataArr.optJSONObject(i)?.optString("id")?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
                    }
                }
                if (modelsArr != null) {
                    for (i in 0 until modelsArr.length()) {
                        val obj = modelsArr.optJSONObject(i) ?: continue
                        val id = obj.optString("id").ifBlank { obj.optString("name") }.trim()
                        if (id.isNotBlank()) add(id)
                    }
                }
            }.distinct()
            return if (models.isNotEmpty()) Result.success(models)
            else Result.failure(IllegalStateException("No models returned from $modelsUrl"))
        }
    }

    private fun resolveOpenAiModelsUrl(input: String): String {
        val trimmed = input.trim().trimEnd('/')
        return when {
            trimmed.endsWith("/models") -> trimmed
            trimmed.endsWith("/chat/completions") -> trimmed.removeSuffix("/chat/completions") + "/models"
            trimmed.endsWith("/completions") -> trimmed.removeSuffix("/completions") + "/models"
            trimmed.endsWith("/v1") || trimmed.endsWith("/v1beta") -> "$trimmed/models"
            else -> "$trimmed/v1/models"
        }
    }

    private fun resolveOpenAiChatUrl(input: String): String {
        val trimmed = input.trim().trimEnd('/')
        return when {
            trimmed.endsWith("/chat/completions") -> trimmed
            trimmed.endsWith("/v1") || trimmed.endsWith("/v1beta") -> "$trimmed/chat/completions"
            trimmed.endsWith("/models") -> trimmed.removeSuffix("/models") + "/chat/completions"
            else -> "$trimmed/v1/chat/completions"
        }
    }

    private fun callAiProvider(
        systemPrompt: String,
        userPrompt: String,
        config: AiProviderConfig
    ): Result<String> {
        val configuredKey = config.keyFor()
        val apiKey = if (configuredKey == "no-key-needed") "" else configuredKey.trim()
        if (apiKey.isBlank() && !config.aiProvider.isApiKeyOptional(config.aiCustomBaseUrl)) {
            return Result.failure(IllegalStateException("Missing API Key for ${config.activeDisplayName}. Open Bayan Keys settings to add it."))
        }
        val model = config.aiModel.trim().ifBlank { config.aiProvider.defaultModel }

        return try {
            when (config.aiProvider) {
                AiProviderType.EXAM1 -> callExam1(resolveExam1Url(config), userPrompt, config.stream)
                AiProviderType.GROKCLOUD -> callGrokCloud(model, systemPrompt, userPrompt, config.stream)
                AiProviderType.GEMINI -> callGemini(apiKey, model, systemPrompt, userPrompt)
                AiProviderType.ANTHROPIC -> callAnthropic(apiKey, model, systemPrompt, userPrompt)
                AiProviderType.OPENAI,
                AiProviderType.GROQ,
                AiProviderType.OPENROUTER,
                AiProviderType.CUSTOM -> {
                    val endpoint = if (config.aiProvider == AiProviderType.CUSTOM) {
                        config.aiCustomBaseUrl.trim().ifBlank { config.aiProvider.defaultBaseUrl }
                    } else {
                        config.aiProvider.defaultBaseUrl
                    }
                    if (config.aiProvider.isApiKeyOptional(endpoint)) callExam1(endpoint, userPrompt, config.stream)
                    else callOpenAiCompatible(endpoint, apiKey, model, systemPrompt, userPrompt)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isExam1Config(config: AiProviderConfig): Boolean =
        config.aiProvider == AiProviderType.EXAM1 ||
            (config.aiProvider == AiProviderType.CUSTOM && config.aiCustomBaseUrl.trimEnd('/').endsWith("api.live", ignoreCase = true))

    private fun resolveExam1Url(config: AiProviderConfig): String =
        if (config.aiProvider == AiProviderType.CUSTOM) config.aiCustomBaseUrl.trim() else EXAM1_URL

    private fun callExam1(endpoint: String, prompt: String, stream: Boolean = false): Result<String> {
        if (prompt.isBlank()) return Result.failure(IllegalArgumentException("قائمة الرسائل فارغة أو محتواها الأخير فارغ"))
        return try {
            val request = Request.Builder()
                .url(endpoint)
                .post(JSONObject().put("prompt", prompt).toString().toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json; charset=utf-8")
                .build()
            client.newCall(request).execute().use { response ->
                val raw = readLimitedBody(response)
                if (response.code !in 200..299) {
                    return Result.failure(IllegalStateException("exam1 HTTP ${response.code}: ${extractErrorMessage(raw, response.code)}"))
                }
                val json = JSONObject(raw)
                // Keep the api.live payload in the requested data envelope before adapting it
                // to the keyboard's text-oriented result contract.
                val wrapped = JSONObject().put("data", json)
                val responsePayload = wrapped.optJSONObject("data") ?: json
                val choices = responsePayload.optJSONArray("choices")
                val text = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content").orEmpty().trim()
                    .ifBlank { responsePayload.optString("content").trim() }
                if (text.isBlank()) return Result.failure(IllegalStateException("exam1 أعاد نصاً فارغاً"))
                Result.success(if (stream) toSse(text) else text)
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("تعذر الاتصال بخادم exam1: ${e.message ?: "خطأ غير معروف"}", e))
        }
    }

    private fun testExam1Credential(endpoint: String): Result<String> {
        return try {
            val request = Request.Builder()
                .url(endpoint)
                .post(JSONObject().put("prompt", "Reply with OK only.").toString().toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json; charset=utf-8")
                .build()
            client.newCall(request).execute().use { response ->
                val raw = readLimitedBody(response)
                if (response.code !in 200..299) return Result.failure(IllegalStateException("exam1 HTTP ${response.code}: ${extractErrorMessage(raw, response.code)}"))
                val json = JSONObject(raw)
                val choices = json.optJSONArray("choices")
                val content = json.optString("content").trim()
                if ((choices?.length() ?: 0) > 0 || content.isNotBlank()) {
                    val text = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content").orEmpty().trim().ifBlank { content }
                    Result.success(text.ifBlank { "Connected" })
                } else Result.failure(IllegalStateException("exam1 لم يُرجع choices أو content"))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("فشل اختبار اتصال exam1: ${e.message ?: "خطأ غير معروف"}", e))
        }
    }

    private fun callGrokCloud(model: String, systemPrompt: String, userPrompt: String, stream: Boolean = false): Result<String> {
        if (userPrompt.isBlank()) return Result.failure(IllegalArgumentException("قائمة الرسائل فارغة أو محتواها الأخير فارغ"))
        val selectedModel = model.takeIf { it.startsWith("grok-") } ?: "grok-4.7"
        val history = JSONArray()
            .put(JSONObject().put("role", "system").put("content", systemPrompt))
            .put(JSONObject().put("role", "user").put("content", userPrompt))
        val body = JSONObject().apply {
            put("appName", "transcribe")
            put("responseType", "json")
            put("userQuery", userPrompt)
            put("context", "You are a helpful assistant.")
            put("maxTokens", 1024)
            put("model", selectedModel)
            put("osType", "android")
            put("userId", "web_client")
            put("deepThinking", false)
            put("webSearch", false)
            put("history", history)
        }
        return postGrokCloud(body, stream)
    }

    private fun testGrokCloudCredential(): Result<String> {
        val body = JSONObject().apply {
            put("appName", "transcribe")
            put("responseType", "json")
            put("userQuery", "Reply with exactly OK.")
            put("context", "You are a helpful assistant.")
            put("maxTokens", 256)
            put("model", "grok-4.7")
            put("osType", "android")
            put("userId", "web_client")
            put("deepThinking", false)
            put("webSearch", false)
        }
        return postGrokCloud(body, stream = false)
    }

    private fun postGrokCloud(body: JSONObject, stream: Boolean): Result<String> {
        return try {
            val request = Request.Builder()
                .url(GROKCLOUD_URL)
                .post(body.toString().toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json; charset=utf-8")
                .build()
            client.newCall(request).execute().use { response ->
                val raw = readLimitedBody(response)
                if (response.code !in 200..299) {
                    return Result.failure(IllegalStateException("grokcloud HTTP ${response.code}: ${extractErrorMessage(raw, response.code)}"))
                }
                val json = JSONObject(raw)
                val text = json.optString("response").trim().ifBlank { json.optString("text").trim() }
                if (text.isBlank()) return Result.failure(IllegalStateException("grokcloud أعاد نصاً فارغاً"))
                val chatCompletion = JSONObject().put("choices", JSONArray().put(
                    JSONObject().put("message", JSONObject().put("role", "assistant").put("content", text))
                ))
                val normalizedText = chatCompletion.optJSONArray("choices")!!.optJSONObject(0)!!
                    .optJSONObject("message")!!.optString("content")
                Result.success(if (stream) toSse(normalizedText) else normalizedText)
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("تعذر الاتصال بخادم grokcloud: ${e.message ?: "خطأ غير معروف"}", e))
        }
    }

    private fun toSse(text: String): String = "data: $text\n\ndata: [DONE]\n\n"

    private fun readLimitedBody(response: okhttp3.Response): String {
        val body = response.body ?: return ""
        if (body.contentLength() > MAX_RESPONSE_BYTES) throw IOException("حجم رد الخادم تجاوز الحد المسموح")
        val buffer = Buffer()
        body.source().use { source ->
            while (true) {
                val remaining = MAX_RESPONSE_BYTES + 1 - buffer.size
                if (remaining <= 0L) throw IOException("حجم رد الخادم تجاوز الحد المسموح")
                val read = source.read(buffer, minOf(8192L, remaining))
                if (read == -1L) break
            }
        }
        if (buffer.size > MAX_RESPONSE_BYTES) throw IOException("حجم رد الخادم تجاوز الحد المسموح")
        return buffer.readUtf8()
    }

    private fun callGemini(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String
    ): Result<String> {
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$encodedKey"
        val bodyJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                }
            ))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
            })
        }
        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException(extractErrorMessage(raw, response.code)))
            }
            val json = JSONObject(raw)
            val text = json.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")
                ?.trim()
            return if (!text.isNullOrBlank()) Result.success(cleanModelOutput(text))
            else Result.failure(IllegalStateException("Empty response from Gemini"))
        }
    }

    private fun callOpenAiCompatible(
        endpoint: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String
    ): Result<String> {
        val normalizedUrl = resolveOpenAiChatUrl(endpoint)

        val bodyJson = JSONObject().apply {
            put("model", model)
            put("temperature", 0.2)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                put(JSONObject().put("role", "user").put("content", userPrompt))
            })
        }

        val builder = Request.Builder()
            .url(normalizedUrl)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .header("Content-Type", "application/json")
        if (apiKey.isNotBlank()) {
            builder.header("Authorization", "Bearer $apiKey")
        }

        client.newCall(builder.build()).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException(extractErrorMessage(raw, response.code)))
            }
            val json = JSONObject(raw)
            val text = json.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.trim()
            return if (!text.isNullOrBlank()) Result.success(cleanModelOutput(text))
            else Result.failure(IllegalStateException("Empty response from provider"))
        }
    }

    private fun callAnthropic(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String
    ): Result<String> {
        val bodyJson = JSONObject().apply {
            put("model", model)
            put("max_tokens", 1024)
            put("system", systemPrompt)
            put("messages", JSONArray().put(
                JSONObject().put("role", "user").put("content", userPrompt)
            ))
        }
        val request = Request.Builder()
            .url(AiProviderType.ANTHROPIC.defaultBaseUrl)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException(extractErrorMessage(raw, response.code)))
            }
            val json = JSONObject(raw)
            val text = json.optJSONArray("content")
                ?.optJSONObject(0)
                ?.optString("text")
                ?.trim()
            return if (!text.isNullOrBlank()) Result.success(cleanModelOutput(text))
            else Result.failure(IllegalStateException("Empty response from Claude"))
        }
    }

    private fun callGoogleTranslate(
        text: String,
        target: TargetTranslateLanguage,
        config: AiProviderConfig
    ): Result<String> {
        val key = config.translationApiKey.trim()
        if (key.isBlank()) {
            return Result.failure(IllegalStateException("Missing Google Cloud Translate API key"))
        }
        val baseUrl = config.translationEndpoint.trim().ifBlank { TranslationProviderType.GOOGLE_TRANSLATE.defaultEndpoint }
        val url = "$baseUrl?key=${URLEncoder.encode(key, "UTF-8")}"
        val bodyJson = JSONObject().apply {
            put("q", text)
            put("target", target.code)
            put("format", "text")
        }
        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IllegalStateException(extractErrorMessage(raw, response.code)))
                }
                val translated = JSONObject(raw)
                    .optJSONObject("data")
                    ?.optJSONArray("translations")
                    ?.optJSONObject(0)
                    ?.optString("translatedText")
                    ?.trim()
                if (!translated.isNullOrBlank()) Result.success(translated)
                else Result.failure(IllegalStateException("Empty translation response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callDeepL(
        text: String,
        target: TargetTranslateLanguage,
        config: AiProviderConfig
    ): Result<String> {
        val key = config.translationApiKey.trim()
        if (key.isBlank()) {
            return Result.failure(IllegalStateException("Missing DeepL Auth Key"))
        }
        val endpoint = config.translationEndpoint.trim().ifBlank { TranslationProviderType.DEEPL.defaultEndpoint }
        val bodyJson = JSONObject().apply {
            put("text", JSONArray().put(text))
            put("target_lang", target.deepLCode)
        }
        val request = Request.Builder()
            .url(endpoint)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .header("Authorization", "DeepL-Auth-Key $key")
            .header("Content-Type", "application/json")
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IllegalStateException(extractErrorMessage(raw, response.code)))
                }
                val translated = JSONObject(raw)
                    .optJSONArray("translations")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.trim()
                if (!translated.isNullOrBlank()) Result.success(translated)
                else Result.failure(IllegalStateException("Empty DeepL response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callLibreTranslate(
        text: String,
        target: TargetTranslateLanguage,
        config: AiProviderConfig
    ): Result<String> {
        val endpoint = config.translationEndpoint.trim().ifBlank { TranslationProviderType.LIBRE_TRANSLATE.defaultEndpoint }
        val bodyJson = JSONObject().apply {
            put("q", text)
            put("source", "auto")
            put("target", target.code)
            put("format", "text")
            if (config.translationApiKey.isNotBlank()) {
                put("api_key", config.translationApiKey.trim())
            }
        }
        val request = Request.Builder()
            .url(endpoint)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .header("Content-Type", "application/json")
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IllegalStateException(extractErrorMessage(raw, response.code)))
                }
                val translated = JSONObject(raw).optString("translatedText").trim()
                if (translated.isNotBlank()) Result.success(translated)
                else Result.failure(IllegalStateException("Empty translation response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cleanModelOutput(text: String): String {
        val trimmed = text.trim()
        return if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) {
            trimmed.substring(1, trimmed.length - 1).trim()
        } else {
            trimmed
        }
    }

    private fun extractErrorMessage(raw: String, code: Int): String {
        return try {
            val obj = JSONObject(raw)
            val errObj = obj.optJSONObject("error")
            val msg = errObj?.optString("message")?.takeIf { it.isNotBlank() }
                ?: obj.optString("message").takeIf { it.isNotBlank() }
                ?: obj.optString("error").takeIf { it.isNotBlank() }
            if (msg != null) "HTTP $code: $msg" else "HTTP $code"
        } catch (_: Exception) {
            "HTTP $code"
        }
    }
}
