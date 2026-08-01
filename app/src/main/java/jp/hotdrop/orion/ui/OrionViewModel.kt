package jp.hotdrop.orion.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.hotdrop.orion.navigation.OrionTopLevelDestination
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class OrionViewModel @Inject constructor(
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
