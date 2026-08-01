package jp.hotdrop.orion.data.settings

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import jp.hotdrop.orion.data.local.OrionDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SettingsDaoTest {
    private lateinit var database: OrionDatabase
    private lateinit var dao: SettingsDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, OrionDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.settingsDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun settings_canBeObservedUpdatedAndDeleted() = runBlocking {
        assertNull(dao.observeGoogleDrivePath().first())

        dao.upsert(SettingsEntity(googleDrivePath = "ORION/Incoming"))
        assertEquals("ORION/Incoming", dao.observeGoogleDrivePath().first())

        dao.upsert(SettingsEntity(googleDrivePath = "ORION/Reports"))
        assertEquals("ORION/Reports", dao.observeGoogleDrivePath().first())

        dao.delete()
        assertNull(dao.observeGoogleDrivePath().first())
    }
}
