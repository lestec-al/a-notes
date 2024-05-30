package com.yurhel.alex.anotes.data.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.Scopes
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.util.Collections

class Drive(private val context: Context) {

    private var service: Drive? = null

    private fun tryConnectToDrive() {
        if (service == null) {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                val credentials = GoogleAccountCredential.usingOAuth2(context, listOf(Scopes.DRIVE_APPFOLDER))
                credentials.selectedAccount = account.account

                service = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(),
                    credentials
                )
                    .setApplicationName("com.yurhel.alex.anotes")
                    .build()
            }
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

    fun getData(): DriveObj {
        var modifiedTime: Long? = null
        var data = JSONArray()
        val isServiceOK = try {
            tryConnectToDrive()
            if (service != null) {
                // Get files
                val fileIds = getFileIds()
                modifiedTime = fileIds.second

                // Try to get drive data
                if (fileIds.first != "") {
                    val outputStream = ByteArrayOutputStream()
                    service!!.files().get(fileIds.first).executeMediaAndDownloadTo(outputStream)
                    data = JSONArray(outputStream.toString())
                }
            }
            true
        } catch (e: Exception) {
            false
        }
        return DriveObj(data, modifiedTime, isServiceOK)
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