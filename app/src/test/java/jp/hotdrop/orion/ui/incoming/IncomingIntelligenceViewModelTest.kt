package jp.hotdrop.orion.ui.incoming

import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceRecord
import jp.hotdrop.orion.data.IncomingIntelligenceRepository
import jp.hotdrop.orion.data.SettingsRepository
import jp.hotdrop.orion.model.GoogleDriveTarget
import jp.hotdrop.orion.ui.settings.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IncomingIntelligenceViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun observingCache_doesNotSynchronizeUntilManualRequest() = runTest {
        val incomingRepository = FakeIncomingRepository()
        val viewModel = IncomingIntelligenceViewModel(
            settingsRepository = FakeIncomingSettingsRepository(
                GoogleDriveTarget("folder-1", "Incoming"),
            ),
            incomingRepository = incomingRepository,
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDriveConfigured)
        assertEquals(0, incomingRepository.synchronizeCalls)

        assertTrue(viewModel.beginSynchronization())
        viewModel.synchronize("access-token")
        advanceUntilIdle()

        assertEquals(1, incomingRepository.synchronizeCalls)
        assertEquals("access-token", incomingRepository.lastAccessToken)
        assertFalse(viewModel.uiState.value.isSyncing)
    }

    @Test
    fun synchronizationCannotStartWithoutConfiguredFolder() = runTest {
        val incomingRepository = FakeIncomingRepository()
        val viewModel = IncomingIntelligenceViewModel(
            FakeIncomingSettingsRepository(null),
            incomingRepository,
        )
        advanceUntilIdle()

        assertFalse(viewModel.beginSynchronization())
        assertEquals(0, incomingRepository.synchronizeCalls)
    }
}

private class FakeIncomingSettingsRepository(target: GoogleDriveTarget?) : SettingsRepository {
    private val targetFlow = MutableStateFlow(target)

    override fun observeDriveTarget(): Flow<GoogleDriveTarget?> = targetFlow

    override suspend fun setDriveTarget(target: GoogleDriveTarget) {
        targetFlow.value = target
    }

    override suspend fun clearDriveTarget() {
        targetFlow.value = null
    }
}

private class FakeIncomingRepository : IncomingIntelligenceRepository {
    var synchronizeCalls = 0
    var lastAccessToken: String? = null

    override fun observeDocuments(rootFolderId: String): Flow<List<IncomingIntelligenceRecord>> =
        flowOf(emptyList())

    override fun observeLastSyncedAt(rootFolderId: String): Flow<Long?> = flowOf(null)

    override suspend fun synchronize(rootFolderId: String, accessToken: String) {
        synchronizeCalls++
        lastAccessToken = accessToken
    }

    override suspend fun markOpened(rootFolderId: String, driveFileId: String) = Unit
}
