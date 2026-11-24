package com.example

import android.app.Application
import com.example.data.local.BayanDatabase
import com.example.data.local.SettingsRepository
import com.example.data.remote.AiTranslationService
import com.example.data.repository.BayanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BayanApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: BayanDatabase by lazy { BayanDatabase.getInstance(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val bayanRepository: BayanRepository by lazy {
        BayanRepository(database.bayanDao(), settingsRepository)
    }
    val aiTranslationService: AiTranslationService by lazy { AiTranslationService() }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            bayanRepository.ensureSeeded()
        }
    }
}
