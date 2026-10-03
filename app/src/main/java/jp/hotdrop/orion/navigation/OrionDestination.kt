package jp.hotdrop.orion.navigation

import android.net.Uri

enum class OrionTopLevelDestination(
    val route: String,
    val title: String,
    val navigationLabel: String,
    val code: String,
) {
    Incoming(route = "incoming", title = "INCOMING INTELLIGENCE", navigationLabel = "INCOMING", code = "IN"),
    Archive(route = "archive", title = "PERSONAL INTELLIGENCE", navigationLabel = "ARCHIVE", code = "PI");

    companion object {
        fun fromRoute(route: String?): OrionTopLevelDestination? =
            entries.firstOrNull { it.route == route }
    }
}

object OrionDestination {
    const val INCOMING_MEMO_ROUTE = "incoming/memo/{documentId}"
    const val INCOMING_DOCUMENT_ID = "documentId"
    fun incomingMemoRoute(documentId: String): String = "incoming/memo/${Uri.encode(documentId)}"

    const val SETTINGS_ROUTE = "settings"
    const val SETTINGS_TITLE = "SYSTEM SETTINGS"
    const val RECORD_DETAIL_ROUTE = "archive/{kind}/detail/{recordId}"
    const val RECORD_EDIT_ROUTE = "archive/{kind}/edit/{recordId}"
    const val ANALYSIS_ROUTE = "archive/analysis"

    fun recordDetailRoute(kind: String, id: Long): String = "archive/$kind/detail/$id"
    fun recordEditRoute(kind: String, id: Long): String = "archive/$kind/edit/$id"
}
