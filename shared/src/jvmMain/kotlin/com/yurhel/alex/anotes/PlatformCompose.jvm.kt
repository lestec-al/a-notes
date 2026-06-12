package com.yurhel.alex.anotes

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import com.yurhel.alex.anotes.ui.theme.darkColorScheme
import com.yurhel.alex.anotes.ui.theme.lightColorScheme
import com.yurhel.alex.anotes.ui.utils.Orientation

@Composable
actual fun BackHandlerCustom(onBack: () -> Unit) {}

@Composable
actual fun getOrientation() = Orientation.Desktop

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

// For tests only ???
@Composable
actual fun getPlatform() = Platform()