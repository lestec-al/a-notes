package com.yurhel.alex.anotes.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.Scopes
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.util.Collections

fun driveSync(
    isExport: Boolean,
    db: DB,
    context: Context,
    checkModifiedTime: (Long?) -> Boolean,
    after: (Boolean) -> Unit
) {
    Thread {
        var isConnectionOK = false
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                val pair: Pair<Drive, FileList> = connectToDrive(context, account)
                val service = pair.first

                // Search notes file
                var driveModifiedTime: Long? = null
                var driveFileId = ""
                for (file in pair.second.files) {
                    if (file.name.equals("notes.json")) {
                        driveFileId = file.id
                        driveModifiedTime = file.modifiedTime.value
                    }
                }

                val localData = db.exportDB().toString()
                val isSendDataAllowed = checkModifiedTime(driveModifiedTime)

                if (isExport && localData.isNotEmpty() && isSendDataAllowed) {
                    exportData(service, driveFileId, localData)
                } else if (!isExport) {
                    // Try to get data
                    val driveData = if (driveFileId != "") {
                        val outputStream = ByteArrayOutputStream()
                        service.files().get(driveFileId).executeMediaAndDownloadTo(outputStream)
                        JSONArray(outputStream.toString())
                    } else {
                        JSONArray()
                    }
                    if (driveData.length() > 0) {
                        // Update local
                        db.importDB(driveData.toString())
                    } else {
                        // If drive empty -> send local data to drive ???
                        if (localData.isNotEmpty()) exportData(service, driveFileId, localData)
                    }
                }
                isConnectionOK = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        after(isConnectionOK)
    }.start()
}

fun driveCheckIfEmpty(context: Context, actionAfter: (Boolean, Long?) -> Unit) {
    Thread {
        var modifiedTime: Long? = null
        var data = JSONArray()
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                val pair: Pair<Drive, FileList> = connectToDrive(context, account)

                // Search notes file
                var driveFileId = ""
                for (file in pair.second.files) {
                    if (file.name.equals("notes.json")) {
                        driveFileId = file.id
                        modifiedTime = file.modifiedTime.value
                    }
                }

                // Try to get drive data
                if (driveFileId != "") {
                    val outputStream = ByteArrayOutputStream()
                    pair.first.files().get(driveFileId).executeMediaAndDownloadTo(outputStream)
                    data = JSONArray(outputStream.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        actionAfter(data.length() < 1, modifiedTime)
    }.start()
}

private fun connectToDrive(context: Context, account: GoogleSignInAccount): Pair<Drive, FileList> {
    val credentials = GoogleAccountCredential.usingOAuth2(context, listOf(Scopes.DRIVE_APPFOLDER))
    credentials.selectedAccount = account.account

    val service = Drive.Builder(
        AndroidHttp.newCompatibleTransport(),
        GsonFactory.getDefaultInstance(),
        credentials
    )
        .setApplicationName("com.yurhel.alex.anotes")
        .build()

    val files = service.files().list()
        .setSpaces("appDataFolder")
        .setFields("nextPageToken, files(id, name, modifiedTime)")
        .setPageSize(10)
        .execute()

    return Pair(service, files)
}

private fun exportData(service: Drive, driveFileId: String, localData: String) {
    if (driveFileId != "") {
        // Update data
        service.files().update(
            driveFileId, null, ByteArrayContent.fromString("application/json", localData)
        ).setFields("id").execute()
    } else {
        // Create data
        val fileMetadata = File()
        fileMetadata.name = "notes.json"
        fileMetadata.parents = Collections.singletonList("appDataFolder")
        service.files().create(
            fileMetadata, ByteArrayContent.fromString("application/json", localData)
        ).setFields("id").execute()
    }
}