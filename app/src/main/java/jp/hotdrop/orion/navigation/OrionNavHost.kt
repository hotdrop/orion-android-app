package jp.hotdrop.orion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import jp.hotdrop.orion.ui.incoming.IncomingIntelligenceRoute
import jp.hotdrop.orion.ui.incoming.memo.IncomingMemoRoute
import jp.hotdrop.orion.ui.intelligence.analysis.AnalysisRoute
import jp.hotdrop.orion.ui.intelligence.ArchiveRoute
import jp.hotdrop.orion.ui.intelligence.RecordDetailRoute
import jp.hotdrop.orion.ui.intelligence.RecordEditorRoute
import jp.hotdrop.orion.ui.settings.SettingsRoute

/**
 * トップレベル画面と記録の詳細・編集・分析の遷移を定義する。
 */
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
            IncomingIntelligenceRoute(
                onOpenSettings = {
                    navController.navigate(OrionDestination.SETTINGS_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onEditMemo = { id -> navController.navigate(OrionDestination.incomingMemoRoute(id)) },
                modifier = Modifier,
            )
        }
        composable(
            route = OrionDestination.INCOMING_MEMO_ROUTE,
            arguments = listOf(navArgument(OrionDestination.INCOMING_DOCUMENT_ID) { type = NavType.StringType }),
        ) {
            IncomingMemoRoute(onClose = { navController.popBackStack() })
        }
        composable(OrionTopLevelDestination.Archive.route) {
            ArchiveRoute(
                onNewSignal = { navController.navigate(OrionDestination.recordEditRoute("signal", 0)) },
                onAnalysis = {
                    navController.navigate(OrionDestination.ANALYSIS_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onSignal = { navController.navigate(OrionDestination.recordDetailRoute("signal", it)) },
                onFocus = { navController.navigate(OrionDestination.recordDetailRoute("focus", it)) },
            )
        }
        composable(
            route = OrionDestination.RECORD_DETAIL_ROUTE,
            arguments = listOf(
                navArgument("kind") { type = NavType.StringType },
                navArgument("recordId") { type = NavType.LongType },
            ),
        ) { entry ->
            val kind = entry.arguments?.getString("kind") ?: "signal"
            val id = entry.arguments?.getLong("recordId") ?: 0
            RecordDetailRoute(
                focus = kind == "focus",
                id = id,
                onEdit = { navController.navigate(OrionDestination.recordEditRoute(kind, id)) },
                onSignal = { recordId ->
                    navController.navigate(OrionDestination.recordDetailRoute("signal", recordId)) {
                        launchSingleTop = true
                    }
                },
                onFocus = { recordId ->
                    navController.navigate(OrionDestination.recordDetailRoute("focus", recordId)) {
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = OrionDestination.RECORD_EDIT_ROUTE,
            arguments = listOf(
                navArgument("kind") { type = NavType.StringType },
                navArgument("recordId") { type = NavType.LongType },
            ),
        ) { entry ->
            RecordEditorRoute(
                canDelete = (entry.arguments?.getLong("recordId") ?: 0) > 0,
                onClose = { navController.popBackStack() },
            )
        }
        composable(OrionDestination.ANALYSIS_ROUTE) {
            AnalysisRoute(
                onClose = { focusId ->
                    navController.popBackStack()
                    if (focusId != null) {
                        navController.navigate(OrionDestination.recordDetailRoute("focus", focusId))
                    }
                },
            )
        }
        composable(OrionDestination.SETTINGS_ROUTE) {
            SettingsRoute(modifier = Modifier)
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
