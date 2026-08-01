package jp.hotdrop.orion.ui.archive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import jp.hotdrop.orion.data.KnowledgeArchiveRepository
import jp.hotdrop.orion.model.KnowledgeArchiveDraft
import jp.hotdrop.orion.model.KnowledgeArchiveEditorFeedback
import jp.hotdrop.orion.model.KnowledgeArchiveValidationError
import jp.hotdrop.orion.navigation.OrionDestination
import jp.hotdrop.orion.ui.archive.uistate.KnowledgeArchiveEditorUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URI

@HiltViewModel
class KnowledgeArchiveEditorViewModel @Inject constructor(
    private val repository: KnowledgeArchiveRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val entryId: Long? = savedStateHandle[OrionDestination.ArchiveEntryIdArgument]
    private val _uiState = MutableStateFlow(
        KnowledgeArchiveEditorUiState(
            entryId = entryId,
            isLoading = entryId != null,
        ),
    )
    val uiState: StateFlow<KnowledgeArchiveEditorUiState> = _uiState.asStateFlow()

    private val _events = Channel<KnowledgeArchiveEditorEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (entryId != null) loadEntry(entryId)
    }

    fun onTitleChanged(title: String) = updateDraft { copy(title = title) }

    fun onUrlChanged(url: String) = updateDraft { copy(url = url) }

    fun onMemoChanged(memo: String) = updateDraft { copy(memo = memo) }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return

        val error = validateKnowledgeArchiveDraft(state.draft.normalized())
        if (error != null) {
            _uiState.update { it.copy(validationError = error) }
            return
        }

        _uiState.update {
            it.copy(
                isSaving = true,
                validationError = null,
                feedback = KnowledgeArchiveEditorFeedback.None,
            )
        }
        viewModelScope.launch {
            try {
                repository.saveEntry(entryId, state.draft)
                _events.send(KnowledgeArchiveEditorEvent.Close)
            } catch (error: Exception) {
                logFailure("Failed to save Knowledge Archive entry", error)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        feedback = KnowledgeArchiveEditorFeedback.SaveFailed,
                    )
                }
            }
        }
    }

    fun requestBack() {
        if (_uiState.value.isDirty) {
            _uiState.update { it.copy(showDiscardConfirmation = true) }
        } else {
            _events.trySend(KnowledgeArchiveEditorEvent.Close)
        }
    }

    fun dismissDiscardConfirmation() {
        _uiState.update { it.copy(showDiscardConfirmation = false) }
    }

    fun discardAndClose() {
        _uiState.update { it.copy(showDiscardConfirmation = false) }
        _events.trySend(KnowledgeArchiveEditorEvent.Close)
    }

    fun requestDelete() {
        if (entryId != null && !_uiState.value.isSaving && !_uiState.value.isDeleting) {
            _uiState.update { it.copy(showDeleteConfirmation = true) }
        }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun confirmDelete() {
        val id = entryId ?: return
        val state = _uiState.value
        if (state.isSaving || state.isDeleting) return

        _uiState.update {
            it.copy(
                isDeleting = true,
                showDeleteConfirmation = false,
                feedback = KnowledgeArchiveEditorFeedback.None,
            )
        }
        viewModelScope.launch {
            try {
                repository.deleteEntry(id)
                _events.send(KnowledgeArchiveEditorEvent.Close)
            } catch (error: Exception) {
                logFailure("Failed to delete Knowledge Archive entry", error)
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        feedback = KnowledgeArchiveEditorFeedback.DeleteFailed,
                    )
                }
            }
        }
    }

    private fun validateKnowledgeArchiveDraft(
        draft: KnowledgeArchiveDraft,
    ): KnowledgeArchiveValidationError? = when {
        draft.title.isBlank() -> KnowledgeArchiveValidationError.TitleRequired
        draft.url.isBlank() -> KnowledgeArchiveValidationError.UrlRequired
        !draft.url.isHttpUrl() -> KnowledgeArchiveValidationError.UrlInvalid
        else -> null
    }

    private fun String.isHttpUrl(): Boolean = runCatching {
        val uri = URI(trim())
        (uri.scheme.equals("http", ignoreCase = true) ||
                uri.scheme.equals("https", ignoreCase = true)) &&
                !uri.host.isNullOrBlank()
    }.getOrDefault(false)

    private fun loadEntry(id: Long) {
        viewModelScope.launch {
            try {
                val entry = requireNotNull(repository.getEntry(id))
                val draft = KnowledgeArchiveDraft(
                    title = entry.title,
                    url = entry.url,
                    memo = entry.memo,
                )
                _uiState.update {
                    it.copy(
                        title = draft.title,
                        url = draft.url,
                        memo = draft.memo,
                        originalDraft = draft,
                        isLoading = false,
                    )
                }
            } catch (error: Exception) {
                logFailure("Failed to load Knowledge Archive entry", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        feedback = KnowledgeArchiveEditorFeedback.LoadFailed,
                    )
                }
            }
        }
    }

    private fun updateDraft(transform: KnowledgeArchiveEditorUiState.() -> KnowledgeArchiveEditorUiState) {
        _uiState.update {
            it.transform().copy(
                validationError = null,
                feedback = KnowledgeArchiveEditorFeedback.None,
            )
        }
    }

    private fun logFailure(message: String, error: Exception) {
        Logger.getLogger(KnowledgeArchiveEditorViewModel::class.java.name).log(
            Level.SEVERE,
            message,
            error,
        )
    }
}

sealed interface KnowledgeArchiveEditorEvent {
    data object Close : KnowledgeArchiveEditorEvent
}
