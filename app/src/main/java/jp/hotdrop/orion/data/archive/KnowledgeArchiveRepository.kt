package jp.hotdrop.orion.data.archive

import java.net.URI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class KnowledgeArchiveEntry(
    val id: Long,
    val title: String,
    val url: String,
    val memo: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class KnowledgeArchiveDraft(
    val title: String,
    val url: String,
    val memo: String,
)

enum class KnowledgeArchiveValidationError {
    TitleRequired,
    UrlRequired,
    UrlInvalid,
}

interface KnowledgeArchiveRepository {
    fun observeEntries(): Flow<List<KnowledgeArchiveEntry>>

    suspend fun getEntry(id: Long): KnowledgeArchiveEntry?

    suspend fun saveEntry(id: Long?, draft: KnowledgeArchiveDraft): Long

    suspend fun deleteEntry(id: Long)
}

class RoomKnowledgeArchiveRepository(
    private val dao: KnowledgeArchiveDao,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : KnowledgeArchiveRepository {
    override fun observeEntries(): Flow<List<KnowledgeArchiveEntry>> =
        dao.observeAll().map { entries -> entries.map(KnowledgeArchiveEntity::toModel) }

    override suspend fun getEntry(id: Long): KnowledgeArchiveEntry? = dao.getById(id)?.toModel()

    override suspend fun saveEntry(id: Long?, draft: KnowledgeArchiveDraft): Long {
        val normalizedDraft = draft.normalized()
        require(validateKnowledgeArchiveDraft(normalizedDraft) == null) {
            "Knowledge Archive entry is invalid"
        }

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
        ) { "Knowledge Archive entry could not be updated: $id" }
        return id
    }

    override suspend fun deleteEntry(id: Long) {
        val existing = requireNotNull(dao.getById(id)) { "Knowledge Archive entry not found: $id" }
        check(dao.delete(existing) == 1) { "Knowledge Archive entry could not be deleted: $id" }
    }
}

fun validateKnowledgeArchiveDraft(
    draft: KnowledgeArchiveDraft,
): KnowledgeArchiveValidationError? = when {
    draft.title.isBlank() -> KnowledgeArchiveValidationError.TitleRequired
    draft.url.isBlank() -> KnowledgeArchiveValidationError.UrlRequired
    !draft.url.isHttpUrl() -> KnowledgeArchiveValidationError.UrlInvalid
    else -> null
}

fun KnowledgeArchiveDraft.normalized(): KnowledgeArchiveDraft = copy(
    title = title.trim(),
    url = url.trim(),
    memo = memo.trim(),
)

private fun String.isHttpUrl(): Boolean = runCatching {
    val uri = URI(trim())
    (uri.scheme.equals("http", ignoreCase = true) ||
        uri.scheme.equals("https", ignoreCase = true)) &&
        !uri.host.isNullOrBlank()
}.getOrDefault(false)

private fun KnowledgeArchiveEntity.toModel(): KnowledgeArchiveEntry = KnowledgeArchiveEntry(
    id = id,
    title = title,
    url = url,
    memo = memo,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
