package jp.hotdrop.orion.ui.archive

import jp.hotdrop.orion.data.archive.KnowledgeArchiveValidationError
import jp.hotdrop.orion.ui.settings.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KnowledgeArchiveEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun existingEntry_isLoadedWithoutDirtyState() = runTest {
        val viewModel = KnowledgeArchiveEditorViewModel(
            repository = FakeKnowledgeArchiveRepository(listOf(archiveEntry(id = 8))),
            entryId = 8,
        )

        advanceUntilIdle()

        assertEquals("Compose State", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isDirty)
    }

    @Test
    fun save_withMissingTitleShowsValidationError() = runTest {
        val viewModel = KnowledgeArchiveEditorViewModel(FakeKnowledgeArchiveRepository(), null)
        viewModel.onUrlChanged("https://example.com")

        viewModel.save()

        assertEquals(
            KnowledgeArchiveValidationError.TitleRequired,
            viewModel.uiState.value.validationError,
        )
    }

    @Test
    fun save_successEmitsCloseAndIgnoresRepeatedTap() = runTest {
        val repository = FakeKnowledgeArchiveRepository()
        val viewModel = KnowledgeArchiveEditorViewModel(repository, null)
        viewModel.onTitleChanged("Title")
        viewModel.onUrlChanged("https://example.com")
        val event = async { viewModel.events.first() }

        viewModel.save()
        viewModel.save()
        advanceUntilIdle()

        assertEquals(1, repository.savedDrafts.size)
        assertEquals(KnowledgeArchiveEditorEvent.Close, event.await())
    }

    @Test
    fun saveFailure_keepsDraftAndAllowsRetry() = runTest {
        val repository = FakeKnowledgeArchiveRepository(failOnSave = true)
        val viewModel = KnowledgeArchiveEditorViewModel(repository, null)
        viewModel.onTitleChanged("Title")
        viewModel.onUrlChanged("https://example.com")

        viewModel.save()
        advanceUntilIdle()

        assertEquals("Title", viewModel.uiState.value.title)
        assertEquals(KnowledgeArchiveEditorFeedback.SaveFailed, viewModel.uiState.value.feedback)
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun requestBack_withDirtyDraftRequiresConfirmation() = runTest {
        val viewModel = KnowledgeArchiveEditorViewModel(FakeKnowledgeArchiveRepository(), null)
        viewModel.onMemoChanged("Uncommitted")

        viewModel.requestBack()

        assertTrue(viewModel.uiState.value.showDiscardConfirmation)
    }

    @Test
    fun delete_successEmitsClose() = runTest {
        val repository = FakeKnowledgeArchiveRepository(listOf(archiveEntry(id = 3)))
        val viewModel = KnowledgeArchiveEditorViewModel(repository, 3)
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.requestDelete()
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals(listOf(3L), repository.deletedIds)
        assertEquals(KnowledgeArchiveEditorEvent.Close, event.await())
    }
}
