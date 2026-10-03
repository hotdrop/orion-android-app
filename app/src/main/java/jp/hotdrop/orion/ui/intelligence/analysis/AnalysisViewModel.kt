package jp.hotdrop.orion.ui.intelligence.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.hotdrop.orion.data.SignalFocusRepository
import jp.hotdrop.orion.model.intelligence.AnalysisDraft
import jp.hotdrop.orion.model.intelligence.extractKeywordCandidates
import jp.hotdrop.orion.ui.intelligence.reportIntelligenceFailure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 分析下書きと、読み込み・自動保存・終了操作の表示状態。
 */
data class AnalysisState(
    val draft: AnalysisDraft? = null,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val savingDraft: Boolean = false,
    val error: String? = null,
    val confirmation: Boolean = false,
    val candidates: List<String> = emptyList(),
)

/**
 * 分析下書きを復元し、編集の自動保存と分析の確定・破棄を管理する。
 */
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val repository: SignalFocusRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AnalysisState())
    val state = mutableState.asStateFlow()

    private val events = Channel<Long?>(Channel.BUFFERED)

    /** 保存したFocusのIDを通知する。下書き保存や破棄による終了ではnullを通知する。 */
    val closed = events.receiveAsFlow()

    private val writes = Mutex()
    private var finished = false
    private var candidateJob: Job? = null
    private var editRevision = 0L

    init {
        load()
    }

    /**
     * 保存済み下書きを再開し、なければ未分析のSignalから下書きを作る。
     */
    fun load() {
        if (state.value.busy) return

        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val draft = repository.beginAnalysis()
                mutableState.update { it.copy(draft = draft, loading = false) }
                refreshCandidates()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                reportIntelligenceFailure(error)
                mutableState.update {
                    it.copy(
                        loading = false,
                        error = "分析を開始できませんでした。再試行してください。",
                    )
                }
            }
        }
    }

    /**
     * 入力を即座に反映して保存する。本文の変更時だけ候補抽出を遅延実行する。
     */
    fun change(body: String, note: String, keywords: String) {
        if (state.value.busy) return

        val bodyChanged = body != state.value.draft?.analysis
        editRevision++
        mutableState.update {
            it.copy(
                draft = it.draft?.copy(
                    analysis = body,
                    note = note,
                    keywords = keywords,
                ),
                error = null,
            )
        }
        if (bodyChanged) {
            // 入力が落ち着いてから抽出
            refreshCandidates(wait = 350.milliseconds)
        }
        persist()
    }

    /**
     * プロンプト表示と結果入力の切り替え位置を下書きに保存する。
     */
    fun stage(value: Int) {
        if (state.value.busy) return

        editRevision++
        mutableState.update { it.copy(draft = it.draft?.copy(stage = value)) }
        persist()
    }

    /**
     * 書き込みを直列化し、ロック取得後の最新下書きを保存する。
     * 保存中に次の編集があった場合は、その保存が終わるまで保存中表示を維持する。
     */
    private fun persist() {
        mutableState.update { it.copy(savingDraft = true) }
        viewModelScope.launch {
            writes.withLock {
                if (finished) return@withLock

                val draft = state.value.draft ?: return@withLock
                val revision = editRevision
                try {
                    repository.saveDraft(draft)
                    if (revision == editRevision) {
                        mutableState.update { it.copy(savingDraft = false, error = null) }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    reportIntelligenceFailure(error)
                    mutableState.update {
                        it.copy(
                            savingDraft = false,
                            error = "下書きを保存できませんでした。再試行するまでこの画面を閉じないでください。",
                        )
                    }
                }
            }
        }
    }

    /**
     * 現在の入力を保持したまま、自動保存の失敗を再試行する。
     */
    fun retryDraft() {
        persist()
    }

    /**
     * 入力待ち時間を設けずにKeyword候補を再取得する。
     */
    fun suggest() {
        refreshCandidates()
    }

    /**
     * 先行する候補取得を取り消し、最新の分析本文から候補を抽出する。
     */
    private fun refreshCandidates(wait: Duration = Duration.ZERO) {
        candidateJob?.cancel()
        candidateJob = viewModelScope.launch {
            // 連続入力中のDB取得・候補抽出を抑える
            delay(wait)
            try {
                val known = repository.knownKeywords()
                mutableState.update {
                    it.copy(
                        candidates = extractKeywordCandidates(
                            analysis = it.draft?.analysis.orEmpty(),
                            knownKeywords = known,
                        ),
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                reportIntelligenceFailure(error)
                mutableState.update { it.copy(error = "候補を取得できませんでした。") }
            }
        }
    }

    /**
     * 下書き破棄の確認を表示・解除する。
     */
    fun confirmDiscard(value: Boolean) {
        mutableState.update { it.copy(confirmation = value) }
    }

    /**
     * 最新の下書きを保存して閉じる。保存に失敗した場合は画面を維持する。
     */
    fun back() {
        finishOperation { draft ->
            if (draft != null) {
                repository.saveDraft(draft)
            }
            null
        }
    }

    /**
     * 下書きだけを破棄して閉じる。分析済み範囲は更新しない。
     */
    fun discard() {
        finishOperation { draft ->
            if (draft != null) {
                repository.discardDraft(draft.token)
            }
            null
        }
    }

    /**
     * 分析本文を検証し、Focusとして保存したIDを終了イベントで通知する。
     */
    fun save() {
        val draft = state.value.draft ?: return
        if (draft.analysis.isBlank()) {
            mutableState.update { it.copy(error = "ChatGPT Analysisを入力してください。") }
            return
        }

        finishOperation { repository.completeAnalysis(requireNotNull(it)) }
    }

    /**
     * 終了を伴う保存・破棄を自動保存と同じロックで直列化する。
     * 成功後は待機中の自動保存を無効にし、失敗時は入力を残して再操作を許可する。
     */
    private fun finishOperation(operation: suspend (AnalysisDraft?) -> Long?) {
        if (state.value.busy || state.value.loading || finished) return

        mutableState.update { it.copy(busy = true, confirmation = false, error = null) }
        viewModelScope.launch {
            writes.withLock {
                try {
                    val result = operation(state.value.draft)
                    finished = true
                    events.send(result)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    reportIntelligenceFailure(error)
                    mutableState.update {
                        it.copy(
                            busy = false,
                            error = "保存または破棄に失敗しました。入力を保持しています。再試行してください。",
                        )
                    }
                }
            }
        }
    }
}
