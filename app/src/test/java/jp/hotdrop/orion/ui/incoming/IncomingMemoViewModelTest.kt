package jp.hotdrop.orion.ui.incoming

import androidx.lifecycle.SavedStateHandle
import jp.hotdrop.orion.data.FakeIncomingPersonalDao
import jp.hotdrop.orion.data.IncomingPersonalRepository
import jp.hotdrop.orion.navigation.OrionDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class IncomingMemoViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun saveFailureRetainsInput_andRetryPersistsAndCloses() = runTest(dispatcher) {
        val dao = FakeIncomingPersonalDao()
        val vm = IncomingMemoViewModel(IncomingPersonalRepository(dao), handle())
        runCurrent()
        vm.changeMemo("important")
        dao.failSave = true
        vm.save()
        runCurrent()
        assertEquals("important", vm.uiState.value.memo)
        assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.isSaving)
        dao.failSave = false
        vm.save()
        runCurrent()
        assertEquals("important", dao.records["doc"]?.memo)
        assertEquals(Unit, vm.events.first())
    }

    @Test fun dirtyBackRequiresConfirmation_andDraftRestoresWithoutSaving() = runTest(dispatcher) {
        val dao = FakeIncomingPersonalDao()
        val savedState = handle()
        val repository = IncomingPersonalRepository(dao)
        val vm = IncomingMemoViewModel(repository, savedState)
        runCurrent()
        vm.changeMemo("draft")
        vm.requestBack()
        assertTrue(vm.uiState.value.showDiscard)
        vm.dismissDiscard()
        assertFalse(vm.uiState.value.showDiscard)
        val restored = IncomingMemoViewModel(repository, savedState)
        runCurrent()
        assertEquals("draft", restored.uiState.value.memo)
        assertEquals("", dao.records["doc"]?.memo)
        restored.requestBack()
        assertTrue(restored.uiState.value.showDiscard)
    }

    private fun handle() = SavedStateHandle(mapOf(OrionDestination.INCOMING_DOCUMENT_ID to "doc"))
}
