package jp.hotdrop.orion.ui.archive

import jp.hotdrop.orion.ui.settings.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KnowledgeArchiveViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun entries_areObservedFromRepository() = runTest {
        val repository = FakeKnowledgeArchiveRepository(listOf(archiveEntry()))
        val viewModel = KnowledgeArchiveViewModel(repository)

        advanceUntilIdle()

        assertEquals("Compose State", viewModel.uiState.value.entries.single().title)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.loadFailed)
    }

    @Test
    fun urlOpenFailure_canBeReportedAndCleared() = runTest {
        val viewModel = KnowledgeArchiveViewModel(FakeKnowledgeArchiveRepository())
        advanceUntilIdle()

        viewModel.reportUrlOpenFailure()
        assertTrue(viewModel.uiState.value.urlOpenFailed)

        viewModel.clearUrlOpenFailure()
        assertFalse(viewModel.uiState.value.urlOpenFailed)
    }
}
