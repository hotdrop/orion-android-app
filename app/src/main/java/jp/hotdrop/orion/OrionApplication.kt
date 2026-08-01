package jp.hotdrop.orion

import android.app.Application
import androidx.room.Room
import jp.hotdrop.orion.data.archive.KnowledgeArchiveRepository
import jp.hotdrop.orion.data.archive.RoomKnowledgeArchiveRepository
import jp.hotdrop.orion.data.local.OrionDatabase
import jp.hotdrop.orion.data.incoming.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.incoming.HttpGoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.incoming.IncomingIntelligenceRepository
import jp.hotdrop.orion.data.incoming.RoomIncomingIntelligenceRepository
import jp.hotdrop.orion.data.settings.RoomSettingsRepository
import jp.hotdrop.orion.data.settings.SettingsRepository

class OrionApplication : Application() {
    private val database: OrionDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            OrionDatabase::class.java,
            OrionDatabase.Name,
        ).addMigrations(OrionDatabase.Migration1To2).build()
    }

    val settingsRepository: SettingsRepository by lazy {
        RoomSettingsRepository(database.settingsDao())
    }

    val knowledgeArchiveRepository: KnowledgeArchiveRepository by lazy {
        RoomKnowledgeArchiveRepository(database.knowledgeArchiveDao())
    }

    val googleDriveRemoteDataSource: GoogleDriveRemoteDataSource by lazy {
        HttpGoogleDriveRemoteDataSource()
    }

    val incomingIntelligenceRepository: IncomingIntelligenceRepository by lazy {
        RoomIncomingIntelligenceRepository(
            dao = database.incomingIntelligenceDao(),
            remoteDataSource = googleDriveRemoteDataSource,
        )
    }
}
