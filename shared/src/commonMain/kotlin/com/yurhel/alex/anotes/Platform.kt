package com.yurhel.alex.anotes

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.Clipboard
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.Note

expect class Platform {
    var showBackButtonTest: Boolean

    suspend fun importImage(after: (String) -> Unit)

    fun getDrive(): PlatformDrive

    fun getWidgetIdWhenCreated(): Int

    fun callInitUpdateWidget(
        isInitAction: Boolean,
        widgetId: Int,
        noteCreated: String,
        note: Note,
        db: LocalDB
    )

    fun callExit()

    fun formatDate(date: Long): String

    suspend fun copyToClipboard(str: String, clipboard: Clipboard)

    fun toBase64(img: ImageBitmap, format: String, maxSizeKB: Int = 900): String?

    fun toImageBitmap(str: String?, compress: Boolean): ImageBitmap?

    fun getSqlDriver(): SqlDriver

    fun createDataStorePlatform(): DataStore<Preferences>

    fun openLink(link: String)

    fun showToast(msg: String)

    fun getAppVersion(): String
}