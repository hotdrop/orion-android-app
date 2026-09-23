package jp.hotdrop.orion.data.incoming

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import jp.hotdrop.orion.data.local.MIGRATION_1_2
import jp.hotdrop.orion.data.local.MIGRATION_2_3
import jp.hotdrop.orion.data.local.OrionDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class IncomingMigrationTest {
    @get:Rule val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(), OrionDatabase::class.java,
    )

    @Test fun migrationPreservesExistingDataAndSupportsPersonalRecords() = runBlocking {
        helper.createDatabase(DATABASE_NAME, 1).apply {
            execSQL("INSERT INTO settings VALUES (1, 'Reports', 'root')")
            execSQL("INSERT INTO knowledge_archive_entries VALUES (7, 'Saved article', 'https://example.com', 'Keep me', 1, 2)")
            execSQL("INSERT INTO incoming_intelligence_documents VALUES ('root', 'doc', 'Report', 3, '/', 'https://example.com/doc', 0)")
            execSQL("INSERT INTO incoming_intelligence_sync_state VALUES ('root', 4)")
            close()
        }
        helper.runMigrationsAndValidate(DATABASE_NAME, 2, true, MIGRATION_1_2).apply {
            query("SELECT google_drive_folder_id FROM settings").use { assertTrue(it.moveToFirst()); assertEquals("root", it.getString(0)) }
            query("SELECT memo FROM knowledge_archive_entries WHERE id = 7").use { assertTrue(it.moveToFirst()); assertEquals("Keep me", it.getString(0)) }
            close()
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(context, OrionDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
        try {
            val document = db.incomingPersonalDao().observeDocuments("root").first().single()
            assertEquals("Report", document.title)
            assertFalse(document.isNew)
            assertFalse(document.isFavorite)
            assertEquals("", document.memo)
            assertEquals(4L, db.incomingIntelligenceDao().observeLastSyncedAt("root").first())
        } finally { db.close() }
    }

    companion object { private const val DATABASE_NAME = "incoming-migration-test" }
}
