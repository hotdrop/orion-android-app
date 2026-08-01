package jp.hotdrop.orion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import jp.hotdrop.orion.data.local.dao.KnowledgeArchiveDao
import jp.hotdrop.orion.data.local.entity.KnowledgeArchiveEntity
import jp.hotdrop.orion.data.local.dao.IncomingIntelligenceDao
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceEntity
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceSyncStateEntity
import jp.hotdrop.orion.data.local.dao.SettingsDao
import jp.hotdrop.orion.data.local.entity.SettingsEntity

@Database(
    entities = [
        SettingsEntity::class,
        KnowledgeArchiveEntity::class,
        IncomingIntelligenceEntity::class,
        IncomingIntelligenceSyncStateEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class OrionDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun knowledgeArchiveDao(): KnowledgeArchiveDao
    abstract fun incomingIntelligenceDao(): IncomingIntelligenceDao

    companion object {
        const val DATABASE_NAME = "orion.db"
    }
}
