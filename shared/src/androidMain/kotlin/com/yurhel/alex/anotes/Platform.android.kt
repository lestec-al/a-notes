package com.yurhel.alex.anotes

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.Note
import com.yurhel.alex.anotes.data.Widget
import db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import java.io.ByteArrayOutputStream
import java.util.Date

actual class Platform(private val context: Context) {

    actual fun getDrive(): PlatformDrive = PlatformDrive(context)

    actual fun callExit() = (context as Activity).finishAffinity()

    actual fun getWidgetIdWhenCreated(): Int {
        return (context as ComponentActivity).intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    actual fun callInitUpdateWidget(
        isInitAction: Boolean,
        widgetId: Int,
        noteCreated: String,
        note: Note,
        db: LocalDB
    ) {
        val activity = context as ComponentActivity
        activity.lifecycleScope.launch(Dispatchers.Default) {
            // Update widget
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)
            updateAppWidgetState(context, glanceId) {
                it[stringPreferencesKey("noteCreated")] = noteCreated
                it[stringPreferencesKey("noteText")] = note.text
                it[intPreferencesKey("noteId")] = note.id
            }
            NoteWidget().update(context, glanceId)
            // Initialize widget
            if (isInitAction) {
                // Log widget to DB
                db.widget.insert(Widget(widgetId = widgetId, noteCreated = noteCreated))
                // Create the return intent, set it with the activity result, finish the activity
                activity.setResult(RESULT_OK, Intent())
                activity.finish()
            }
        }
    }

    actual fun formatDate(date: Long): String {
        return if (DateUtils.isToday(date)) {
            DateFormat.getTimeFormat(context).format(Date(date))
        } else {
            DateFormat.getMediumDateFormat(context).format(Date(date))
        }
    }

    actual suspend fun copyToClipboard(str: String, clipboard: Clipboard) {
        val clipData = ClipData.newPlainText("", str)
        clipboard.setClipEntry(clipData.toClipEntry())
    }

    actual fun toBase64(img: ImageBitmap): String? {
        return try {
            val bitmap = img.asAndroidBitmap()
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (_: Exception) {
            null
        }
    }

    actual fun toImageBitmap(str: String?): ImageBitmap? {
        if (str == null) return null
        val decodedString = Base64.decode(str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size).asImageBitmap()
    }

    actual fun getSqlDriver(): SqlDriver = AndroidSqliteDriver(Database.Schema, context, "notes.db")

    actual fun createDataStorePlatform(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            (context.filesDir.absolutePath + "/notes.preferences_pb").toPath()
        }
    )

    actual fun openLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent.setData(link.toUri()))
    }

    actual fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    actual fun getAppVersion() = try {
        val context = context
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: ""
    } catch (_: Exception) {
        ""
    }
}