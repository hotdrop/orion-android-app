package jp.hotdrop.orion.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import jp.hotdrop.orion.data.KnowledgeArchiveRepository
import jp.hotdrop.orion.ui.archive.uistate.KnowledgeArchiveUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class KnowledgeArchiveViewModel @Inject constructor(
    private val repository: KnowledgeArchiveRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(KnowledgeArchiveUiState())
    val uiState: StateFlow<KnowledgeArchiveUiState> = _uiState.asStateFlow()

    init {
        observeEntries()
    }

    fun reportUrlOpenFailure() {
        _uiState.update { it.copy(urlOpenFailed = true) }
    }

    fun clearUrlOpenFailure() {
        _uiState.update { it.copy(urlOpenFailed = false) }
    }

    private fun observeEntries() {
        viewModelScope.launch {
            repository.observeEntries()
                .catch { error ->
                    Logger.getLogger(KnowledgeArchiveViewModel::class.java.name).log(
                        Level.SEVERE,
                        "Failed to load Knowledge Archive entries",
                        error,
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadFailed = true,
                        )
                    }
                }
                .collect { entries ->
                    _uiState.update {
                        it.copy(
                            entries = entries,
                            isLoading = false,
                            loadFailed = false,
                        )
                    }
                }
        }
    }
}
