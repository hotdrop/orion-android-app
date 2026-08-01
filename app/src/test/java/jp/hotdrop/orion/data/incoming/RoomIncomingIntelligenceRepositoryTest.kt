package jp.hotdrop.orion.data.incoming

import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RoomIncomingIntelligenceRepositoryTest {
    @Test
    fun synchronize_recursivelyBuildsRelativePathsAndMarksNewDocuments() = runTest {
        val dao = FakeIncomingDao()
        val remote = FakeDriveRemoteDataSource(
            children = mapOf(
                "root" to listOf(folder("android", "Android"), document("doc-root", "Root Doc", 10)),
                "android" to listOf(folder("compose", "Compose")),
                "compose" to listOf(document("doc-compose", "Compose Doc", 20)),
            ),
        )
        val repository = RoomIncomingIntelligenceRepository(dao, remote)

        repository.synchronize("root", "token")

        val saved = dao.documents.value.sortedBy { it.driveFileId }
        assertEquals(listOf("doc-compose", "doc-root"), saved.map { it.driveFileId })
        assertEquals("Android/Compose", saved.first().relativePath)
        assertEquals("/", saved.last().relativePath)
        assertTrue(saved.all { it.isNew })
        assertEquals(listOf("root", "android", "compose"), remote.requestedFolderIds)
    }

    @Test
    fun synchronize_preservesCacheWhenRecursiveRequestFails() = runTest {
        val cached = entity("cached", modifiedAt = 10, isNew = false)
        val dao = FakeIncomingDao(listOf(cached))
        val remote = FakeDriveRemoteDataSource(
            children = mapOf("root" to listOf(folder("child", "Child"))),
            failingFolderId = "child",
        )
        val repository = RoomIncomingIntelligenceRepository(dao, remote)

        try {
            repository.synchronize("root", "token")
            fail("Expected remote failure")
        } catch (_: IOException) {
            // Expected.
        }

        assertEquals(listOf(cached), dao.documents.value)
        assertFalse(dao.replaceCalled)
    }

    @Test
    fun synchronize_marksOnlyChangedDocumentsAsNew() = runTest {
        val dao = FakeIncomingDao(
            listOf(
                entity("unchanged", modifiedAt = 10, isNew = false),
                entity("updated", modifiedAt = 10, isNew = false),
            ),
        )
        val remote = FakeDriveRemoteDataSource(
            children = mapOf(
                "root" to listOf(
                    document("unchanged", "Unchanged", 10),
                    document("updated", "Updated", 20),
                ),
            ),
        )
        val repository = RoomIncomingIntelligenceRepository(dao, remote)

        repository.synchronize("root", "token")

        val saved = dao.documents.value.associateBy { it.driveFileId }
        assertFalse(saved.getValue("unchanged").isNew)
        assertTrue(saved.getValue("updated").isNew)
    }

    private fun folder(id: String, name: String) = GoogleDriveFile(
        id = id,
        name = name,
        mimeType = GoogleDriveFolderMimeType,
        modifiedAt = 0,
        webViewLink = null,
    )

    private fun document(id: String, name: String, modifiedAt: Long) = GoogleDriveFile(
        id = id,
        name = name,
        mimeType = GoogleDocumentMimeType,
        modifiedAt = modifiedAt,
        webViewLink = "https://docs.google.com/document/d/$id/edit",
    )

    private fun entity(id: String, modifiedAt: Long, isNew: Boolean) = IncomingIntelligenceEntity(
        rootFolderId = "root",
        driveFileId = id,
        title = id,
        modifiedAt = modifiedAt,
        relativePath = "/",
        webUrl = "https://docs.google.com/document/d/$id/edit",
        isNew = isNew,
    )
}

private class FakeDriveRemoteDataSource(
    private val children: Map<String, List<GoogleDriveFile>>,
    private val failingFolderId: String? = null,
) : GoogleDriveRemoteDataSource {
    val requestedFolderIds = mutableListOf<String>()

    override suspend fun getFolder(accessToken: String, folderId: String) =
        error("Not used")

    override suspend fun listChildren(accessToken: String, folderId: String): List<GoogleDriveFile> {
        requestedFolderIds += folderId
        if (folderId == failingFolderId) throw IOException("request failed")
        return children[folderId].orEmpty()
    }
}

private class FakeIncomingDao(initialDocuments: List<IncomingIntelligenceEntity> = emptyList()) :
    IncomingIntelligenceDao {
    val documents = MutableStateFlow(initialDocuments)
    private val lastSyncedAt = MutableStateFlow<Long?>(null)
    var replaceCalled = false

    override fun observeDocuments(rootFolderId: String): Flow<List<IncomingIntelligenceEntity>> = documents

    override suspend fun getDocuments(rootFolderId: String): List<IncomingIntelligenceEntity> =
        documents.value

    override fun observeLastSyncedAt(rootFolderId: String): Flow<Long?> = lastSyncedAt

    override suspend fun insertAll(documents: List<IncomingIntelligenceEntity>) {
        this.documents.value = documents
    }

    override suspend fun upsertSyncState(syncState: IncomingIntelligenceSyncStateEntity) {
        lastSyncedAt.value = syncState.lastSyncedAt
    }

    override suspend fun deleteForRoot(rootFolderId: String) {
        documents.value = emptyList()
    }

    override suspend fun markOpened(rootFolderId: String, driveFileId: String) {
        documents.value = documents.value.map {
            if (it.driveFileId == driveFileId) it.copy(isNew = false) else it
        }
    }

    override suspend fun replaceForRoot(
        rootFolderId: String,
        documents: List<IncomingIntelligenceEntity>,
        syncedAt: Long,
    ) {
        replaceCalled = true
        this.documents.value = documents
        lastSyncedAt.value = syncedAt
    }
}
