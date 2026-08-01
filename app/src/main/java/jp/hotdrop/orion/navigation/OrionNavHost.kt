package jp.hotdrop.orion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import jp.hotdrop.orion.ui.archive.KnowledgeArchiveScreen
import jp.hotdrop.orion.ui.incoming.IncomingIntelligenceScreen
import jp.hotdrop.orion.ui.settings.SettingsScreen

@Composable
fun OrionNavHost(
    navController: NavHostController,
    startDestination: OrionTopLevelDestination,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
    ) {
        composable(OrionTopLevelDestination.Incoming.route) {
            IncomingIntelligenceScreen(modifier = Modifier)
        }
        composable(OrionTopLevelDestination.Archive.route) {
            KnowledgeArchiveScreen(modifier = Modifier)
        }
        composable(OrionDestination.SettingsRoute) {
            SettingsScreen(modifier = Modifier)
        }
    }
}

fun NavHostController.navigateToTopLevel(destination: OrionTopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
