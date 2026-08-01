package jp.hotdrop.orion.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "incoming_intelligence_documents",
    primaryKeys = ["root_folder_id", "drive_file_id"],
)
data class IncomingIntelligenceEntity(
    @ColumnInfo(name = "root_folder_id")
    val rootFolderId: String,
    @ColumnInfo(name = "drive_file_id")
    val driveFileId: String,
    val title: String,
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long,
    @ColumnInfo(name = "relative_path")
    val relativePath: String,
    @ColumnInfo(name = "web_url")
    val webUrl: String,
    @ColumnInfo(name = "is_new")
    val isNew: Boolean,
)

data class IncomingIntelligenceRecord(
    val id: String,
    val title: String,
    val modifiedAt: Long,
    val relativePath: String,
    val webUrl: String,
    val isNew: Boolean,
)

internal fun IncomingIntelligenceEntity.toRecord() = IncomingIntelligenceRecord(
    id = driveFileId,
    title = title,
    modifiedAt = modifiedAt,
    relativePath = relativePath,
    webUrl = webUrl,
    isNew = isNew,
)

@Entity(tableName = "incoming_intelligence_sync_state")
data class IncomingIntelligenceSyncStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "root_folder_id")
    val rootFolderId: String,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long,
)
