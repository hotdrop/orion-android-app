package jp.hotdrop.orion.data

import jp.hotdrop.orion.data.local.dao.IncomingPersonalDao
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceRecord
import jp.hotdrop.orion.data.local.entity.IncomingPersonalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeIncomingPersonalDao : IncomingPersonalDao() {
    val records = mutableMapOf<String, IncomingPersonalEntity>()
    var cached: IncomingIntelligenceRecord? = IncomingIntelligenceRecord("doc", "Title", 10, "/", "https://example.com", false)
    var failSave = false
    override fun observeDocuments(rootFolderId: String?): Flow<List<IncomingIntelligenceRecord>> = flowOf(emptyList())
    override suspend fun get(id: String) = records[id]
    override suspend fun insertIfAbsent(document: IncomingPersonalEntity) { records.putIfAbsent(document.driveFileId, document) }
    override suspend fun toggleFavoriteValue(id: String) { records[id]?.let { records[id] = it.copy(isFavorite = !it.isFavorite) } }
    override suspend fun updateMemo(id: String, memo: String) {
        if (failSave) throw java.io.IOException("disk unavailable")
        records[id]?.let { records[id] = it.copy(memo = memo) }
    }
    override suspend fun delete(id: String) { records.remove(id) }
    override suspend fun getCachedDocument(id: String) = cached?.takeIf { it.id == id }
}
