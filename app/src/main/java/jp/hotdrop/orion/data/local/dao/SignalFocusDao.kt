package jp.hotdrop.orion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import jp.hotdrop.orion.data.local.entity.FocusCheckpointEntity
import jp.hotdrop.orion.data.local.entity.FocusDraftEntity
import jp.hotdrop.orion.data.local.entity.FocusEntity
import jp.hotdrop.orion.data.local.entity.FocusKeywordEntity
import jp.hotdrop.orion.data.local.entity.SignalEntity
import jp.hotdrop.orion.data.local.entity.SignalKeywordEntity
import jp.hotdrop.orion.model.intelligence.RelatedFocus
import kotlinx.coroutines.flow.Flow

/** Signal・FocusとKeyword、単一の分析下書き・チェックポイントへアクセスする。 */
@Dao
interface SignalFocusDao {
    @Query("SELECT * FROM signals ORDER BY createdAt DESC, id DESC")
    fun observeSignals(): Flow<List<SignalEntity>>

    @Query("SELECT * FROM focuses ORDER BY generatedAt DESC, id DESC")
    fun observeFocuses(): Flow<List<FocusEntity>>

    @Query("SELECT * FROM signal_keywords ORDER BY position")
    fun observeSignalKeywords(): Flow<List<SignalKeywordEntity>>

    @Query("SELECT * FROM focus_keywords ORDER BY position")
    fun observeFocusKeywords(): Flow<List<FocusKeywordEntity>>

    /** 共有Keywordが複数あっても、記録の組み合わせは一件にまとめる。 */
    @Query(
        """
        SELECT DISTINCT s.signalId, f.focusId, r.generatedAt
        FROM signal_keywords s
        JOIN focus_keywords f ON s.normalized = f.normalized
        JOIN focuses r ON r.id = f.focusId
        ORDER BY r.generatedAt DESC, f.focusId DESC
        """,
    )
    fun observeLinks(): Flow<List<RelatedFocus>>

    @Query("SELECT * FROM focus_draft WHERE id = 1")
    fun observeDraft(): Flow<FocusDraftEntity?>

    @Query("SELECT * FROM signals WHERE id = :id")
    suspend fun signal(id: Long): SignalEntity?

    @Query("SELECT * FROM focuses WHERE id = :id")
    suspend fun focus(id: Long): FocusEntity?

    @Query("SELECT * FROM signal_keywords WHERE signalId = :id ORDER BY position")
    suspend fun signalKeywords(id: Long): List<SignalKeywordEntity>

    @Query("SELECT * FROM focus_keywords WHERE focusId = :id ORDER BY position")
    suspend fun focusKeywords(id: Long): List<FocusKeywordEntity>

    @Query("SELECT * FROM signals WHERE id > :afterId ORDER BY id")
    suspend fun signalsAfter(afterId: Long): List<SignalEntity>

    @Query("SELECT display FROM signal_keywords UNION SELECT display FROM focus_keywords")
    suspend fun knownKeywords(): List<String>

    @Query("SELECT * FROM focus_draft WHERE id = 1")
    suspend fun draft(): FocusDraftEntity?

    @Query("SELECT * FROM focus_checkpoint WHERE id = 1")
    suspend fun checkpoint(): FocusCheckpointEntity?

    @Insert
    suspend fun insertSignal(value: SignalEntity): Long

    @Update
    suspend fun updateSignal(value: SignalEntity): Int

    @Insert
    suspend fun insertFocus(value: FocusEntity): Long

    @Update
    suspend fun updateFocus(value: FocusEntity): Int

    @Insert
    suspend fun insertSignalKeywords(values: List<SignalKeywordEntity>)

    @Insert
    suspend fun insertFocusKeywords(values: List<FocusKeywordEntity>)

    @Query("DELETE FROM signal_keywords WHERE signalId = :id")
    suspend fun clearSignalKeywords(id: Long)

    @Query("DELETE FROM focus_keywords WHERE focusId = :id")
    suspend fun clearFocusKeywords(id: Long)

    @Query("DELETE FROM signals WHERE id = :id")
    suspend fun deleteSignal(id: Long): Int

    @Query("DELETE FROM focuses WHERE id = :id")
    suspend fun deleteFocus(id: Long): Int

    @Insert
    suspend fun insertDraft(value: FocusDraftEntity)

    /** トークンが一致する下書きの編集項目のみを更新し、更新件数を返す。 */
    @Query(
        """
        UPDATE focus_draft
        SET analysis = :analysis, note = :note, keywords = :keywords, stage = :stage
        WHERE token = :token
        """,
    )
    suspend fun updateDraft(
        token: String,
        analysis: String,
        note: String,
        keywords: String,
        stage: Int,
    ): Int

    @Query("DELETE FROM focus_draft WHERE token = :token")
    suspend fun deleteDraft(token: String): Int

    @Upsert
    suspend fun saveCheckpoint(value: FocusCheckpointEntity)
}
