package jp.hotdrop.orion.ui

import androidx.lifecycle.SavedStateHandle
import jp.hotdrop.orion.navigation.OrionTopLevelDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class OrionViewModelTest {
    @Test
    fun selectedDestination_startsWithIncoming() {
        val viewModel = OrionViewModel(SavedStateHandle())

        assertEquals(OrionTopLevelDestination.Incoming, viewModel.selectedDestination.value)
    }

    @Test
    fun selectDestination_updatesState() {
        val viewModel = OrionViewModel(SavedStateHandle())

        viewModel.selectDestination(OrionTopLevelDestination.Archive)

        assertEquals(OrionTopLevelDestination.Archive, viewModel.selectedDestination.value)
    }
}
