package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BayanDao {

    // --- Clipboard ---
    @Query("SELECT * FROM clipboard_items ORDER BY isPinned DESC, CASE WHEN isPinned = 1 THEN pinOrder ELSE 0 END ASC, timestamp DESC")
    fun observeClipboardItems(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_items ORDER BY isPinned DESC, CASE WHEN isPinned = 1 THEN pinOrder ELSE 0 END ASC, timestamp DESC LIMIT :limit")
    suspend fun getClipboardItemsOnce(limit: Int = 60): List<ClipboardEntity>

    @Query("SELECT * FROM clipboard_items WHERE text = :text LIMIT 1")
    suspend fun findClipboardByText(text: String): ClipboardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClipboardItem(item: ClipboardEntity): Long

    @Query("UPDATE clipboard_items SET isPinned = :pinned, timestamp = :timestamp, pinOrder = :pinOrder WHERE id = :id")
    suspend fun updateClipboardPinned(id: Long, pinned: Boolean, pinOrder: Long = 0L, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE clipboard_items SET pinOrder = :pinOrder WHERE id = :id")
    suspend fun updateClipboardPinOrder(id: Long, pinOrder: Long)

    @Query("UPDATE clipboard_items SET timestamp = :timestamp WHERE id = :id")
    suspend fun touchClipboardTimestamp(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM clipboard_items WHERE id = :id")
    suspend fun deleteClipboardItem(id: Long)

    @Query("DELETE FROM clipboard_items WHERE isPinned = 0")
    suspend fun clearUnpinnedClipboard()

    // --- Learned Words & Suggestions ---
    @Query("SELECT * FROM learned_words ORDER BY isUserAdded DESC, frequency DESC, lastUsedAt DESC")
    fun observeLearnedWords(): Flow<List<LearnedWordEntity>>

    @Query("SELECT COUNT(*) FROM learned_words")
    fun observeLearnedWordsCount(): Flow<Int>

    @Query("SELECT * FROM learned_words")
    suspend fun getAllLearnedWords(): List<LearnedWordEntity>

    @Query("SELECT * FROM learned_words WHERE languageCode = :languageCode AND word = :word LIMIT 1")
    suspend fun getWord(word: String, languageCode: String): LearnedWordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(entity: LearnedWordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWordsIgnore(entities: List<LearnedWordEntity>)

    @Query(
        """
        SELECT * FROM learned_words
        WHERE languageCode = :languageCode AND word LIKE :prefix || '%'
        ORDER BY frequency DESC, lastUsedAt DESC
        LIMIT :limit
        """
    )
    suspend fun searchWordsByPrefix(
        prefix: String,
        languageCode: String,
        limit: Int = 8
    ): List<LearnedWordEntity>

    @Query(
        """
        SELECT * FROM learned_words
        WHERE word LIKE :prefix || '%'
        ORDER BY frequency DESC, lastUsedAt DESC
        LIMIT :limit
        """
    )
    suspend fun searchWordsAnyLanguage(prefix: String, limit: Int = 8): List<LearnedWordEntity>

    @Query(
        """
        SELECT * FROM learned_words
        WHERE languageCode = :languageCode
        ORDER BY frequency DESC, lastUsedAt DESC
        LIMIT :limit
        """
    )
    suspend fun getTopWordsForLanguage(languageCode: String, limit: Int = 250): List<LearnedWordEntity>

    @Query("DELETE FROM learned_words WHERE word = :word AND languageCode = :languageCode")
    suspend fun deleteLearnedWord(word: String, languageCode: String)

    @Query("DELETE FROM learned_words WHERE isUserAdded = 1 OR frequency >= 1")
    suspend fun clearAllLearnedWords()

    // --- Bigrams (Next Word Prediction) ---
    @Query(
        """
        SELECT * FROM word_bigrams
        WHERE prevWord = :prevWord AND languageCode = :languageCode
        ORDER BY frequency DESC, lastUsedAt DESC
        LIMIT :limit
        """
    )
    suspend fun getNextWordPredictions(
        prevWord: String,
        languageCode: String,
        limit: Int = 5
    ): List<WordBigramEntity>

    @Query("SELECT * FROM word_bigrams WHERE prevWord = :prevWord AND nextWord = :nextWord AND languageCode = :languageCode LIMIT 1")
    suspend fun getBigram(prevWord: String, nextWord: String, languageCode: String): WordBigramEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBigram(entity: WordBigramEntity)

    // --- Saved Custom AI Providers ---
    @Query("SELECT * FROM custom_providers ORDER BY updatedAt DESC")
    fun observeCustomProviders(): Flow<List<CustomProviderEntity>>

    @Query("SELECT * FROM custom_providers WHERE id = :id LIMIT 1")
    suspend fun getCustomProviderById(id: Long): CustomProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomProvider(provider: CustomProviderEntity): Long

    @Query("DELETE FROM custom_providers WHERE id = :id")
    suspend fun deleteCustomProvider(id: Long)

    // --- Password Manager ---
    @Query("SELECT * FROM password_entries ORDER BY updatedAt DESC")
    fun observePasswordEntries(): Flow<List<PasswordEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPasswordEntry(entry: PasswordEntryEntity): Long

    @Query("DELETE FROM password_entries WHERE id = :id")
    suspend fun deletePasswordEntry(id: Long)

    // --- Custom AI Bots ---
    @Query("SELECT * FROM custom_bots ORDER BY isPinnedToToolbar DESC, updatedAt DESC")
    fun observeCustomBots(): Flow<List<CustomBotEntity>>

    @Query("SELECT * FROM custom_bots WHERE id = :id LIMIT 1")
    suspend fun getCustomBotById(id: Long): CustomBotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomBot(bot: CustomBotEntity): Long

    @Query("DELETE FROM custom_bots WHERE id = :id")
    suspend fun deleteCustomBot(id: Long)
}
