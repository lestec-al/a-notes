package com.yurhel.alex.anotes

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yurhel.alex.anotes.data.SettingsDataStore
import com.yurhel.alex.anotes.ui.App
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.utils.getAppName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.awt.Dimension

fun main() = application {
    // Fixes problems with sharpness & flickering
    System.setProperty("skiko.renderApi", "OPENGL")

    val platform = Platform()
    val settings = SettingsDataStore.getInstance { platform.createDataStorePlatform() }
    // runBlocking is necessary here
    val screen = runBlocking { settings.getScreen() }
    val screenState = rememberWindowState(
        position = WindowPosition(screen.posX, screen.posY),
        width = screen.width,
        height = screen.height
    )

    Window(
        onCloseRequest = {
            CoroutineScope(Dispatchers.IO + Job()).launch {
                settings.setScreen(
                    width = screenState.size.width.value.toLong(),
                    height = screenState.size.height.value.toLong(),
                    posX = screenState.position.x.value.toLong(),
                    posY = screenState.position.y.value.toLong()
                )
                this@application.exitApplication()
            }
        },
        state = screenState,
        title = getAppName(),
        icon = painterResource("icon.png")
    ) {
        this.window.minimumSize = Dimension(400, 600)
        this.window.background = Color.WHITE
        val vm: MainViewModel = viewModel(
            factory = MainViewModel.Factory(platform = platform, settings = settings)
        )
        App(vm)
    }
}