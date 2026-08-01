package jp.hotdrop.orion.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class RoomSettingsRepositoryTest {
    @Test
    fun setGoogleDrivePath_normalizesOuterWhitespaceAndSlashes() = runTest {
        val dao = FakeSettingsDao()
        val repository = RoomSettingsRepository(dao)

        repository.setGoogleDrivePath("  /ORION/Incoming/  ")

        assertEquals("ORION/Incoming", dao.path.value)
    }

    @Test
    fun setGoogleDrivePath_preservesInteriorSeparators() = runTest {
        val dao = FakeSettingsDao()
        val repository = RoomSettingsRepository(dao)

        repository.setGoogleDrivePath("/ORION//Incoming/")

        assertEquals("ORION//Incoming", dao.path.value)
    }

    @Test
    fun setGoogleDrivePath_deletesSettingsWhenNormalizedPathIsEmpty() = runTest {
        val dao = FakeSettingsDao(initialPath = "ORION/Incoming")
        val repository = RoomSettingsRepository(dao)

        repository.setGoogleDrivePath(" / ")

        assertNull(dao.path.value)
    }

    @Test
    fun setGoogleDrivePath_propagatesDaoFailure() = runTest {
        val dao = FakeSettingsDao(failOnWrite = true)
        val repository = RoomSettingsRepository(dao)

        try {
            repository.setGoogleDrivePath("ORION/Incoming")
            fail("Expected the DAO failure to propagate")
        } catch (error: IllegalStateException) {
            assertEquals("write failed", error.message)
        }
    }
}

private class FakeSettingsDao(
    initialPath: String? = null,
    private val failOnWrite: Boolean = false,
) : SettingsDao {
    val path = MutableStateFlow(initialPath)

    override fun observeGoogleDrivePath(id: Int): Flow<String?> = path

    override suspend fun upsert(settings: SettingsEntity) {
        check(!failOnWrite) { "write failed" }
        path.value = settings.googleDrivePath
    }

    override suspend fun delete(id: Int) {
        check(!failOnWrite) { "write failed" }
        path.value = null
    }
}
