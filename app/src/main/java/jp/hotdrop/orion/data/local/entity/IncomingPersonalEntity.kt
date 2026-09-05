package jp.hotdrop.orion.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** User-owned values and the last known metadata survive replacement of the Drive listing. */
@Entity(tableName = "incoming_personal_documents")
data class IncomingPersonalEntity(
    @PrimaryKey @ColumnInfo(name = "drive_file_id") val driveFileId: String,
    val title: String,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long,
    @ColumnInfo(name = "relative_path") val relativePath: String,
    @ColumnInfo(name = "web_url") val webUrl: String,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    val memo: String = "",
)
