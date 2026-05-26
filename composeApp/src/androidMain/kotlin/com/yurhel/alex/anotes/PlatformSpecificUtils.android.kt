package com.yurhel.alex.anotes

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Base64
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.toClipEntry
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yurhel.alex.anotes.ui.utils.OrientationObj
import com.yurhel.alex.anotes.ui.theme.darkColorScheme
import com.yurhel.alex.anotes.ui.theme.lightColorScheme
import db.Database
import java.io.ByteArrayOutputStream
import java.util.Date

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

@Composable
actual fun SetStatusBarColor(setIsLight: Boolean?, darkTheme: Boolean?) {
    val view = LocalView.current
    val isDark = darkTheme ?: isSystemInDarkTheme()
    if (setIsLight != null) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = setIsLight
        }
        DisposableEffect(Unit) {
            onDispose {
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            }
        }
    } else {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }
}

@Composable
actual fun formatDate(date: Long): String {
    val context = LocalContext.current
    return if (DateUtils.isToday(date)) {
        DateFormat.getTimeFormat(context).format(Date(date))
    } else {
        DateFormat.getMediumDateFormat(context).format(Date(date))
    }
}

actual suspend fun String.copyToClipboard(clipboard: Clipboard) {
    val clipData = ClipData.newPlainText("", this)
    clipboard.setClipEntry(clipData.toClipEntry())
}

actual fun ImageBitmap.toBase64(): String? {
    return try {
        val bitmap = this.asAndroidBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (_: Exception) {
        null
    }
}

actual fun String.toImageBitmap(): ImageBitmap? {
    val decodedString = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size).asImageBitmap()
}

actual fun getSqlDriver(): SqlDriver = AndroidSqliteDriver(Database.Schema, MyApp.appContext, "notes.db")

actual fun createDataStorePlatform(): DataStore<Preferences> = createDataStore(
    producePath = { MyApp.appContext.filesDir.resolve(dataStoreFileName).absolutePath }
)

actual fun openLink(link: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    MyApp.appContext.startActivity(intent.setData(link.toUri()))
}

actual fun showToast(msg: String) {
    Toast.makeText(MyApp.appContext, msg, Toast.LENGTH_SHORT).show()
}

actual fun getAppVersion() = try {
    val context = MyApp.appContext
    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    pInfo.versionName ?: ""
} catch (_: Exception) {
    ""
}