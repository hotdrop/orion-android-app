package jp.hotdrop.orion.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import jp.hotdrop.orion.navigation.OrionDestination
import jp.hotdrop.orion.navigation.OrionNavHost
import jp.hotdrop.orion.navigation.OrionTopLevelDestination
import jp.hotdrop.orion.navigation.navigateToTopLevel
import jp.hotdrop.orion.ui.components.OrionBottomNavigation
import jp.hotdrop.orion.ui.components.OrionHeader
import jp.hotdrop.orion.ui.incoming.IncomingIntelligenceScreen
import jp.hotdrop.orion.ui.incoming.uistate.IncomingIntelligenceUiState
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun OrionRoot(
    modifier: Modifier = Modifier,
    viewModel: OrionViewModel = hiltViewModel(),
) {
    val selectedDestination by viewModel.selectedDestination.collectAsStateWithLifecycle()

    OrionAppShell(
        selectedDestination = selectedDestination,
        onDestinationSelected = viewModel::selectDestination,
        modifier = modifier,
    )
}

@Composable
internal fun OrionAppShell(
    selectedDestination: OrionTopLevelDestination,
    onDestinationSelected: (OrionTopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    navHost: @Composable (NavHostController, Modifier) -> Unit = { navController, contentModifier ->
        OrionNavHost(
            navController = navController,
            startDestination = selectedDestination,
            modifier = contentModifier,
        )
    },
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isShowingSettings = currentRoute == OrionDestination.SETTINGS_ROUTE
    val isShowingArchiveEditor = currentRoute == OrionDestination.ARCHIVE_NEW_ROUTE || currentRoute == OrionDestination.ARCHIVE_EDIT_ROUTE
    val isShowingMemoEditor = currentRoute == OrionDestination.INCOMING_MEMO_ROUTE
    val isShowingSecondaryDestination = isShowingSettings || isShowingArchiveEditor || isShowingMemoEditor
    val currentTopLevelDestination = OrionTopLevelDestination.fromRoute(currentRoute) ?: selectedDestination
    val currentTitle = when {
        isShowingMemoEditor -> "EDIT PERSONAL NOTE"
        isShowingSettings -> OrionDestination.SETTINGS_TITLE
        currentRoute == OrionDestination.ARCHIVE_NEW_ROUTE -> OrionDestination.ARCHIVE_NEW_TITLE
        currentRoute == OrionDestination.ARCHIVE_EDIT_ROUTE -> OrionDestination.ARCHIVE_EDIT_TITLE
        else -> currentTopLevelDestination.title
    }
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    BackHandler(enabled = isShowingSettings) {
        navController.popBackStack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = OrionDeepNavy,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            OrionHeader(
                title = currentTitle,
                isShowingBackNavigation = isShowingSecondaryDestination,
                onSettingsClick = {
                    navController.navigate(OrionDestination.SETTINGS_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onBackClick = { backPressedDispatcher?.onBackPressed() },
            )
        },
        bottomBar = {
            if (!isShowingSecondaryDestination) {
                OrionBottomNavigation(
                    selectedDestination = currentTopLevelDestination,
                    onDestinationSelected = { destination ->
                        onDestinationSelected(destination)
                        navController.navigateToTopLevel(destination)
                    },
                )
            }
        },
    ) { innerPadding ->
        navHost(
            navController,
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

@Preview(showBackground = true, heightDp = 852)
@Composable
private fun OrionRootPreview() {
    OrionTheme {
        OrionAppShell(
            selectedDestination = OrionTopLevelDestination.Incoming,
            onDestinationSelected = {},
            navHost = { _, modifier ->
                IncomingIntelligenceScreen(
                    uiState = IncomingIntelligenceUiState(),
                    onSync = {},
                    onOpenSettings = {},
                    onOpenDocument = {},
                    modifier = modifier,
                )
            },
        )
    }
}
