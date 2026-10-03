package jp.hotdrop.orion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import jp.hotdrop.orion.data.local.dao.IncomingIntelligenceDao
import jp.hotdrop.orion.data.local.dao.IncomingPersonalDao
import jp.hotdrop.orion.data.local.dao.SettingsDao
import jp.hotdrop.orion.data.local.dao.SignalFocusDao
import jp.hotdrop.orion.data.local.entity.FocusCheckpointEntity
import jp.hotdrop.orion.data.local.entity.FocusDraftEntity
import jp.hotdrop.orion.data.local.entity.FocusEntity
import jp.hotdrop.orion.data.local.entity.FocusKeywordEntity
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceEntity
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceSyncStateEntity
import jp.hotdrop.orion.data.local.entity.IncomingPersonalEntity
import jp.hotdrop.orion.data.local.entity.SettingsEntity
import jp.hotdrop.orion.data.local.entity.SignalEntity
import jp.hotdrop.orion.data.local.entity.SignalKeywordEntity

@Database(
    entities = [
        IncomingPersonalEntity::class,
        SettingsEntity::class,
        SignalEntity::class,
        FocusEntity::class,
        SignalKeywordEntity::class,
        FocusKeywordEntity::class,
        FocusDraftEntity::class,
        FocusCheckpointEntity::class,
        IncomingIntelligenceEntity::class,
        IncomingIntelligenceSyncStateEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class OrionDatabase : RoomDatabase() {
    abstract fun incomingPersonalDao(): IncomingPersonalDao

    abstract fun settingsDao(): SettingsDao

    abstract fun signalFocusDao(): SignalFocusDao

    abstract fun incomingIntelligenceDao(): IncomingIntelligenceDao

    companion object {
        const val DATABASE_NAME = "orion.db"
    }
}
