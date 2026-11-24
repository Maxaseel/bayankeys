package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clipboard_items",
    indices = [Index(value = ["isPinned", "timestamp"])]
)
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(
    tableName = "learned_words",
    primaryKeys = ["word", "languageCode"],
    indices = [Index(value = ["languageCode", "frequency"])]
)
data class LearnedWordEntity(
    val word: String,
    val languageCode: String,
    val frequency: Int = 1,
    val lastUsedAt: Long = System.currentTimeMillis(),
    val isUserAdded: Boolean = false
)

@Entity(
    tableName = "word_bigrams",
    primaryKeys = ["prevWord", "nextWord", "languageCode"]
)
data class WordBigramEntity(
    val prevWord: String,
    val nextWord: String,
    val languageCode: String,
    val frequency: Int = 1,
    val lastUsedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_providers")
data class CustomProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val selectedModel: String,
    val fetchedModelsCsv: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun fetchedModelsList(): List<String> =
        fetchedModelsCsv.split("\n").map { it.trim() }.filter { it.isNotBlank() }.distinct()
}

@Entity(tableName = "password_entries")
data class PasswordEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteName: String,
    val siteUrl: String = "",
    val username: String,
    val encryptedPassword: String,
    val brandId: String = "custom",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_bots")
data class CustomBotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val prompt: String,
    val iconId: String = "bot_chat",
    val accentArgb: Long = 0xFF3B82F6,
    val knowledgeSourcesJson: String = "[]",
    val isPinnedToToolbar: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
