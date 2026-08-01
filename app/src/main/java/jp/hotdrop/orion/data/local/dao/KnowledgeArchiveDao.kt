package jp.hotdrop.orion.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import jp.hotdrop.orion.data.local.entity.KnowledgeArchiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeArchiveDao {
    @Query("SELECT * FROM knowledge_archive_entries ORDER BY updated_at DESC, id DESC")
    fun observeAll(): Flow<List<KnowledgeArchiveEntity>>

    @Query("SELECT * FROM knowledge_archive_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): KnowledgeArchiveEntity?

    @Insert
    suspend fun insert(entry: KnowledgeArchiveEntity): Long

    @Update
    suspend fun update(entry: KnowledgeArchiveEntity): Int

    @Delete
    suspend fun delete(entry: KnowledgeArchiveEntity): Int
}