package jp.hotdrop.orion

import android.app.Application
import androidx.room.Room
import jp.hotdrop.orion.data.archive.KnowledgeArchiveRepository
import jp.hotdrop.orion.data.archive.RoomKnowledgeArchiveRepository
import jp.hotdrop.orion.data.local.OrionDatabase
import jp.hotdrop.orion.data.settings.RoomSettingsRepository
import jp.hotdrop.orion.data.settings.SettingsRepository

class OrionApplication : Application() {
    private val database: OrionDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            OrionDatabase::class.java,
            OrionDatabase.Name,
        ).build()
    }

    val settingsRepository: SettingsRepository by lazy {
        RoomSettingsRepository(database.settingsDao())
    }

    val knowledgeArchiveRepository: KnowledgeArchiveRepository by lazy {
        RoomKnowledgeArchiveRepository(database.knowledgeArchiveDao())
    }
}
