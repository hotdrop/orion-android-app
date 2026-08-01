package jp.hotdrop.orion.data

import jp.hotdrop.orion.data.remote.GoogleDocumentMimeType
import jp.hotdrop.orion.data.remote.GoogleDriveFile
import jp.hotdrop.orion.data.remote.GoogleDriveFolderMimeType
import jp.hotdrop.orion.data.remote.GoogleDriveRemoteDataSource
import jp.hotdrop.orion.data.local.dao.IncomingIntelligenceDao
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceEntity
import jp.hotdrop.orion.data.local.entity.IncomingIntelligenceRecord
import jp.hotdrop.orion.data.local.entity.toRecord
import java.util.ArrayDeque
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface IncomingIntelligenceRepository {
    fun observeDocuments(rootFolderId: String): Flow<List<IncomingIntelligenceRecord>>
    fun observeLastSyncedAt(rootFolderId: String): Flow<Long?>
    suspend fun synchronize(rootFolderId: String, accessToken: String)
    suspend fun markOpened(rootFolderId: String, driveFileId: String)
}

class RoomIncomingIntelligenceRepository(
    private val dao: IncomingIntelligenceDao,
    private val remoteDataSource: GoogleDriveRemoteDataSource,
) : IncomingIntelligenceRepository {
    override fun observeDocuments(rootFolderId: String): Flow<List<IncomingIntelligenceRecord>> =
        dao.observeDocuments(rootFolderId).map { entities -> entities.map { it.toRecord() } }

    override fun observeLastSyncedAt(rootFolderId: String): Flow<Long?> =
        dao.observeLastSyncedAt(rootFolderId)

    override suspend fun synchronize(rootFolderId: String, accessToken: String) {
        val cachedById = dao.getDocuments(rootFolderId).associateBy { it.driveFileId }
        val remoteDocuments = fetchAllDocuments(rootFolderId, accessToken)
        val entities = remoteDocuments.map { document ->
            val cached = cachedById[document.file.id]
            IncomingIntelligenceEntity(
                rootFolderId = rootFolderId,
                driveFileId = document.file.id,
                title = document.file.name,
                modifiedAt = document.file.modifiedAt,
                relativePath = document.relativePath,
                webUrl = document.file.webViewLink
                    ?: "https://docs.google.com/document/d/${document.file.id}/edit",
                isNew = cached == null || cached.isNew || cached.modifiedAt < document.file.modifiedAt,
            )
        }
        // The cache is replaced only after the complete traversal succeeds.
        dao.replaceForRoot(rootFolderId, entities, System.currentTimeMillis())
    }

    override suspend fun markOpened(rootFolderId: String, driveFileId: String) {
        dao.markOpened(rootFolderId, driveFileId)
    }

    private suspend fun fetchAllDocuments(
        rootFolderId: String,
        accessToken: String,
    ): List<RemoteDocument> {
        val pendingFolders = ArrayDeque<FolderToScan>()
        val visitedFolderIds = mutableSetOf<String>()
        val documents = mutableListOf<RemoteDocument>()
        pendingFolders += FolderToScan(rootFolderId, relativePath = "")

        while (pendingFolders.isNotEmpty()) {
            val folder = pendingFolders.removeFirst()
            if (!visitedFolderIds.add(folder.id)) continue
            remoteDataSource.listChildren(accessToken, folder.id).forEach { file ->
                when (file.mimeType) {
                    GoogleDriveFolderMimeType -> pendingFolders += FolderToScan(
                        id = file.id,
                        relativePath = joinPath(folder.relativePath, file.name),
                    )
                    GoogleDocumentMimeType -> documents += RemoteDocument(
                        file = file,
                        relativePath = folder.relativePath.ifEmpty { "/" },
                    )
                }
            }
        }
        return documents.sortedByDescending { it.file.modifiedAt }
    }
}

private data class FolderToScan(
    val id: String,
    val relativePath: String,
)

private data class RemoteDocument(
    val file: GoogleDriveFile,
    val relativePath: String,
)

private fun joinPath(parent: String, child: String): String = if (parent.isEmpty()) child else "$parent/$child"
