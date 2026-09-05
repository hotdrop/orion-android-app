package jp.hotdrop.orion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceEntity
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceSyncStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomingIntelligenceDao {
    @Query(
        "SELECT * FROM incoming_intelligence_documents " +
            "WHERE root_folder_id = :rootFolderId ORDER BY modified_at DESC, title ASC",
    )
    fun observeDocuments(rootFolderId: String): Flow<List<IncomingIntelligenceEntity>>

    @Query("SELECT * FROM incoming_intelligence_documents WHERE root_folder_id = :rootFolderId")
    suspend fun getDocuments(rootFolderId: String): List<IncomingIntelligenceEntity>

    @Query("SELECT last_synced_at FROM incoming_intelligence_sync_state WHERE root_folder_id = :rootFolderId")
    fun observeLastSyncedAt(rootFolderId: String): Flow<Long?>

    @Insert
    suspend fun insertAll(documents: List<IncomingIntelligenceEntity>)

    @Upsert
    suspend fun upsertSyncState(syncState: IncomingIntelligenceSyncStateEntity)

    @Query("DELETE FROM incoming_intelligence_documents WHERE root_folder_id = :rootFolderId")
    suspend fun deleteForRoot(rootFolderId: String)

    @Query(
        "UPDATE incoming_intelligence_documents SET is_new = 0 " +
            "WHERE root_folder_id = :rootFolderId AND drive_file_id = :driveFileId",
    )
    suspend fun markOpened(rootFolderId: String, driveFileId: String)

    @Query("""
        UPDATE incoming_personal_documents SET
            title = (SELECT title FROM incoming_intelligence_documents WHERE root_folder_id = :rootFolderId AND drive_file_id = incoming_personal_documents.drive_file_id),
            modified_at = (SELECT modified_at FROM incoming_intelligence_documents WHERE root_folder_id = :rootFolderId AND drive_file_id = incoming_personal_documents.drive_file_id),
            relative_path = (SELECT relative_path FROM incoming_intelligence_documents WHERE root_folder_id = :rootFolderId AND drive_file_id = incoming_personal_documents.drive_file_id),
            web_url = (SELECT web_url FROM incoming_intelligence_documents WHERE root_folder_id = :rootFolderId AND drive_file_id = incoming_personal_documents.drive_file_id)
        WHERE drive_file_id IN (SELECT drive_file_id FROM incoming_intelligence_documents WHERE root_folder_id = :rootFolderId)
    """)
    suspend fun refreshPersonalSnapshots(rootFolderId: String)

    @Transaction
    suspend fun replaceForRoot(
        rootFolderId: String,
        documents: List<IncomingIntelligenceEntity>,
        syncedAt: Long,
    ) {
        deleteForRoot(rootFolderId)
        if (documents.isNotEmpty()) insertAll(documents)
        refreshPersonalSnapshots(rootFolderId)
        upsertSyncState(IncomingIntelligenceSyncStateEntity(rootFolderId, syncedAt))
    }
}