package jp.hotdrop.orion.data.remote

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.core.net.toUri

internal const val GoogleDriveFolderMimeType = "application/vnd.google-apps.folder"
internal const val GoogleDocumentMimeType = "application/vnd.google-apps.document"
internal const val WordDocumentMimeType =
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

data class GoogleDriveFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val modifiedAt: Long,
    val webViewLink: String?,
)

interface GoogleDriveRemoteDataSource {
    suspend fun listChildren(accessToken: String, folderId: String): List<GoogleDriveFile>
    suspend fun listFolders(accessToken: String, parentFolderId: String): List<GoogleDriveFile>
}

class HttpGoogleDriveRemoteDataSource(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : GoogleDriveRemoteDataSource {
    override suspend fun listChildren(
        accessToken: String,
        folderId: String,
    ): List<GoogleDriveFile> = listFiles(
        accessToken = accessToken,
        parentFolderId = folderId,
        mimeTypeQuery = "(" +
            "mimeType = '$GoogleDriveFolderMimeType' or " +
            "mimeType = '$GoogleDocumentMimeType' or " +
            "mimeType = '$WordDocumentMimeType'" +
            ")",
        orderBy = "modifiedTime desc",
    )

    override suspend fun listFolders(
        accessToken: String,
        parentFolderId: String,
    ): List<GoogleDriveFile> = listFiles(
        accessToken = accessToken,
        parentFolderId = parentFolderId,
        mimeTypeQuery = "mimeType = '$GoogleDriveFolderMimeType'",
        orderBy = "name",
    )

    private suspend fun listFiles(
        accessToken: String,
        parentFolderId: String,
        mimeTypeQuery: String,
        orderBy: String,
    ): List<GoogleDriveFile> = withContext(ioDispatcher) {
        val files = mutableListOf<GoogleDriveFile>()
        var pageToken: String? = null
        do {
            val query = "'$parentFolderId' in parents and trashed = false and $mimeTypeQuery"
            val uriBuilder = "https://www.googleapis.com/drive/v3/files".toUri()
                .buildUpon()
                .appendQueryParameter("q", query)
                .appendQueryParameter("spaces", "drive")
                .appendQueryParameter("orderBy", orderBy)
                .appendQueryParameter("pageSize", "1000")
                .appendQueryParameter("fields", "nextPageToken,files($DRIVE_FILE_FIELDS)")
            pageToken?.let { uriBuilder.appendQueryParameter("pageToken", it) }

            val response = executeGet(uriBuilder.build().toString(), accessToken)
            val fileArray = response.getJSONArray("files")
            for (index in 0 until fileArray.length()) {
                files += parseFile(fileArray.getJSONObject(index))
            }
            pageToken = response.optString("nextPageToken").ifBlank { null }
        } while (pageToken != null)
        files
    }

    private fun executeGet(url: String, accessToken: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            val responseBody = (if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            })?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (responseCode !in 200..299) {
                throw GoogleDriveApiException(responseCode, responseBody)
            }
            JSONObject(responseBody)
        } catch (error: GoogleDriveApiException) {
            throw error
        } catch (error: IOException) {
            throw GoogleDriveNetworkException(error)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseFile(json: JSONObject) = GoogleDriveFile(
        id = json.getString("id"),
        name = json.getString("name"),
        mimeType = json.getString("mimeType"),
        modifiedAt = json.optString("modifiedTime")
            .takeIf(String::isNotBlank)
            ?.let { Instant.parse(it).toEpochMilli() }
            ?: 0L,
        webViewLink = json.optString("webViewLink").ifBlank { null },
    )

    private companion object {
        const val DRIVE_FILE_FIELDS = "id,name,mimeType,modifiedTime,webViewLink"
    }
}

class GoogleDriveApiException(
    val responseCode: Int,
    responseBody: String,
) : IOException("Google Drive API returned HTTP $responseCode: $responseBody")

class GoogleDriveNetworkException(cause: IOException) : IOException(cause)
