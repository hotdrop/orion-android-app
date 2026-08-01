package jp.hotdrop.orion.data.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = SingletonId,
    @ColumnInfo(name = "google_drive_path")
    val googleDrivePath: String,
) {
    companion object {
        const val SingletonId = 1
    }
}
