package com.yurhel.alex.anotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.yurhel.alex.anotes.getColorScheme

@Composable
fun ANotesTheme(
    darkTheme: Boolean? = null,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = getColorScheme(dynamicColor, darkTheme ?: isSystemInDarkTheme()),
        typography = Typography,
        content = content
    )
}