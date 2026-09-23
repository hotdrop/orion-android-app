package jp.hotdrop.orion.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS incoming_personal_documents (
                drive_file_id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                modified_at INTEGER NOT NULL,
                relative_path TEXT NOT NULL,
                web_url TEXT NOT NULL,
                is_favorite INTEGER NOT NULL,
                memo TEXT NOT NULL
            )
        """.trimIndent())
    }
}

/** Signal・Focusの保存領域を作成し、廃止されたKnowledge Archiveのテーブルを削除する。 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS signals (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                body TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS focuses (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                analysis TEXT NOT NULL,
                note TEXT NOT NULL,
                generatedAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS signal_keywords (
                signalId INTEGER NOT NULL,
                normalized TEXT NOT NULL,
                display TEXT NOT NULL,
                position INTEGER NOT NULL,
                PRIMARY KEY(signalId, normalized),
                FOREIGN KEY(signalId) REFERENCES signals(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_signal_keywords_normalized ON signal_keywords(normalized)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS focus_keywords (
                focusId INTEGER NOT NULL,
                normalized TEXT NOT NULL,
                display TEXT NOT NULL,
                position INTEGER NOT NULL,
                PRIMARY KEY(focusId, normalized),
                FOREIGN KEY(focusId) REFERENCES focuses(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_keywords_normalized ON focus_keywords(normalized)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS focus_draft (
                id INTEGER NOT NULL PRIMARY KEY,
                token TEXT NOT NULL,
                startedAt INTEGER NOT NULL,
                upperSignalId INTEGER NOT NULL,
                signalCount INTEGER NOT NULL,
                prompt TEXT NOT NULL,
                analysis TEXT NOT NULL,
                note TEXT NOT NULL,
                keywords TEXT NOT NULL,
                stage INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS focus_checkpoint (
                id INTEGER NOT NULL PRIMARY KEY,
                upperSignalId INTEGER NOT NULL,
                startedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE knowledge_archive_entries")
    }
}
