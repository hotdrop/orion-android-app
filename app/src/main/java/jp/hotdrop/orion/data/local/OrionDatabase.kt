package jp.hotdrop.orion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import jp.hotdrop.orion.data.archive.KnowledgeArchiveDao
import jp.hotdrop.orion.data.archive.KnowledgeArchiveEntity
import jp.hotdrop.orion.data.incoming.IncomingIntelligenceDao
import jp.hotdrop.orion.data.incoming.IncomingIntelligenceEntity
import jp.hotdrop.orion.data.incoming.IncomingIntelligenceSyncStateEntity
import jp.hotdrop.orion.data.settings.SettingsDao
import jp.hotdrop.orion.data.settings.SettingsEntity

@Database(
    entities = [
        SettingsEntity::class,
        KnowledgeArchiveEntity::class,
        IncomingIntelligenceEntity::class,
        IncomingIntelligenceSyncStateEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class OrionDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun knowledgeArchiveDao(): KnowledgeArchiveDao
    abstract fun incomingIntelligenceDao(): IncomingIntelligenceDao

    companion object {
        const val Name = "orion.db"

        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE settings ADD COLUMN google_drive_folder_id TEXT",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS incoming_intelligence_sync_state (
                        root_folder_id TEXT NOT NULL,
                        last_synced_at INTEGER NOT NULL,
                        PRIMARY KEY(root_folder_id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS incoming_intelligence_documents (
                        root_folder_id TEXT NOT NULL,
                        drive_file_id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        modified_at INTEGER NOT NULL,
                        relative_path TEXT NOT NULL,
                        web_url TEXT NOT NULL,
                        is_new INTEGER NOT NULL,
                        PRIMARY KEY(root_folder_id, drive_file_id)
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
