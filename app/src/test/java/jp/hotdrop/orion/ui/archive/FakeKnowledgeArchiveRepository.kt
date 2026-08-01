package jp.hotdrop.orion.ui.archive

import jp.hotdrop.orion.data.KnowledgeArchiveRepository
import jp.hotdrop.orion.model.KnowledgeArchiveDraft
import jp.hotdrop.orion.model.KnowledgeArchiveEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeKnowledgeArchiveRepository(
    initialEntries: List<KnowledgeArchiveEntry> = emptyList(),
    var failOnLoad: Boolean = false,
    var failOnSave: Boolean = false,
    var failOnDelete: Boolean = false,
) : KnowledgeArchiveRepository {
    val entries = MutableStateFlow(initialEntries)
    val savedDrafts = mutableListOf<Pair<Long?, KnowledgeArchiveDraft>>()
    val deletedIds = mutableListOf<Long>()

    override fun observeEntries(): Flow<List<KnowledgeArchiveEntry>> {
        if (failOnLoad) error("load failed")
        return entries
    }

    override suspend fun getEntry(id: Long): KnowledgeArchiveEntry? {
        check(!failOnLoad) { "load failed" }
        return entries.value.firstOrNull { it.id == id }
    }

    override suspend fun saveEntry(id: Long?, draft: KnowledgeArchiveDraft): Long {
        savedDrafts += id to draft
        check(!failOnSave) { "save failed" }
        return id ?: 1L
    }

    override suspend fun deleteEntry(id: Long) {
        deletedIds += id
        check(!failOnDelete) { "delete failed" }
    }
}

internal fun archiveEntry(
    id: Long = 1,
    title: String = "Compose State",
    url: String = "https://developer.android.com/compose",
    memo: String = "Memo",
) = KnowledgeArchiveEntry(
    id = id,
    title = title,
    url = url,
    memo = memo,
    createdAt = 1,
    updatedAt = 2,
)
