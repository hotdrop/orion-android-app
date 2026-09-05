package jp.hotdrop.orion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceRecord
import jp.hotdrop.orion.data.local.entity.IncomingPersonalEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class IncomingPersonalDao {
    @Query("""
        SELECT d.drive_file_id AS id, d.title, d.modified_at AS modifiedAt,
            d.relative_path AS relativePath, d.web_url AS webUrl, d.is_new AS isNew,
            COALESCE(p.is_favorite, 0) AS isFavorite, COALESCE(p.memo, '') AS memo,
            1 AS isSyncTarget
        FROM incoming_intelligence_documents d
        LEFT JOIN incoming_personal_documents p ON p.drive_file_id = d.drive_file_id
        WHERE d.root_folder_id = :rootFolderId
        UNION ALL
        SELECT p.drive_file_id AS id, p.title, p.modified_at AS modifiedAt,
            p.relative_path AS relativePath, p.web_url AS webUrl, 0 AS isNew,
            p.is_favorite AS isFavorite, p.memo, 0 AS isSyncTarget
        FROM incoming_personal_documents p
        WHERE (p.is_favorite = 1 OR p.memo != '') AND NOT EXISTS (
            SELECT 1 FROM incoming_intelligence_documents d
            WHERE d.root_folder_id = :rootFolderId AND d.drive_file_id = p.drive_file_id
        )
        ORDER BY modifiedAt DESC, title ASC
    """)
    abstract fun observeDocuments(rootFolderId: String?): Flow<List<IncomingIntelligenceRecord>>

    @Query("SELECT * FROM incoming_personal_documents WHERE drive_file_id = :id")
    abstract suspend fun get(id: String): IncomingPersonalEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertIfAbsent(document: IncomingPersonalEntity)

    @Query("UPDATE incoming_personal_documents SET is_favorite = NOT is_favorite WHERE drive_file_id = :id")
    abstract suspend fun toggleFavoriteValue(id: String)

    @Query("UPDATE incoming_personal_documents SET memo = :memo WHERE drive_file_id = :id")
    abstract suspend fun updateMemo(id: String, memo: String)

    @Query("DELETE FROM incoming_personal_documents WHERE drive_file_id = :id")
    abstract suspend fun delete(id: String)

    @Transaction
    open suspend fun toggleFavorite(snapshot: IncomingPersonalEntity) {
        insertIfAbsent(snapshot)
        toggleFavoriteValue(snapshot.driveFileId)
    }

    @Transaction
    open suspend fun saveMemo(snapshot: IncomingPersonalEntity, memo: String) {
        insertIfAbsent(snapshot)
        updateMemo(snapshot.driveFileId, memo)
    }

    @Query("""
        SELECT drive_file_id AS id, title, modified_at AS modifiedAt,
            relative_path AS relativePath, web_url AS webUrl, is_new AS isNew,
            0 AS isFavorite, '' AS memo, 1 AS isSyncTarget
        FROM incoming_intelligence_documents WHERE drive_file_id = :id
        ORDER BY modified_at DESC LIMIT 1
    """)
    abstract suspend fun getCachedDocument(id: String): IncomingIntelligenceRecord?
}
