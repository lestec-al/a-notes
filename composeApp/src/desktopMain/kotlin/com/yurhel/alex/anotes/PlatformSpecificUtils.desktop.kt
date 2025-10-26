package com.yurhel.alex.anotes

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.useResource
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
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
import com.yurhel.alex.anotes.data.DriveObj
import com.yurhel.alex.anotes.ui.OrientationObj
import com.yurhel.alex.anotes.ui.theme.darkColorScheme
import com.yurhel.alex.anotes.ui.theme.lightColorScheme
import db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import org.jetbrains.skia.Image
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.Base64
import java.util.Collections
import javax.imageio.ImageIO

actual class Drive {

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
            //
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

    actual suspend fun getData(): DriveObj {
        var modifiedTime: Long? = null
        var data = JsonArray(emptyList())
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
                    data = Json.decodeFromString<JsonArray>(withContext(Dispatchers.IO) {
                        // UTF-8 - very import for PC ver !!!
                        outputStream.toString("UTF-8")
                    })
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
        return DriveObj(data, modifiedTime, isServiceOK)
    }

    actual suspend fun sendData(localData: String) {
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


@Composable
actual fun BackHandlerCustom(onBack: () -> Unit) {}

@Composable
actual fun getOrientation() = OrientationObj.Desktop

@Composable
actual fun keyboardAsState(): State<Boolean> {
    return rememberUpdatedState(false)
}

@Composable
actual fun getColorScheme(
    dynamicColor: Boolean,
    darkTheme: Boolean
): ColorScheme {
    return when {
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }
}

fun getSqlDriver(): SqlDriver {
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:notes.db")
    try {
        Database.Schema.create(driver)
    } catch (_: Exception) {}
    return driver
}

actual fun ImageBitmap.toBase64(): String? {
    val bufferedImage = this.toAwtImage()
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "PNG", outputStream)
    return Base64.getEncoder().encodeToString(outputStream.toByteArray())
}

actual fun String.toImageBitmap(): ImageBitmap? {
    val byteArray = Base64.getDecoder().decode(this)
    return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
}