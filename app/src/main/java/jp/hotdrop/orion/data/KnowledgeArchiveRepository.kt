package jp.hotdrop.orion.data

import jp.hotdrop.orion.data.local.dao.KnowledgeArchiveDao
import jp.hotdrop.orion.data.local.entity.KnowledgeArchiveEntity
import jp.hotdrop.orion.model.KnowledgeArchiveDraft
import jp.hotdrop.orion.model.KnowledgeArchiveEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnowledgeArchiveRepository internal constructor(
    private val dao: KnowledgeArchiveDao,
    private val currentTimeMillis: () -> Long,
) {
    @Inject
    constructor(dao: KnowledgeArchiveDao) : this(dao, System::currentTimeMillis)

    fun observeEntries(): Flow<List<KnowledgeArchiveEntry>> {
        return dao.observeAll().map { entries ->
            entries.map { entry -> entry.toModel() }
        }
    }

    suspend fun getEntry(id: Long): KnowledgeArchiveEntry? {
        return dao.getById(id)?.toModel()
    }

    suspend fun saveEntry(id: Long?, draft: KnowledgeArchiveDraft): Long {
        val normalizedDraft = draft.normalized()
        val now = currentTimeMillis()
        if (id == null) {
            return dao.insert(
                KnowledgeArchiveEntity(
                    title = normalizedDraft.title,
                    url = normalizedDraft.url,
                    memo = normalizedDraft.memo,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }

        val existing = requireNotNull(dao.getById(id)) { "Knowledge Archive entry not found: $id" }
        check(
            dao.update(
                existing.copy(
                    title = normalizedDraft.title,
                    url = normalizedDraft.url,
                    memo = normalizedDraft.memo,
                    updatedAt = now,
                ),
            ) == 1,
        ) {
            "Knowledge Archive entry could not be updated: $id"
        }
        return id
    }

    suspend fun deleteEntry(id: Long) {
        val existing = requireNotNull(dao.getById(id)) { "Knowledge Archive entry not found: $id" }
        check(dao.delete(existing) == 1) { "Knowledge Archive entry could not be deleted: $id" }
    }

    private fun KnowledgeArchiveEntity.toModel(): KnowledgeArchiveEntry = KnowledgeArchiveEntry(
        id = id,
        title = title,
        url = url,
        memo = memo,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
