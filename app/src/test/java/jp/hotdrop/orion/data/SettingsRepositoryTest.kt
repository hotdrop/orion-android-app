package jp.hotdrop.orion.data

import jp.hotdrop.orion.data.local.dao.SettingsDao
import jp.hotdrop.orion.data.local.entity.SettingsEntity
import jp.hotdrop.orion.model.GoogleDriveTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class RoomSettingsRepositoryTest {
    @Test
    fun setDriveTarget_normalizesDisplayPathAndStoresFolderId() = runTest {
        val dao = FakeSettingsDao()
        val repository = SettingsRepository(dao)

        repository.setDriveTarget(GoogleDriveTarget("folder-1", "  /ORION/Incoming/  "))

        assertEquals("folder-1", dao.settings.value?.googleDriveFolderId)
        assertEquals("ORION/Incoming", dao.settings.value?.googleDrivePath)
    }

    @Test
    fun observeDriveTarget_ignoresLegacyPathWithoutFolderId() = runTest {
        val dao = FakeSettingsDao(SettingsEntity(googleDrivePath = "ORION/Incoming"))
        val repository = SettingsRepository(dao)

        assertNull(repository.observeDriveTarget().first())
    }

    @Test
    fun clearDriveTarget_deletesSettings() = runTest {
        val dao = FakeSettingsDao(
            SettingsEntity(googleDrivePath = "Incoming", googleDriveFolderId = "folder-1"),
        )
        val repository = SettingsRepository(dao)

        repository.clearDriveTarget()

        assertNull(dao.settings.value)
    }

    @Test
    fun setDriveTarget_propagatesDaoFailure() = runTest {
        val repository = SettingsRepository(FakeSettingsDao(failOnWrite = true))

        try {
            repository.setDriveTarget(GoogleDriveTarget("folder-1", "Incoming"))
            fail("Expected the DAO failure to propagate")
        } catch (error: IllegalStateException) {
            assertEquals("write failed", error.message)
        }
    }
}

private class FakeSettingsDao(
    initialSettings: SettingsEntity? = null,
    private val failOnWrite: Boolean = false,
) : SettingsDao {
    val settings = MutableStateFlow(initialSettings)

    override fun observeSettings(id: Int): Flow<SettingsEntity?> = settings

    override suspend fun upsert(settings: SettingsEntity) {
        check(!failOnWrite) { "write failed" }
        this.settings.value = settings
    }

    override suspend fun delete(id: Int) {
        check(!failOnWrite) { "write failed" }
        settings.value = null
    }
}
