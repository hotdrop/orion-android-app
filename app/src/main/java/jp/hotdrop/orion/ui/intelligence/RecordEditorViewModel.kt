package jp.hotdrop.orion.ui.intelligence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.hotdrop.orion.data.SignalFocusRepository
import jp.hotdrop.orion.model.intelligence.extractKeywordCandidates
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 編集中の本文・メモ・改行区切りのKeyword。Signalではメモを使用しない。
 */
data class RecordInput(
    val body: String = "",
    val note: String = "",
    val keywords: String = "",
)

/**
 * 編集内容と比較元を保持し、未保存の変更と操作可否を表す。
 */
data class RecordEditorState(
    val focus: Boolean = false,
    val input: RecordInput = RecordInput(),
    val original: RecordInput = RecordInput(),
    val loading: Boolean = false,
    val busy: Boolean = false,
    val loaded: Boolean = true,
    val error: String? = null,
    val confirmation: String? = null,
    val candidates: List<String> = emptyList(),
) {
    val dirty: Boolean
        get() = input != original
}

/**
 * Signalの新規作成と、既存Signal・Focusの編集および削除を管理する。
 */
@HiltViewModel
class RecordEditorViewModel @Inject constructor(
    private val repository: SignalFocusRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val id: Long? = savedState.get<Long>("recordId")?.takeIf { it > 0 }
    private val focus = savedState.get<String>("kind") == "focus"

    private val mutableState = MutableStateFlow(RecordEditorState(focus = focus, loading = id != null, loaded = id == null))
    val state = mutableState.asStateFlow()

    private val events = Channel<Unit>(Channel.BUFFERED)
    val closed = events.receiveAsFlow()

    init {
        load()
    }

    /**
     * 保存済み記録を読み、復元可能な未保存入力があればその入力を優先する。
     */
    fun load() {
        if (mutableState.value.busy) return

        viewModelScope.launch {
            mutableState.update { it.copy(loading = true, error = null) }
            try {
                val original = when {
                    id == null -> RecordInput()
                    focus -> {
                        val record = requireNotNull(repository.getFocus(id)) { "Focusが見つかりません。" }
                        RecordInput(
                            body = record.analysis,
                            note = record.note,
                            keywords = record.keywords.joinToString("\n"),
                        )
                    }
                    else -> {
                        val record = requireNotNull(repository.getSignal(id)) { "Signalが見つかりません。" }
                        RecordInput(
                            body = record.body,
                            keywords = record.keywords.joinToString("\n"),
                        )
                    }
                }
                val input = RecordInput(
                    body = savedState["body"] ?: original.body,
                    note = savedState["note"] ?: original.note,
                    keywords = savedState["keywords"] ?: original.keywords,
                )
                mutableState.update {
                    it.copy(
                        input = input,
                        original = original,
                        loading = false,
                        loaded = true,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                reportIntelligenceFailure(error)
                mutableState.update {
                    it.copy(
                        loading = false,
                        loaded = false,
                        error = "記録を読み込めませんでした。",
                    )
                }
            }
        }
    }

    /**
     * 入力を画面へ反映し、プロセス再生成時の復元用にも保持する。
     */
    fun change(input: RecordInput) {
        if (state.value.busy || !state.value.loaded) return

        savedState["body"] = input.body
        savedState["note"] = input.note
        savedState["keywords"] = input.keywords
        mutableState.update { it.copy(input = input, error = null) }
    }

    /**
     * 保存済みKeywordと現在の本文から、ユーザーが選択する候補を抽出する。
     */
    fun suggest() = viewModelScope.launch {
        try {
            val known = repository.knownKeywords()
            mutableState.update {
                it.copy(
                    candidates = extractKeywordCandidates(
                        analysis = it.input.body,
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

    /**
     * 未保存の変更がある場合は破棄確認を表示し、なければそのまま閉じる。
     */
    fun back() {
        if (state.value.busy) return

        if (state.value.dirty) {
            confirm("discard")
        } else {
            events.trySend(Unit)
        }
    }

    /**
     * deleteで削除、discardで入力破棄の確認を表示する。nullで確認を閉じる。
     */
    fun confirm(value: String?) {
        mutableState.update { it.copy(confirmation = value) }
    }

    /**
     * 保存済み記録を変更せず、未保存入力を破棄して閉じる。
     */
    fun discard() {
        events.trySend(Unit)
    }

    /**
     * 本文を検証し、記録とKeywordを保存してから閉じる。
     */
    fun save() {
        val current = state.value
        if (current.busy || current.loading || !current.loaded) return

        if (current.input.body.isBlank()) {
            mutableState.update { it.copy(error = "本文を入力してください。") }
            return
        }

        perform {
            if (focus) {
                repository.updateFocus(
                    id = requireNotNull(id),
                    analysis = current.input.body,
                    note = current.input.note,
                    keywords = current.input.keywords,
                )
            } else {
                repository.saveSignal(
                    id = id,
                    body = current.input.body,
                    keywords = current.input.keywords,
                )
            }
        }
    }

    /**
     * 編集対象の記録を削除する。未保存の新規Signalには何もしない。
     */
    fun delete() {
        val recordId = id ?: return
        perform {
            if (focus) {
                repository.deleteFocus(recordId)
            } else {
                repository.deleteSignal(recordId)
            }
        }
    }

    /**
     * 保存・削除中の重複操作を防ぎ、成功時だけ閉じる。失敗時は入力を保持する。
     */
    private fun perform(action: suspend () -> Unit) {
        if (state.value.busy) return

        mutableState.update { it.copy(busy = true, confirmation = null, error = null) }
        viewModelScope.launch {
            try {
                action()
                events.send(Unit)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                reportIntelligenceFailure(error)
                mutableState.update {
                    it.copy(
                        busy = false,
                        error = "保存または削除に失敗しました。入力を保持しています。再試行してください。",
                    )
                }
            }
        }
    }
}
