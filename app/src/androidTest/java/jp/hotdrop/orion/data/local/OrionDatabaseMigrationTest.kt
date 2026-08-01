package jp.hotdrop.orion.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OrionDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        OrionDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2_preservesExistingDataAndAddsIncomingTables() {
        helper.createDatabase(DatabaseName, 1).apply {
            execSQL(
                "INSERT INTO settings (id, google_drive_path) VALUES (1, 'ORION/Incoming')",
            )
            close()
        }

        helper.runMigrationsAndValidate(
            DatabaseName,
            2,
            true,
            OrionDatabase.Migration1To2,
        ).use { database ->
            database.query(
                "SELECT google_drive_path, google_drive_folder_id FROM settings WHERE id = 1",
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals("ORION/Incoming", cursor.getString(0))
                assertEquals(null, cursor.getString(1))
            }
            database.query("SELECT COUNT(*) FROM incoming_intelligence_documents").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    private companion object {
        const val DatabaseName = "migration-test"
    }
}
