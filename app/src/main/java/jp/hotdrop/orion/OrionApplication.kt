package jp.hotdrop.orion

import android.app.Application
import androidx.room.Room
import jp.hotdrop.orion.data.KnowledgeArchiveRepository
import jp.hotdrop.orion.data.RoomKnowledgeArchiveRepository
import jp.hotdrop.orion.data.local.OrionDatabase
import jp.hotdrop.orion.data.remote.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.remote.HttpGoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.IncomingIntelligenceRepository
import jp.hotdrop.orion.data.RoomIncomingIntelligenceRepository
import jp.hotdrop.orion.data.RoomSettingsRepository
import jp.hotdrop.orion.data.SettingsRepository

class OrionApplication : Application() {
    private val database: OrionDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            OrionDatabase::class.java,
            OrionDatabase.DATABASE_NAME,
        ).build()
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
