package jp.hotdrop.orion.data

import androidx.room.withTransaction
import jp.hotdrop.orion.data.local.OrionDatabase
import jp.hotdrop.orion.data.local.entity.FocusCheckpointEntity
import jp.hotdrop.orion.data.local.entity.FocusDraftEntity
import jp.hotdrop.orion.data.local.entity.FocusEntity
import jp.hotdrop.orion.data.local.entity.FocusKeywordEntity
import jp.hotdrop.orion.data.local.entity.SignalEntity
import jp.hotdrop.orion.data.local.entity.SignalKeywordEntity
import jp.hotdrop.orion.model.intelligence.AnalysisDraft
import jp.hotdrop.orion.model.intelligence.FocusRecord
import jp.hotdrop.orion.model.intelligence.IntelligenceArchive
import jp.hotdrop.orion.model.intelligence.SignalRecord
import jp.hotdrop.orion.model.intelligence.buildFocusPrompt
import jp.hotdrop.orion.model.intelligence.parseKeywords
import jp.hotdrop.orion.model.intelligence.requireRecordBody
import kotlinx.coroutines.flow.combine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Signal・FocusとKeyword、再開可能な分析下書きの保存を調停する。
 * 記録とKeywordの更新、分析の確定はトランザクション内でまとめて行う。
 */
@Singleton
class SignalFocusRepository internal constructor(
    private val database: OrionDatabase,
    private val now: () -> Long,
) {
    @Inject
    constructor(database: OrionDatabase) : this(database, System::currentTimeMillis)

    private val dao = database.signalFocusDao()

    private val records = combine(
        dao.observeSignals(),
        dao.observeSignalKeywords(),
        dao.observeFocuses(),
        dao.observeFocusKeywords(),
    ) { signals, signalKeys, focuses, focusKeys ->
        val signalKeywords = signalKeys.groupBy { it.signalId }
        val focusKeywords = focusKeys.groupBy { it.focusId }
        IntelligenceArchive(
            signals = signals.map { signal ->
                val keywords = signalKeywords[signal.id].orEmpty().map { it.display }
                signal.toRecord(keywords)
            },
            focuses = focuses.map { focus ->
                val keywords = focusKeywords[focus.id].orEmpty().map { it.display }
                focus.toRecord(keywords)
            },
        )
    }

    /**
     * 保存済み記録、Keywordで結ばれた関連、再開できる下書きの有無を監視する。
     */
    val archive = combine(
        records,
        dao.observeLinks(),
        dao.observeDraft(),
    ) { records, links, draft ->
        records.copy(links = links, hasDraft = draft != null)
    }

    /**
     * 本文とKeywordを同一トランザクションで取得する。削除済みならnullを返す。
     */
    suspend fun getSignal(id: Long): SignalRecord? = database.withTransaction {
        dao.signal(id)?.toRecord(dao.signalKeywords(id).map { it.display })
    }

    /**
     * 分析本文・メモとKeywordを取得する。削除済みならnullを返す。
     */
    suspend fun getFocus(id: Long): FocusRecord? = database.withTransaction {
        dao.focus(id)?.toRecord(dao.focusKeywords(id).map { it.display })
    }

    /**
     * Signalを作成または更新する。更新時は作成時刻を維持し、Keywordを置き換える。
     */
    suspend fun saveSignal(
        id: Long?,
        body: String,
        keywords: String,
    ): Long = database.withTransaction {
        requireRecordBody(body)
        val time = now()
        val savedId = if (id == null) {
            dao.insertSignal(
                SignalEntity(
                    body = body.trim(),
                    createdAt = time,
                    updatedAt = time,
                ),
            )
        } else {
            val old = requireNotNull(dao.signal(id)) { "Signalが見つかりません。" }
            check(dao.updateSignal(old.copy(body = body.trim(), updatedAt = time)) == 1)
            id
        }
        val keywordEntities = parseKeywords(keywords).mapIndexed { index, keyword ->
            SignalKeywordEntity(
                signalId = savedId,
                normalized = keyword.normalized,
                display = keyword.display,
                position = index,
            )
        }
        dao.clearSignalKeywords(savedId)
        dao.insertSignalKeywords(keywordEntities)
        savedId
    }

    /**
     * Focusの生成時刻を維持して、分析本文・メモ・Keywordを更新する。
     */
    suspend fun updateFocus(
        id: Long,
        analysis: String,
        note: String,
        keywords: String,
    ) = database.withTransaction {
        requireRecordBody(analysis)
        val old = requireNotNull(dao.focus(id)) { "Focusが見つかりません。" }
        val updated = old.copy(
            analysis = analysis.trim(),
            note = note,
            updatedAt = now(),
        )
        check(dao.updateFocus(updated) == 1)
        replaceFocusKeywords(id, keywords)
    }

    /**
     * Signalを削除する。関連Keywordは外部キー制約によって削除される。
     */
    suspend fun deleteSignal(id: Long) {
        check(dao.deleteSignal(id) == 1) { "Signalが見つかりません。" }
    }

    /**
     * Focusを削除する。分析済み範囲のチェックポイントは維持する。
     */
    suspend fun deleteFocus(id: Long) {
        check(dao.deleteFocus(id) == 1) { "Focusが見つかりません。" }
    }

    /**
     * 既存下書きを再開する。なければ前回確定したIDより後のSignalで下書きを作る。
     * 時刻の巻き戻りによる取りこぼしを避けるため、対象範囲はIDで判定する。
     * プロンプトは開始時点で固定し、対象が空なら下書きを作らずnullを返す。
     */
    suspend fun beginAnalysis(): AnalysisDraft? = database.withTransaction {
        dao.draft()?.let { return@withTransaction it.toModel() }

        val afterId = dao.checkpoint()?.upperSignalId ?: 0
        val signals = dao.signalsAfter(afterId).map { signal ->
            val keywords = dao.signalKeywords(signal.id).map { it.display }
            signal.toRecord(keywords)
        }
        if (signals.isEmpty()) return@withTransaction null

        val draft = FocusDraftEntity(
            token = UUID.randomUUID().toString(),
            startedAt = now(),
            upperSignalId = signals.maxOf { it.id },
            signalCount = signals.size,
            prompt = buildFocusPrompt(signals),
        )
        dao.insertDraft(draft)
        draft.toModel()
    }

    /**
     * 編集可能な項目だけを更新する。確定・破棄済みの下書きを復活させない。
     */
    suspend fun saveDraft(draft: AnalysisDraft) {
        val updatedRows = dao.updateDraft(
            token = draft.token,
            analysis = draft.analysis,
            note = draft.note,
            keywords = draft.keywords,
            stage = draft.stage,
        )
        check(updatedRows == 1) { "分析下書きが見つかりません。画面を開き直してください。" }
    }

    /**
     * 指定の下書きを削除する。Signalと分析済み範囲は変更しない。
     */
    suspend fun discardDraft(token: String) {
        dao.deleteDraft(token)
    }

    /**
     * 候補抽出に使う、SignalとFocusに保存されたKeywordの表示名を返す。
     */
    suspend fun knownKeywords(): List<String> = dao.knownKeywords()

    /**
     * Focusの作成、分析済み範囲の更新、下書き削除を一度に確定する。
     * トークンの確認と削除を同じトランザクションで行い、同時送信による二重保存を防ぐ。
     * 途中で失敗した場合は下書きとチェックポイントを変更前の状態に戻す。
     */
    suspend fun completeAnalysis(draft: AnalysisDraft): Long = database.withTransaction {
        requireRecordBody(draft.analysis)
        val stored = requireNotNull(dao.draft()?.takeIf { it.token == draft.token }) { "この分析は既に保存または破棄されています。" }
        val id = dao.insertFocus(
            FocusEntity(
                analysis = draft.analysis.trim(),
                note = draft.note,
                generatedAt = stored.startedAt,
                updatedAt = now(),
            ),
        )
        replaceFocusKeywords(id, draft.keywords)
        dao.saveCheckpoint(
            FocusCheckpointEntity(
                upperSignalId = stored.upperSignalId,
                startedAt = stored.startedAt,
            ),
        )
        check(dao.deleteDraft(stored.token) == 1)
        id
    }

    /**
     * 呼び出し元のトランザクション内でKeywordを入力順に置き換える。
     */
    private suspend fun replaceFocusKeywords(id: Long, keywords: String) {
        val keywordEntities = parseKeywords(keywords).mapIndexed { index, keyword ->
            FocusKeywordEntity(
                focusId = id,
                normalized = keyword.normalized,
                display = keyword.display,
                position = index,
            )
        }
        dao.clearFocusKeywords(id)
        dao.insertFocusKeywords(keywordEntities)
    }

    private fun FocusDraftEntity.toModel() = AnalysisDraft(
        token = token,
        startedAt = startedAt,
        upperSignalId = upperSignalId,
        signalCount = signalCount,
        prompt = prompt,
        analysis = analysis,
        note = note,
        keywords = keywords,
        stage = stage,
    )

    private fun SignalEntity.toRecord(keywords: List<String>) = SignalRecord(
        id = id,
        body = body,
        createdAt = createdAt,
        updatedAt = updatedAt,
        keywords = keywords,
    )

    private fun FocusEntity.toRecord(keywords: List<String>) = FocusRecord(
        id = id,
        analysis = analysis,
        note = note,
        generatedAt = generatedAt,
        updatedAt = updatedAt,
        keywords = keywords,
    )
}
