package com.yurhel.alex.anotes

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
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
import com.yurhel.alex.anotes.data.DriveObj
import com.yurhel.alex.anotes.ui.OrientationObj
import com.yurhel.alex.anotes.ui.theme.darkColorScheme
import com.yurhel.alex.anotes.ui.theme.lightColorScheme
import db.Database
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import java.io.ByteArrayOutputStream
import java.util.Collections

actual class Drive(private val context: Context) {

    private var service: Drive? = null

    private suspend fun tryConnectToDrive() {
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
                    tryConnectToDrive()
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
                    data = Json.decodeFromString<JsonArray>(outputStream.toString())
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
actual fun BackHandlerCustom(onBack: () -> Unit) {
    BackHandler(true, onBack)
}

@Composable
actual fun getOrientation() = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) OrientationObj.Landscape else OrientationObj.Portrait

@Composable
actual fun keyboardAsState(): State<Boolean> {
    val view = LocalView.current
    var isImeVisible by remember { mutableStateOf(false) }
    DisposableEffect(LocalWindowInfo.current) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            isImeVisible = ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) == true
            true
        }
        view.viewTreeObserver.addOnPreDrawListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }
    return rememberUpdatedState(isImeVisible)
}

@Composable
actual fun getColorScheme(
    dynamicColor: Boolean,
    darkTheme: Boolean
): ColorScheme {
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }
}

fun getSqlDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(Database.Schema, context, "notes.db")
}

actual fun ImageBitmap.toBase64(): String? {
    return try {
        val bitmap = this.asAndroidBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        null
    }
}

actual fun String.toImageBitmap(): ImageBitmap? {
    val decodedString = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size).asImageBitmap()
}