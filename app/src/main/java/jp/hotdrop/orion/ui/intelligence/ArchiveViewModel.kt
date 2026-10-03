package jp.hotdrop.orion.ui.intelligence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.hotdrop.orion.data.SignalFocusRepository
import jp.hotdrop.orion.model.intelligence.IntelligenceArchive
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 一覧・詳細で表示する記録と読み込み状態。
 */
data class ArchiveUiState(
    val archive: IntelligenceArchive = IntelligenceArchive(),
    val loading: Boolean = true,
    val error: String? = null,
)

/**
 * ローカルの記録と関連を監視し、一覧・詳細画面へ公開する。
 */
@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: SignalFocusRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ArchiveUiState())
    val state = mutableState.asStateFlow()

    private var observation: Job? = null

    init {
        reload()
    }

    /**
     * 既存の監視を停止して再開する。読み込み失敗からの再試行にも使用する。
     */
    fun reload() {
        observation?.cancel()
        mutableState.update { it.copy(loading = true, error = null) }
        observation = viewModelScope.launch {
            try {
                repository.archive.collect { archive ->
                    mutableState.value = ArchiveUiState(
                        archive = archive,
                        loading = false,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                reportIntelligenceFailure(error)
                mutableState.update {
                    it.copy(
                        loading = false,
                        error = "記録を読み込めませんでした。再試行してください。",
                    )
                }
            }
        }
    }
}
