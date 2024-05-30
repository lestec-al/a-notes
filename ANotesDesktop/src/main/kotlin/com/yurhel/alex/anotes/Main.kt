package com.yurhel.alex.anotes

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Screens
import com.yurhel.alex.anotes.ui.theme.ANotesDesktopTheme

fun main() = application {

    val vm = MainViewModel()

    val screen = vm.db.getScreen()
    val screenState = WindowState(
        position = WindowPosition(screen.posX, screen.posY),
        width = screen.width,
        height = screen.height
    )

    Window(
        onCloseRequest = {
            vm.db.setScreen(
                width = screenState.size.width.value.toLong(),
                height = screenState.size.height.value.toLong(),
                posX = screenState.position.x.value.toLong(),
                posY = screenState.position.y.value.toLong()
            )
            this.exitApplication()
        },
        state = screenState,
        title = "ANotes",
        icon = painterResource("icon.png")
    ) {
        ANotesDesktopTheme {
            Screens(vm = vm)
        }
    }
}