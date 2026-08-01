package jp.hotdrop.orion.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import jp.hotdrop.orion.navigation.OrionTopLevelDestination
import kotlinx.coroutines.flow.StateFlow

class OrionViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val selectedDestination: StateFlow<OrionTopLevelDestination> =
        savedStateHandle.getStateFlow(
            key = SelectedDestinationKey,
            initialValue = OrionTopLevelDestination.Incoming,
        )

    fun selectDestination(destination: OrionTopLevelDestination) {
        savedStateHandle[SelectedDestinationKey] = destination
    }

    private companion object {
        const val SelectedDestinationKey = "selected_destination"
    }
}
