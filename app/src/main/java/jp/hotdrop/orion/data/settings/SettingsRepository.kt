package jp.hotdrop.orion.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeGoogleDrivePath(): Flow<String?>

    suspend fun setGoogleDrivePath(rawPath: String)
}

class RoomSettingsRepository(
    private val settingsDao: SettingsDao,
) : SettingsRepository {
    override fun observeGoogleDrivePath(): Flow<String?> = settingsDao.observeGoogleDrivePath()

    override suspend fun setGoogleDrivePath(rawPath: String) {
        val normalizedPath = normalizeGoogleDrivePath(rawPath)
        if (normalizedPath.isEmpty()) {
            settingsDao.delete()
        } else {
            settingsDao.upsert(SettingsEntity(googleDrivePath = normalizedPath))
        }
    }
}

internal fun normalizeGoogleDrivePath(rawPath: String): String =
    rawPath.trim { character -> character.isWhitespace() || character == '/' }
