package com.yurhel.alex.anotes

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.foundation.text.TextContextMenu
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.yurhel.alex.anotes.ui.theme.darkColorScheme
import com.yurhel.alex.anotes.ui.theme.lightColorScheme
import com.yurhel.alex.anotes.ui.utils.Orientation

@Composable
actual fun BackHandlerCustom(onBack: () -> Unit) {}

@Composable
actual fun getOrientation() = Orientation.Desktop

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
actual fun getPlatform() = Platform()

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun FixCutBugCompositionProvider(getSelection: () -> String?, content: @Composable () -> Unit) {
    // Here we just hide the "cut" button
    CompositionLocalProvider(LocalTextContextMenu provides getTextContextMenu(), content)
}

@OptIn(ExperimentalFoundationApi::class)
private fun getTextContextMenu(): TextContextMenu {
    return object : TextContextMenu {
        @Composable
        override fun Area(
            textManager: TextContextMenu.TextManager,
            state: ContextMenuState,
            content: @Composable () -> Unit
        ) {
            val items = {
                listOfNotNull(
                    textManager.copy?.let {
                        if (textManager.selectedText.isEmpty()) {
                            null
                        } else {
                            ContextMenuItem("Copy", it.execute)
                        }
                    },
                    textManager.paste?.let {
                        ContextMenuItem("Paste", it.execute)
                    },
                    textManager.selectAll?.let {
                        ContextMenuItem("Select All", it.execute)
                    }
                )
            }
            ContextMenuArea(items, state, content = content)
        }
    }
}