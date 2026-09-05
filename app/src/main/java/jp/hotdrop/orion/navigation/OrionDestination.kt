package jp.hotdrop.orion.navigation

import android.net.Uri
enum class OrionTopLevelDestination(
    val route: String,
    val title: String,
    val navigationLabel: String,
    val code: String,
) {
    Incoming(
        route = "incoming",
        title = "INCOMING INTELLIGENCE",
        navigationLabel = "INCOMING",
        code = "IN",
    ),
    Archive(
        route = "archive",
        title = "KNOWLEDGE ARCHIVE",
        navigationLabel = "ARCHIVE",
        code = "KA",
    ),
    ;

    companion object {
        fun fromRoute(route: String?): OrionTopLevelDestination? =
            entries.firstOrNull { it.route == route }
    }
}

object OrionDestination {
    const val INCOMING_MEMO_ROUTE = "incoming/memo/{documentId}"
    const val INCOMING_DOCUMENT_ID = "documentId"
    fun incomingMemoRoute(documentId: String): String = "incoming/memo/${Uri.encode(documentId)}"

    const val SettingsRoute = "settings"
    const val SettingsTitle = "SYSTEM SETTINGS"
    const val ArchiveNewRoute = "archive/new"
    const val ArchiveEditRoute = "archive/edit/{entryId}"
    const val ArchiveEntryIdArgument = "entryId"
    const val ArchiveNewTitle = "NEW KNOWLEDGE RECORD"
    const val ArchiveEditTitle = "EDIT KNOWLEDGE RECORD"

    fun archiveEditRoute(entryId: Long): String = "archive/edit/$entryId"
}
