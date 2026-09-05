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
