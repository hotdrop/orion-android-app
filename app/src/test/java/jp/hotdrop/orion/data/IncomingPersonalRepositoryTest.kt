package jp.hotdrop.orion.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class IncomingPersonalRepositoryTest {
    @Test fun independentEdits_preserveEachOtherAndDoNotChangeDriveMetadata() = runTest {
        val dao = FakeIncomingPersonalDao()
        val repository = IncomingPersonalRepository(dao)
        val original = dao.cached
        repository.saveMemo("doc", "  useful\nnotes  ")
        repository.toggleFavorite("doc")
        assertEquals("useful\nnotes", repository.getDocument("doc")?.memo)
        assertTrue(repository.getDocument("doc")!!.isFavorite)
        repository.saveMemo("doc", "updated")
        assertTrue(repository.getDocument("doc")!!.isFavorite)
        repository.toggleFavorite("doc")
        assertEquals("updated", repository.getDocument("doc")?.memo)
        assertEquals(original, dao.cached)
    }

    @Test fun editingSnapshot_survivesRemovalDuringEditing() = runTest {
        val dao = FakeIncomingPersonalDao()
        val repository = IncomingPersonalRepository(dao)
        repository.prepareMemo("doc")
        dao.cached = null
        repository.saveMemo("doc", "Keep this document")
        val restored = repository.getDocument("doc")!!
        assertEquals("Title", restored.title)
        assertEquals("Keep this document", restored.memo)
        assertFalse(restored.isSyncTarget)
    }

    @Test fun blankMemoClearsOnlyMemo_andDeleteRemovesOnlyPersonalRecord() = runTest {
        val dao = FakeIncomingPersonalDao()
        val repository = IncomingPersonalRepository(dao)
        repository.toggleFavorite("doc")
        repository.saveMemo("doc", "notes")
        repository.saveMemo("doc", "  ")
        assertEquals("", repository.getDocument("doc")!!.memo)
        assertTrue(repository.getDocument("doc")!!.isFavorite)
        repository.deleteLocalRecord("doc")
        assertTrue(dao.records.isEmpty())
        assertNotNull(dao.cached)
    }
}
