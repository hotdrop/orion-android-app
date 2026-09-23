package jp.hotdrop.orion.data.intelligence

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import jp.hotdrop.orion.data.local.MIGRATION_1_2
import jp.hotdrop.orion.data.local.MIGRATION_2_3
import jp.hotdrop.orion.data.local.OrionDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignalFocusMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        OrionDatabase::class.java,
    )

    @Test
    fun versionTwoPreservesOtherFeaturesAndRemovesKnowledge() = verifyMigration(2)

    @Test
    fun versionOneUpgradesThroughBothMigrations() = verifyMigration(1)

    private fun verifyMigration(from: Int) {
        val name = "signal-focus-migration-$from"
        helper.createDatabase(name, from).apply {
            execSQL("INSERT INTO settings VALUES (1, 'Reports', 'root')")
            execSQL("INSERT INTO knowledge_archive_entries VALUES (7, 'Old record', 'https://example.com', 'retired', 1, 2)")
            execSQL(
                """
                INSERT INTO incoming_intelligence_documents
                VALUES ('root', 'doc', 'Report', 3, '/', 'https://example.com/doc', 0)
                """.trimIndent(),
            )
            execSQL("INSERT INTO incoming_intelligence_sync_state VALUES ('root', 4)")
            if (from == 2) {
                execSQL(
                    """
                    INSERT INTO incoming_personal_documents
                    VALUES ('doc', 'Report', 3, '/', 'https://example.com/doc', 1, 'Keep my note')
                    """.trimIndent(),
                )
            }
            close()
        }
        helper.runMigrationsAndValidate(name, 3, true, MIGRATION_1_2, MIGRATION_2_3).use { db ->
            db.query("SELECT google_drive_folder_id FROM settings").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("root", cursor.getString(0))
            }
            db.query("SELECT title FROM incoming_intelligence_documents").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Report", cursor.getString(0))
            }
            db.query("SELECT * FROM incoming_intelligence_sync_state").use { assertEquals(1, it.count) }
            db.query("SELECT name FROM sqlite_master WHERE name = 'knowledge_archive_entries'").use { assertEquals(0, it.count) }
            if (from == 2) {
                db.query("SELECT memo FROM incoming_personal_documents").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("Keep my note", cursor.getString(0))
                }
            }
            listOf(
                "signals",
                "focuses",
                "signal_keywords",
                "focus_keywords",
                "focus_draft",
                "focus_checkpoint",
            ).forEach { table ->
                db.query("SELECT * FROM $table").use { assertEquals(0, it.count) }
            }
        }
    }
}
