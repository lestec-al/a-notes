package com.yurhel.alex.anotes

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.Note
import db.Database
import okio.Path.Companion.toPath
import org.jetbrains.skia.Image
import java.awt.Desktop
import java.awt.datatransfer.StringSelection
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.text.DateFormat
import java.util.Base64
import java.util.Calendar
import java.util.Date
import javax.imageio.ImageIO

actual class Platform {
    actual fun getDrive(): PlatformDrive = PlatformDrive()

    actual fun callExit() {}

    actual fun getWidgetIdWhenCreated(): Int = 0

    actual fun callInitUpdateWidget(
        isInitAction: Boolean,
        widgetId: Int,
        noteCreated: String,
        note: Note,
        db: LocalDB
    ) {}

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

    @OptIn(ExperimentalComposeUiApi::class)
    actual suspend fun copyToClipboard(str: String, clipboard: Clipboard) {
        clipboard.setClipEntry(
            ClipEntry(StringSelection(str))
        )
    }

    actual fun toBase64(img: ImageBitmap): String? {
        val bufferedImage = img.toAwtImage()
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "PNG", outputStream)
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    actual fun toImageBitmap(str: String?): ImageBitmap? {
        if (str == null) return null
        val byteArray = Base64.getDecoder().decode(str)
        return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
    }

    actual fun getSqlDriver(): SqlDriver = JdbcSqliteDriver("jdbc:sqlite:notes.db").also {
        try { Database.Schema.create(it) } catch (_: Exception) {}
    }

    actual fun createDataStorePlatform(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            val file = File("notes.preferences_pb")
            file.absolutePath.toPath()
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
}