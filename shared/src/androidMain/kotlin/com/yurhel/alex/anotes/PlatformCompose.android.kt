package com.yurhel.alex.anotes

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.AndroidClipboard
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.nativeClipboardManager
import androidx.compose.ui.platform.toClipEntry
import androidx.core.view.WindowCompat
import com.yurhel.alex.anotes.ui.theme.darkColorScheme
import com.yurhel.alex.anotes.ui.theme.lightColorScheme
import com.yurhel.alex.anotes.ui.utils.Orientation

@Composable
actual fun BackHandlerCustom(onBack: () -> Unit) {
    BackHandler(true, onBack)
}

@Composable
actual fun getOrientation() = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Orientation.Landscape else Orientation.Portrait

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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                setIsLight
        }
        DisposableEffect(Unit) {
            onDispose {
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    !isDark
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
actual fun getPlatform() = Platform(LocalContext.current)

@Composable
actual fun FixCutBugCompositionProvider(getSelection: () -> String?, content: @Composable () -> Unit) {
    // Here we override clipboard
    val clipboard = LocalClipboard.current
    val newClipboard = getClipboard(clipboard, getSelection)
    CompositionLocalProvider(LocalClipboard provides newClipboard, content)
}

private fun getClipboard(originalClipboard: Clipboard, getSelection: () -> String?): Clipboard {
    return object : AndroidClipboard {
        override val clipboardManager: ClipboardManager
            get() = originalClipboard.nativeClipboardManager

        override suspend fun getClipEntry() = originalClipboard.getClipEntry()

        override suspend fun setClipEntry(clipEntry: ClipEntry?) {
            val text = getSelection()
            if (text != null) {
                val clipData = ClipData.newPlainText("", text)
                originalClipboard.setClipEntry(clipData.toClipEntry())
            } else {
                originalClipboard.setClipEntry(clipEntry)
            }
        }

    }
}