package data

import androidx.compose.ui.res.useResource
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.*

class Drive {
    private var service: Drive? = null

    private fun tryConnectToDrive() {
        if (service == null) {
            val gsonFactory = GsonFactory.getDefaultInstance()
            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()

            // Load client secrets
            val clientSecrets = useResource("auth.json") { inputStream ->
                GoogleClientSecrets.load(gsonFactory, InputStreamReader(inputStream))
            }

            // Build flow and trigger user authorization request.
            val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, gsonFactory, clientSecrets, listOf(DriveScopes.DRIVE_APPDATA))
                .setDataStoreFactory(FileDataStoreFactory(java.io.File("token")))
                .setAccessType("offline")
                .build()
            val receiver = LocalServerReceiver.Builder().setPort(8888).build()
            val credentials = AuthorizationCodeInstalledApp(flow, receiver).authorize("user")

            service = Drive.Builder(httpTransport, gsonFactory, credentials)
                .setApplicationName("com.yurhel.alex.anotes")
                .build()
        }
    }

    private fun getFileIds(): Pair<String, Long?> {
        // Get files
        val files = service!!.files().list()
            .setSpaces("appDataFolder")
            .setFields("nextPageToken, files(id, name, modifiedTime)")
            .setPageSize(3)
            .execute()
            .files

        // Search notes file
        var id = ""
        var modifiedTime: Long? = null
        for (file in files) {
            if (file.name.equals("notes.json")) {
                id = file.id
                modifiedTime = file.modifiedTime.value
            }
        }

        // Return file id, file modified time
        return Pair(id, modifiedTime)
    }

    fun getData(): Pair<JSONArray, Long?> {
        var modifiedTime: Long? = null
        var data = JSONArray()
        try {
            tryConnectToDrive()
            if (service != null) {
                // Get files
                val fileIds = getFileIds()
                modifiedTime = fileIds.second

                // Try to get drive data
                if (fileIds.first != "") {
                    val outputStream = ByteArrayOutputStream()
                    service!!.files().get(fileIds.first).executeMediaAndDownloadTo(outputStream)
                    data = JSONArray(outputStream.toString("UTF-8")) // UTF-8 - very import for PC ver !!!
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(data, modifiedTime)
    }

    fun sendData(localData: String) {
        try {
            tryConnectToDrive()
            if (service != null) {
                // Get files
                val fileIds = getFileIds()
                val driveFileId = fileIds.first
                //val driveModifiedTime: Long? = fileIds.second

                if (driveFileId != "") {
                    // Update data
                    service!!.files().update(
                        driveFileId, null, ByteArrayContent.fromString("application/json", localData)
                    ).setFields("id").execute()
                } else {
                    // Create data
                    val fileMetadata = File()
                    fileMetadata.name = "notes.json"
                    fileMetadata.parents = Collections.singletonList("appDataFolder")
                    service!!.files().create(
                        fileMetadata, ByteArrayContent.fromString("application/json", localData)
                    ).setFields("id").execute()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}