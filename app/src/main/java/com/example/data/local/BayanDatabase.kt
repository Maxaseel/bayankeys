package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ClipboardEntity::class,
        LearnedWordEntity::class,
        WordBigramEntity::class,
        CustomProviderEntity::class,
        PasswordEntryEntity::class,
        CustomBotEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class BayanDatabase : RoomDatabase() {
    abstract fun bayanDao(): BayanDao

    companion object {
        @Volatile
        private var INSTANCE: BayanDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS custom_providers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        baseUrl TEXT NOT NULL,
                        apiKey TEXT NOT NULL,
                        selectedModel TEXT NOT NULL,
                        fetchedModelsCsv TEXT NOT NULL DEFAULT '',
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS password_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        siteName TEXT NOT NULL,
                        siteUrl TEXT NOT NULL DEFAULT '',
                        username TEXT NOT NULL,
                        encryptedPassword TEXT NOT NULL,
                        brandId TEXT NOT NULL DEFAULT 'custom',
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS custom_bots (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        prompt TEXT NOT NULL,
                        iconId TEXT NOT NULL DEFAULT 'bot_chat',
                        accentArgb INTEGER NOT NULL DEFAULT 4282090230,
                        knowledgeSourcesJson TEXT NOT NULL DEFAULT '[]',
                        isPinnedToToolbar INTEGER NOT NULL DEFAULT 1,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE password_entries ADD COLUMN localIconPath TEXT")
            }
        }

        fun getInstance(context: Context): BayanDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BayanDatabase::class.java,
                    "bayan_keyboard.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
