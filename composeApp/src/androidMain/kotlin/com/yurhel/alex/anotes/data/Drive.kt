package com.yurhel.alex.anotes.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import java.io.ByteArrayOutputStream
import java.util.Collections

class Drive private constructor() {

    companion object {
        @Volatile
        private var instance: com.yurhel.alex.anotes.data.Drive? = null

        fun getInstance(): com.yurhel.alex.anotes.data.Drive {
            return instance ?: synchronized(this) {
                instance ?: Drive().also { instance = it }
            }
        }
    }

    private var service: Drive? = null

    private suspend fun tryConnectToDrive(context: Context) {
        if (service == null) {
            // Drive auth
            var isComplete = false
            var needAuth = false

            val requestedScopes: List<Scope> = listOf(Scope(Scopes.DRIVE_APPFOLDER))
            val authorizationRequest = AuthorizationRequest.builder().setRequestedScopes(requestedScopes).build()
            Identity.getAuthorizationClient(context)
                .authorize(authorizationRequest)
                .addOnCompleteListener { task ->
                    if (task.result.hasResolution()) {
                        needAuth = true

                    } else {
                        // Setup drive service
                        val credentials = GoogleCredentials.create(AccessToken(task.result.accessToken, null))
                        service = Drive.Builder(
                            NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            HttpCredentialsAdapter(credentials)
                        )
                            .setApplicationName("com.yurhel.alex.anotes")
                            .build()
                    }
                    isComplete = true
                }

            while (!isComplete) {
                delay(500)
            }

            if (needAuth) {
                // Google auth
                try {
                    val request: GetCredentialRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(
                            GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(
                                    "1005212743226-9kmcfsa90u7n4ug9aqkcfgsj13qp17rt.apps.googleusercontent.com"
                                )
                                .setAutoSelectEnabled(true)
                                .setNonce("jnjnjnjn8hh77h77h")
                                .build()
                        )
                        .build()

                    val credentialManager = CredentialManager.create(context)
                    credentialManager.getCredential(context, request)
                    tryConnectToDrive(context)

                } catch (_: Exception) {}
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

    suspend fun getData(context: Context): DriveObj {
        var modifiedTime: Long? = null
        var data = JsonArray(emptyList())
        val isServiceOK = try {
            tryConnectToDrive(context)
            if (service != null) {
                // Get files
                val fileIds = getFileIds()
                modifiedTime = fileIds.second

                // Try to get drive data
                if (fileIds.first != "") {
                    val outputStream = ByteArrayOutputStream()
                    service!!.files().get(fileIds.first).executeMediaAndDownloadTo(outputStream)
                    data = Json.decodeFromString<JsonArray>(outputStream.toString())
                }
            }
            true
        } catch (e: Exception) {
            false
        }
        return DriveObj(data, modifiedTime, isServiceOK)
    }

    suspend fun sendData(
        localData: String,
        context: Context
    ) {
        try {
            tryConnectToDrive(context)
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