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
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.add_img
import db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.getString
import org.jetbrains.skia.Image
import java.awt.Desktop
import java.awt.RenderingHints
import java.awt.datatransfer.StringSelection
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.text.DateFormat
import java.util.Base64
import java.util.Calendar
import java.util.Date
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class Platform {
    actual var showBackButtonTest: Boolean = false

    actual suspend fun importImage(after: (String) -> Unit) {
        val fileChooser = JFileChooser().apply {
            fileFilter = FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "bmp", "gif")
            dialogTitle = getString(Res.string.add_img)
        }
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                val file = fileChooser.selectedFile
                val bufferedImage = withContext(Dispatchers.IO) { ImageIO.read(file) } ?: throw Exception("Failed to read image")
                val rgbImage = convertToRGB(bufferedImage)
                val image = rgbImage.toComposeImageBitmap()

                val base64Str = toBase64(image, "PNG")
                if (base64Str != null) {
                    after(base64Str)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun convertToRGB(image: BufferedImage): BufferedImage {
        // If already RGB, return as is
        if (image.type == BufferedImage.TYPE_INT_RGB ||
            image.type == BufferedImage.TYPE_INT_ARGB) {
            return image
        }
        // Convert to RGB
        val rgbImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        val g = rgbImage.createGraphics()
        g.drawImage(image, 0, 0, null)
        g.dispose()
        return rgbImage
    }

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

    private fun resize(bufImage: BufferedImage): BufferedImage {
        val w = bufImage.width / 2
        val h = bufImage.height / 2
        val bImg = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        val g = bImg.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.drawImage(bufImage, 0, 0, w, h, null)
        g.dispose()
        return bImg
    }

    actual fun toBase64(
        img: ImageBitmap,
        format: String,
        maxSizeKB: Int
    ): String? {
        var quality = 100
        var bufImage = img.toAwtImage()
        // Decrease the quality
        while (quality > 10) {
            val stream = ByteArrayOutputStream()
            ImageIO.write(bufImage, format, stream)
            val bytes = stream.toByteArray()
            if (bytes.size <= maxSizeKB * 1024) {
                return Base64.getEncoder().encodeToString(bytes)
            } else {
                bufImage = resize(bufImage)
            }
            quality -= 5
        }
        return null
    }

    actual fun toImageBitmap(str: String?, compress: Boolean): ImageBitmap? {
        if (str == null) return null
        val byteArray = Base64.getDecoder().decode(
            str.replace(Regex("[^A-Za-z0-9+/=]"), "")
        )
        return Image.makeFromEncoded(byteArray).run {
            if (!compress) {
                this.toComposeImageBitmap()
            } else {
                if (!(this.width > 1000 || this.height > 1000)) {
                    this.toComposeImageBitmap()
                } else {
                    resize(this.toComposeImageBitmap().toAwtImage()).toComposeImageBitmap()
                }
            }
        }
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