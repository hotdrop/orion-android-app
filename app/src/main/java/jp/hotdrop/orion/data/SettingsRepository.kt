package jp.hotdrop.orion.data

import jp.hotdrop.orion.data.local.dao.SettingsDao
import jp.hotdrop.orion.data.local.entity.SettingsEntity
import jp.hotdrop.orion.model.GoogleDriveTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
) {
    fun observeDriveTarget(): Flow<GoogleDriveTarget?> =
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

    suspend fun setDriveTarget(target: GoogleDriveTarget) {
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

    suspend fun clearDriveTarget() = settingsDao.delete()
}

internal fun normalizeGoogleDrivePath(rawPath: String): String = rawPath.trim { character -> character.isWhitespace() || character == '/' }
