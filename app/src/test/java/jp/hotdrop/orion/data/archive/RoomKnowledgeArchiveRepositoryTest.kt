package jp.hotdrop.orion.data.archive

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomKnowledgeArchiveRepositoryTest {
    @Test
    fun saveEntry_normalizesValuesAndUsesClock() = runTest {
        val dao = FakeKnowledgeArchiveDao()
        val repository = RoomKnowledgeArchiveRepository(dao, currentTimeMillis = { 100L })

        val id = repository.saveEntry(
            id = null,
            draft = KnowledgeArchiveDraft(
                title = "  Compose State  ",
                url = " https://developer.android.com/compose ",
                memo = "  Read later  ",
            ),
        )

        assertEquals(1L, id)
        assertEquals("Compose State", dao.entries.value.single().title)
        assertEquals("https://developer.android.com/compose", dao.entries.value.single().url)
        assertEquals("Read later", dao.entries.value.single().memo)
        assertEquals(100L, dao.entries.value.single().createdAt)
        assertEquals(100L, dao.entries.value.single().updatedAt)
    }

    @Test
    fun updateEntry_preservesCreatedAtAndChangesUpdatedAt() = runTest {
        val dao = FakeKnowledgeArchiveDao(
            listOf(
                KnowledgeArchiveEntity(
                    id = 7,
                    title = "Old",
                    url = "https://example.com/old",
                    memo = "",
                    createdAt = 10,
                    updatedAt = 10,
                ),
            ),
        )
        val repository = RoomKnowledgeArchiveRepository(dao, currentTimeMillis = { 20L })

        repository.saveEntry(
            id = 7,
            draft = KnowledgeArchiveDraft("New", "https://example.com/new", "Memo"),
        )

        val updated = dao.entries.value.single()
        assertEquals(10L, updated.createdAt)
        assertEquals(20L, updated.updatedAt)
        assertEquals("New", updated.title)
    }

    @Test
    fun invalidUrl_isRejectedWithoutWriting() = runTest {
        val dao = FakeKnowledgeArchiveDao()
        val repository = RoomKnowledgeArchiveRepository(dao)

        val result = runCatching {
            repository.saveEntry(null, KnowledgeArchiveDraft("Title", "ftp://example.com", ""))
        }

        assertTrue(result.isFailure)
        assertEquals(emptyList<KnowledgeArchiveEntity>(), dao.entries.value)
        assertEquals(
            KnowledgeArchiveValidationError.UrlInvalid,
            validateKnowledgeArchiveDraft(KnowledgeArchiveDraft("Title", "ftp://example.com", "")),
        )
    }

    @Test
    fun deleteEntry_removesRecord() = runTest {
        val dao = FakeKnowledgeArchiveDao(
            listOf(KnowledgeArchiveEntity(4, "Title", "https://example.com", "", 1, 1)),
        )
        val repository = RoomKnowledgeArchiveRepository(dao)

        repository.deleteEntry(4)

        assertNull(repository.getEntry(4))
        assertEquals(emptyList<KnowledgeArchiveEntry>(), repository.observeEntries().first())
    }
}

private class FakeKnowledgeArchiveDao(
    initialEntries: List<KnowledgeArchiveEntity> = emptyList(),
) : KnowledgeArchiveDao {
    val entries = MutableStateFlow(initialEntries)

    override fun observeAll(): Flow<List<KnowledgeArchiveEntity>> = entries

    override suspend fun getById(id: Long): KnowledgeArchiveEntity? =
        entries.value.firstOrNull { it.id == id }

    override suspend fun insert(entry: KnowledgeArchiveEntity): Long {
        val id = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1L
        entries.value += entry.copy(id = id)
        return id
    }

    override suspend fun update(entry: KnowledgeArchiveEntity): Int {
        if (entries.value.none { it.id == entry.id }) return 0
        entries.value = entries.value.map { if (it.id == entry.id) entry else it }
        return 1
    }

    override suspend fun delete(entry: KnowledgeArchiveEntity): Int {
        if (entries.value.none { it.id == entry.id }) return 0
        entries.value = entries.value.filterNot { it.id == entry.id }
        return 1
    }
}
