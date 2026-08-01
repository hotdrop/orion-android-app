package jp.hotdrop.orion.data.incoming

import android.net.Uri
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal const val GoogleDriveFolderMimeType = "application/vnd.google-apps.folder"
internal const val GoogleDocumentMimeType = "application/vnd.google-apps.document"

data class GoogleDriveFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val modifiedAt: Long,
    val webViewLink: String?,
)

interface GoogleDriveRemoteDataSource {
    suspend fun getFolder(accessToken: String, folderId: String): GoogleDriveFile

    suspend fun listChildren(accessToken: String, folderId: String): List<GoogleDriveFile>
}

class HttpGoogleDriveRemoteDataSource(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : GoogleDriveRemoteDataSource {
    override suspend fun getFolder(accessToken: String, folderId: String): GoogleDriveFile =
        withContext(ioDispatcher) {
            val url = Uri.parse("https://www.googleapis.com/drive/v3/files/$folderId")
                .buildUpon()
                .appendQueryParameter("fields", DriveFileFields)
                .build()
                .toString()
            parseFile(executeGet(url, accessToken))
        }

    override suspend fun listChildren(
        accessToken: String,
        folderId: String,
    ): List<GoogleDriveFile> = withContext(ioDispatcher) {
        val files = mutableListOf<GoogleDriveFile>()
        var pageToken: String? = null
        do {
            val query = "'$folderId' in parents and trashed = false and " +
                "(mimeType = '$GoogleDriveFolderMimeType' or mimeType = '$GoogleDocumentMimeType')"
            val uriBuilder = Uri.parse("https://www.googleapis.com/drive/v3/files")
                .buildUpon()
                .appendQueryParameter("q", query)
                .appendQueryParameter("spaces", "drive")
                .appendQueryParameter("orderBy", "modifiedTime desc")
                .appendQueryParameter("pageSize", "1000")
                .appendQueryParameter("fields", "nextPageToken,files($DriveFileFields)")
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
            connection.connectTimeout = ConnectTimeoutMillis
            connection.readTimeout = ReadTimeoutMillis
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
        const val DriveFileFields = "id,name,mimeType,modifiedTime,webViewLink"
        const val ConnectTimeoutMillis = 15_000
        const val ReadTimeoutMillis = 30_000
    }
}

class GoogleDriveApiException(
    val responseCode: Int,
    responseBody: String,
) : IOException("Google Drive API returned HTTP $responseCode: $responseBody")

class GoogleDriveNetworkException(cause: IOException) : IOException(cause)
