package jp.hotdrop.orion.data

import javax.inject.Inject
import javax.inject.Singleton
import jp.hotdrop.orion.data.local.dao.IncomingPersonalDao
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceRecord
import jp.hotdrop.orion.data.local.entity.IncomingPersonalEntity

@Singleton
class IncomingPersonalRepository @Inject constructor(private val dao: IncomingPersonalDao) {
    fun observeDocuments(rootFolderId: String?) = dao.observeDocuments(rootFolderId)

    suspend fun getDocument(id: String): IncomingIntelligenceRecord? {
        val personal = dao.get(id)
        val cached = dao.getCachedDocument(id)
        val latestCached = cached?.takeIf { personal == null || it.modifiedAt >= personal.modifiedAt }
        return latestCached?.copy(isFavorite = personal?.isFavorite ?: false, memo = personal?.memo.orEmpty())
            ?: personal?.let {
                IncomingIntelligenceRecord(it.driveFileId, it.title, it.modifiedAt, it.relativePath,
                    it.webUrl, false, it.isFavorite, it.memo, false)
            }
    }

    suspend fun prepareMemo(id: String): IncomingIntelligenceRecord? {
        val document = getDocument(id) ?: return null
        dao.insertIfAbsent(document.snapshot())
        return document
    }

    suspend fun toggleFavorite(id: String) {
        dao.toggleFavorite(requireNotNull(getDocument(id)) { "資料が見つかりません。" }.snapshot())
    }

    suspend fun saveMemo(id: String, memo: String) {
        dao.saveMemo(requireNotNull(getDocument(id)) { "資料が見つかりません。" }.snapshot(), memo.trim())
    }

    suspend fun deleteLocalRecord(id: String) = dao.delete(id)

    private fun IncomingIntelligenceRecord.snapshot() = IncomingPersonalEntity(
        driveFileId = id, title = title, modifiedAt = modifiedAt,
        relativePath = relativePath, webUrl = webUrl,
    )
}
