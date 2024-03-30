package com.yurhel.alex.anotes

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Screens
import com.yurhel.alex.anotes.ui.theme.ANotesDesktopTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ANotes",
        icon = painterResource("icon.png")
    ) {
        ANotesDesktopTheme {
            Screens(vm = MainViewModel())
        }
    }
}