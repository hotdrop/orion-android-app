package jp.hotdrop.orion.data.settings

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT google_drive_path FROM settings WHERE id = :id LIMIT 1")
    fun observeGoogleDrivePath(id: Int = SettingsEntity.SingletonId): Flow<String?>

    @Upsert
    suspend fun upsert(settings: SettingsEntity)

    @Query("DELETE FROM settings WHERE id = :id")
    suspend fun delete(id: Int = SettingsEntity.SingletonId)
}
