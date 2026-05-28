package com.yurhel.alex.anotes

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.Clipboard
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import com.yurhel.alex.anotes.data.DriveObj
import com.yurhel.alex.anotes.ui.utils.OrientationObj
import okio.Path.Companion.toPath

expect class Drive() {
    suspend fun getData(): DriveObj
    @Suppress("unused")
    suspend fun sendData(localData: String)
}

@Composable
expect fun BackHandlerCustom(onBack: ()-> Unit)

@Composable
expect fun getOrientation(): OrientationObj

@Composable
expect fun keyboardAsState(): State<Boolean>

@Composable
expect fun getColorScheme(
    dynamicColor: Boolean,
    darkTheme: Boolean
): ColorScheme

@Composable
expect fun SetStatusBarColor(setIsLight: Boolean?, darkTheme: Boolean?)

@Composable
expect fun formatDate(date: Long): String

expect suspend fun String.copyToClipboard(clipboard: Clipboard)

expect fun ImageBitmap.toBase64(): String?

expect fun String.toImageBitmap(): ImageBitmap?

expect fun getSqlDriver(): SqlDriver

expect fun createDataStorePlatform(): DataStore<Preferences>

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
internal const val dataStoreFileName = "notes.preferences_pb"

expect fun openLink(link: String)

expect fun showToast(msg: String)

expect fun getAppVersion(): String