package jp.hotdrop.orion.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import jp.hotdrop.orion.data.settings.SettingsRepository
import jp.hotdrop.orion.navigation.OrionDestination
import jp.hotdrop.orion.navigation.OrionNavHost
import jp.hotdrop.orion.navigation.OrionTopLevelDestination
import jp.hotdrop.orion.navigation.navigateToTopLevel
import jp.hotdrop.orion.ui.components.OrionBottomNavigation
import jp.hotdrop.orion.ui.components.OrionHeader
import jp.hotdrop.orion.ui.theme.OrionDeepNavy
import jp.hotdrop.orion.ui.theme.OrionTheme

@Composable
fun OrionRoot(
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    viewModel: OrionViewModel = viewModel(),
) {
    val selectedDestination by viewModel.selectedDestination.collectAsStateWithLifecycle()

    OrionAppShell(
        selectedDestination = selectedDestination,
        onDestinationSelected = viewModel::selectDestination,
        settingsRepository = settingsRepository,
        modifier = modifier,
    )
}

@Composable
internal fun OrionAppShell(
    selectedDestination: OrionTopLevelDestination,
    onDestinationSelected: (OrionTopLevelDestination) -> Unit,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isShowingSettings = currentRoute == OrionDestination.SettingsRoute
    val currentTopLevelDestination = OrionTopLevelDestination.fromRoute(currentRoute)
        ?: selectedDestination
    val currentTitle = if (isShowingSettings) {
        OrionDestination.SettingsTitle
    } else {
        currentTopLevelDestination.title
    }

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
                isShowingSettings = isShowingSettings,
                onSettingsClick = {
                    navController.navigate(OrionDestination.SettingsRoute) {
                        launchSingleTop = true
                    }
                },
                onBackClick = navController::popBackStack,
            )
        },
        bottomBar = {
            if (!isShowingSettings) {
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
        OrionNavHost(
            navController = navController,
            startDestination = selectedDestination,
            settingsRepository = settingsRepository,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Preview(
    name = "ORION Root Shell",
    showBackground = true,
    backgroundColor = 0xFF030812,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun OrionRootPreview() {
    OrionTheme {
        OrionAppShell(
            selectedDestination = OrionTopLevelDestination.Incoming,
            onDestinationSelected = {},
            settingsRepository = PreviewSettingsRepository,
        )
    }
}

private val PreviewSettingsRepository = object : SettingsRepository {
    override fun observeGoogleDrivePath() = kotlinx.coroutines.flow.flowOf<String?>(null)

    override suspend fun setGoogleDrivePath(rawPath: String) = Unit
}
