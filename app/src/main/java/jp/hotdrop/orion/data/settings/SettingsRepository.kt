package jp.hotdrop.orion.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    fun observeDriveTarget(): Flow<GoogleDriveTarget?>

    suspend fun setDriveTarget(target: GoogleDriveTarget)

    suspend fun clearDriveTarget()
}

data class GoogleDriveTarget(
    val folderId: String,
    val displayPath: String,
)

class RoomSettingsRepository(
    private val settingsDao: SettingsDao,
) : SettingsRepository {
    override fun observeDriveTarget(): Flow<GoogleDriveTarget?> =
        settingsDao.observeSettings().map { settings ->
            val folderId = settings?.googleDriveFolderId
            if (folderId == null) {
                null
            } else {
                GoogleDriveTarget(
                    folderId = folderId,
                    displayPath = settings.googleDrivePath,
                )
            }
        }

    override suspend fun setDriveTarget(target: GoogleDriveTarget) {
        require(target.folderId.isNotBlank()) { "Google Drive folder ID must not be blank" }
        val normalizedPath = normalizeGoogleDrivePath(target.displayPath)
        require(normalizedPath.isNotEmpty()) { "Google Drive display path must not be blank" }
        settingsDao.upsert(
            SettingsEntity(
                googleDrivePath = normalizedPath,
                googleDriveFolderId = target.folderId,
            ),
        )
    }

    override suspend fun clearDriveTarget() = settingsDao.delete()
}

internal fun normalizeGoogleDrivePath(rawPath: String): String =
    rawPath.trim { character -> character.isWhitespace() || character == '/' }
