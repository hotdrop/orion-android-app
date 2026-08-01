package jp.hotdrop.orion.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.util.logging.Level
import java.util.logging.Logger
import jp.hotdrop.orion.data.archive.KnowledgeArchiveEntry
import jp.hotdrop.orion.data.archive.KnowledgeArchiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KnowledgeArchiveUiState(
    val entries: List<KnowledgeArchiveEntry> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val urlOpenFailed: Boolean = false,
)

class KnowledgeArchiveViewModel(
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

    companion object {
        fun factory(repository: KnowledgeArchiveRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { KnowledgeArchiveViewModel(repository) }
            }
    }
}
