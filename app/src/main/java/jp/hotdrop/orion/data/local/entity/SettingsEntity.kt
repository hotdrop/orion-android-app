package jp.hotdrop.orion.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = SingletonId,
    @ColumnInfo(name = "google_drive_path")
    val googleDrivePath: String,
    @ColumnInfo(name = "google_drive_folder_id")
    val googleDriveFolderId: String? = null,
) {
    companion object {
        const val SingletonId = 1
    }
}