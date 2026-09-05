package jp.hotdrop.orion.data.incoming

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import jp.hotdrop.orion.data.IncomingPersonalRepository
import jp.hotdrop.orion.data.local.OrionDatabase
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class IncomingPersonalDaoTest {
    private lateinit var db: OrionDatabase
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, OrionDatabase::class.java).build()
    }
    @After fun close() = db.close()

    @Test fun syncAndEdits_preservePersonalValuesAndRetainMissingDocuments() = runBlocking {
        val incoming = db.incomingIntelligenceDao()
        val personal = db.incomingPersonalDao()
        val repository = IncomingPersonalRepository(personal)
        incoming.replaceForRoot("root", listOf(document()), 10)
        repository.toggleFavorite("doc")
        val sync = async { incoming.replaceForRoot("root", listOf(document().copy(title = "Renamed", modifiedAt = 20)), 20) }
        val edit = async { repository.saveMemo("doc", "Remember") }
        sync.await(); edit.await()
        val updated = personal.observeDocuments("root").first().single()
        assertEquals("Renamed", updated.title)
        assertTrue(updated.isFavorite)
        assertEquals("Remember", updated.memo)
        incoming.replaceForRoot("root", emptyList(), 30)
        val retained = personal.observeDocuments("root").first().single()
        assertFalse(retained.isSyncTarget)
        assertEquals("Renamed", retained.title)
        assertEquals(1, personal.observeDocuments(null).first().size)
        assertEquals(1, personal.observeDocuments("different-root").first().size)
        repository.deleteLocalRecord("doc")
        assertTrue(personal.observeDocuments(null).first().isEmpty())
    }

    @Test fun sharedFileAcrossRoots_hasOnePersonalRecordAndNoDuplicateRows() = runBlocking {
        val incoming = db.incomingIntelligenceDao()
        val repository = IncomingPersonalRepository(db.incomingPersonalDao())
        incoming.replaceForRoot("root", listOf(document()), 10)
        incoming.replaceForRoot("other", listOf(document().copy(rootFolderId = "other")), 10)
        repository.saveMemo("doc", "One note")
        assertEquals(1, db.incomingPersonalDao().observeDocuments("other").first().size)
        repository.toggleFavorite("doc")
        assertTrue(db.incomingPersonalDao().observeDocuments("other").first().single().isFavorite)
        repository.saveMemo("doc", "")
        repository.toggleFavorite("doc")
        assertTrue(db.incomingPersonalDao().observeDocuments(null).first().isEmpty())
    }

    private fun document() = IncomingIntelligenceEntity("root", "doc", "Report", 10, "/", "https://example.com", false)
}
