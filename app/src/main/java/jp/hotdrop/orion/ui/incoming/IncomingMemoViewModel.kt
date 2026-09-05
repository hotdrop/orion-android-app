package jp.hotdrop.orion.ui.incoming

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.hotdrop.orion.data.IncomingPersonalRepository
import jp.hotdrop.orion.navigation.OrionDestination
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.logging.Level
import java.util.logging.Logger

data class IncomingMemoUiState(
    val title: String = "",
    val memo: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,
    val showDiscard: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class IncomingMemoViewModel @Inject constructor(
    private val repository: IncomingPersonalRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val documentId: String = checkNotNull(savedStateHandle[OrionDestination.INCOMING_DOCUMENT_ID])
    private var originalMemo = ""
    private val state = MutableStateFlow(IncomingMemoUiState())
    val uiState = state.asStateFlow()
    private val closeEvents = Channel<Unit>(Channel.BUFFERED)
    val events = closeEvents.receiveAsFlow()

    init { load() }

    fun load() {
        state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val document = repository.prepareMemo(documentId)
                if (document == null) {
                    state.update { it.copy(isLoading = false, error = "資料が見つかりません。") }
                } else {
                    originalMemo = savedStateHandle["originalMemo"] ?: document.memo
                    savedStateHandle["originalMemo"] = originalMemo
                    state.update { it.copy(title = document.title, memo = savedStateHandle["memo"] ?: document.memo,
                        isLoading = false, canSave = true) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                reportFailure("メモを読み込めませんでした。再試行してください。", error)
                state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun changeMemo(memo: String) {
        if (!state.value.canSave || state.value.isSaving) return
        savedStateHandle["memo"] = memo
        state.update { it.copy(memo = memo) }
    }

    fun save() {
        val current = state.value
        if (!current.canSave || current.isSaving) return
        state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                repository.saveMemo(documentId, current.memo)
                closeEvents.send(Unit)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                reportFailure("メモを保存できませんでした。再試行してください。", error)
            } finally {
                state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun requestBack() {
        if (state.value.isSaving) return
        if (state.value.memo != originalMemo) state.update { it.copy(showDiscard = true) }
        else close()
    }

    fun dismissDiscard() { state.update { it.copy(showDiscard = false) } }
    fun close() { viewModelScope.launch { closeEvents.send(Unit) } }

    private fun reportFailure(message: String, error: Exception) {
        Logger.getLogger(IncomingMemoViewModel::class.java.name).log(Level.WARNING, message, error)
        state.update { it.copy(error = message) }
    }
}
