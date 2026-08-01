package jp.hotdrop.orion.data.archive

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import jp.hotdrop.orion.data.local.OrionDatabase
import jp.hotdrop.orion.data.local.dao.KnowledgeArchiveDao
import jp.hotdrop.orion.data.local.entity.KnowledgeArchiveEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class KnowledgeArchiveDaoTest {
    private lateinit var database: OrionDatabase
    private lateinit var dao: KnowledgeArchiveDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, OrionDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.knowledgeArchiveDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun entries_canBeInsertedUpdatedObservedAndDeleted() = runBlocking {
        assertEquals(emptyList<KnowledgeArchiveEntity>(), dao.observeAll().first())

        val olderId = dao.insert(entity(title = "Older", updatedAt = 10))
        val newerId = dao.insert(entity(title = "Newer", updatedAt = 20))
        assertEquals(listOf("Newer", "Older"), dao.observeAll().first().map { it.title })

        val older = requireNotNull(dao.getById(olderId))
        assertEquals(1, dao.update(older.copy(title = "Updated", updatedAt = 30)))
        assertEquals(listOf("Updated", "Newer"), dao.observeAll().first().map { it.title })

        val newer = requireNotNull(dao.getById(newerId))
        assertEquals(1, dao.delete(newer))
        assertNull(dao.getById(newerId))
    }

    private fun entity(title: String, updatedAt: Long) = KnowledgeArchiveEntity(
        title = title,
        url = "https://example.com/$title",
        memo = "memo",
        createdAt = updatedAt,
        updatedAt = updatedAt,
    )
}
