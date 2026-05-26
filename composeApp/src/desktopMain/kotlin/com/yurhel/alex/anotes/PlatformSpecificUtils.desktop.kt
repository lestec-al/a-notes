package com.yurhel.alex.anotes

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.yurhel.alex.anotes.ui.utils.OrientationObj
import com.yurhel.alex.anotes.ui.theme.darkColorScheme
import com.yurhel.alex.anotes.ui.theme.lightColorScheme
import db.Database
import org.jetbrains.skia.Image
import java.awt.Desktop
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.text.DateFormat
import java.util.Base64
import java.util.Calendar
import java.util.Date
import javax.imageio.ImageIO

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

@Composable
actual fun SetStatusBarColor(setIsLight: Boolean?, darkTheme: Boolean?) {}

@Composable
actual fun formatDate(date: Long): String {
    val date = Date(date)
    // Check if is today
    val today = Calendar.Builder().build()
    today.time = Date()
    val check = Calendar.Builder().build()
    check.time = date
    return if (
        today.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR) &&
        today.get(Calendar.YEAR) == check.get(Calendar.YEAR)
    ) {
        DateFormat.getTimeInstance().format(date)
    } else {
        DateFormat.getDateInstance().format(date)
    }
}

actual suspend fun String.copyToClipboard(clipboard: Clipboard) {
    clipboard.setClipEntry(
        ClipEntry(java.awt.datatransfer.StringSelection(this))
    )
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

actual fun getSqlDriver(): SqlDriver = JdbcSqliteDriver("jdbc:sqlite:notes.db").also {
    try { Database.Schema.create(it) } catch (_: Exception) {}
}

actual fun createDataStorePlatform(): DataStore<Preferences> = createDataStore(
    producePath = {
        val file = File(dataStoreFileName)
        file.absolutePath
    }
)

actual fun openLink(link: String) {
    val uri = URI.create(link)
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(uri)
        }
    }
}

actual fun showToast(msg: String) {}

actual fun getAppVersion() = System.getProperty("jpackage.app-version") ?: ""