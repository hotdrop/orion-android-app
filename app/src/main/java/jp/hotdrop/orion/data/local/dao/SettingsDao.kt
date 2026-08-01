package jp.hotdrop.orion.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jp.hotdrop.orion.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = :id LIMIT 1")
    fun observeSettings(id: Int = SettingsEntity.SingletonId): Flow<SettingsEntity?>

    @Upsert
    suspend fun upsert(settings: SettingsEntity)

    @Query("DELETE FROM settings WHERE id = :id")
    suspend fun delete(id: Int = SettingsEntity.SingletonId)
}