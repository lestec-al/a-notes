package com.yurhel.alex.anotes

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.app_name
import com.yurhel.alex.anotes.data.Drive
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.getSqlDriver
import com.yurhel.alex.anotes.ui.ANotesTheme
import com.yurhel.alex.anotes.ui.DriveUtils
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Navigation
import com.yurhel.alex.anotes.ui.SyncActionTypes
import org.jetbrains.compose.resources.stringResource
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

fun main() = application {

    val db = LocalDB.getInstance(getSqlDriver())

    val screen = db.getScreen()
    val screenState = rememberWindowState(
        position = WindowPosition(screen.posX, screen.posY),
        width = screen.width,
        height = screen.height
    )

    Window(
        onCloseRequest = {
            db.setScreen(
                width = screenState.size.width.value.toLong(),
                height = screenState.size.height.value.toLong(),
                posX = screenState.position.x.value.toLong(),
                posY = screenState.position.y.value.toLong()
            )
            this.exitApplication()
        },
        state = screenState,
        title = stringResource(Res.string.app_name),
        icon = painterResource("icon.png")
    ) {
        // ???
        // Fixes the bug when hovering over buttons (moving up and down)
        // BUT does not fix the blur on the windowed screen
        this.MenuBar {}

        val vm: MainViewModel = viewModel {
            MainViewModel(
                db = db,
                formatDate = { dateLong ->
                    val date = Date(dateLong)
                    // Check if is today
                    val today = Calendar.Builder().build()
                    today.time = Date()
                    val check = Calendar.Builder().build()
                    check.time = date
                    if (
                        today.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR) &&
                        today.get(Calendar.YEAR) == check.get(Calendar.YEAR)
                    ) {
                        DateFormat.getTimeInstance().format(date)
                    } else {
                        DateFormat.getDateInstance().format(date)
                    }
                },
                syncData = { syncActionType, vm ->
                    when (syncActionType) {
                        SyncActionTypes.Auto -> {
                            DriveUtils.getInstance(vm, Drive.getInstance()).driveSyncAuto()
                        }
                        SyncActionTypes.ManualExport -> {
                            DriveUtils.getInstance(vm, Drive.getInstance()).driveSyncManualThread(true)
                        }
                        SyncActionTypes.ManualImport -> {
                            DriveUtils.getInstance(vm, Drive.getInstance()).driveSyncManualThread(false)
                        }
                    }
                },
                // Next used only in Android
                callExit = {},
                widgetIdWhenCreated = 0,
                noteCreatedDateFromWidget = "",
                callUpdateWidget = { _, _, _, _ -> }
            )
        }

        ANotesTheme {
            Navigation(vm = vm)
        }
    }
}