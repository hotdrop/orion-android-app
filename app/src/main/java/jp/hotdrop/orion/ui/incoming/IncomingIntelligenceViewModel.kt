package jp.hotdrop.orion.ui.incoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import jp.hotdrop.orion.data.remote.GoogleDriveApiException
import jp.hotdrop.orion.data.remote.GoogleDriveNetworkException
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceRecord
import jp.hotdrop.orion.data.IncomingIntelligenceRepository
import jp.hotdrop.orion.data.SettingsRepository
import jp.hotdrop.orion.model.GoogleDriveTarget
import jp.hotdrop.orion.model.IncomingIntelligenceDocument
import jp.hotdrop.orion.ui.incoming.uistate.IncomingIntelligenceUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class IncomingIntelligenceViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val incomingRepository: IncomingIntelligenceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomingIntelligenceUiState())
    val uiState: StateFlow<IncomingIntelligenceUiState> = _uiState.asStateFlow()

    private var currentTarget: GoogleDriveTarget? = null

    init {
        observeLocalState()
    }

    fun beginSynchronization(): Boolean {
        if (currentTarget == null || _uiState.value.isSyncing) return false
        _uiState.update { it.copy(isSyncing = true, isOffline = false, errorMessage = null) }
        return true
    }

    fun synchronize(accessToken: String) {
        val target = currentTarget
        if (target == null || !_uiState.value.isSyncing) return
        viewModelScope.launch {
            try {
                incomingRepository.synchronize(target.folderId, accessToken)
                _uiState.update { it.copy(isSyncing = false, isOffline = false, errorMessage = null) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: GoogleDriveNetworkException) {
                logFailure("Google Drive synchronization failed due to network", error)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        isOffline = true,
                        errorMessage = null,
                    )
                }
            } catch (error: Exception) {
                logFailure("Google Drive synchronization failed", error)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        isOffline = false,
                        errorMessage = syncErrorMessage(error),
                    )
                }
            }
        }
    }

    fun reportAuthorizationFailure() {
        _uiState.update {
            it.copy(
                isSyncing = false,
                errorMessage = "Google Driveの認証を完了できませんでした。",
            )
        }
    }

    fun markDocumentOpened(documentId: String) {
        val target = currentTarget ?: return
        viewModelScope.launch {
            runCatching { incomingRepository.markOpened(target.folderId, documentId) }
                .onFailure { logFailure("Failed to mark an incoming document as opened", it) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeLocalState() {
        viewModelScope.launch {
            settingsRepository.observeDriveTarget()
                .flatMapLatest { target ->
                    currentTarget = target
                    if (target == null) {
                        flowOf(LocalIncomingState(null, emptyList(), null))
                    } else {
                        combine(
                            incomingRepository.observeDocuments(target.folderId),
                            incomingRepository.observeLastSyncedAt(target.folderId),
                        ) { documents, lastSyncedAt ->
                            LocalIncomingState(target, documents, lastSyncedAt)
                        }
                    }
                }
                .catch { error ->
                    logFailure("Failed to observe incoming intelligence cache", error)
                    _uiState.update {
                        it.copy(errorMessage = "保存済みデータを読み込めませんでした。")
                    }
                }
                .collect { localState ->
                    _uiState.update { current ->
                        current.copy(
                            isDriveConfigured = localState.target != null,
                            documents = localState.documents.map(::toUiDocument),
                            lastSyncedAtLabel = localState.lastSyncedAt?.let(::formatTimestamp),
                        )
                    }
                }
        }
    }

    private fun toUiDocument(record: IncomingIntelligenceRecord) = IncomingIntelligenceDocument(
        id = record.id,
        title = record.title,
        updatedAtLabel = formatTimestamp(record.modifiedAt),
        relativePath = record.relativePath,
        webUrl = record.webUrl,
        isNew = record.isNew,
    )

    private fun syncErrorMessage(error: Exception): String = when {
        error is GoogleDriveApiException && error.responseCode in setOf(401, 403) ->
            "Google Driveへのアクセス権を確認して再試行してください。"
        else -> "Google Driveから取得できませんでした。保存済みデータを表示しています。"
    }

    private fun logFailure(message: String, error: Throwable) {
        Logger.getLogger(IncomingIntelligenceViewModel::class.java.name)
            .log(Level.SEVERE, message, error)
    }

    companion object {
        private val TimestampFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")

        private fun formatTimestamp(timestamp: Long): String =
            Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(TimestampFormatter)
    }
}

private data class LocalIncomingState(
    val target: GoogleDriveTarget?,
    val documents: List<IncomingIntelligenceRecord>,
    val lastSyncedAt: Long?,
)
