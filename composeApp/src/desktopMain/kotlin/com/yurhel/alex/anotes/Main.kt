package com.yurhel.alex.anotes

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.app_name
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.ui.DriveUtils
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Navigation
import com.yurhel.alex.anotes.ui.SyncActionTypes
import com.yurhel.alex.anotes.ui.theme.ANotesTheme
import db.Database
import org.jetbrains.compose.resources.stringResource
import java.awt.Color
import java.awt.Dimension
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

fun main() = application {
    // Fixes problems with sharpness & flickering ?
    System.setProperty("skiko.renderApi", "OPENGL")

    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:notes.db")
    try { Database.Schema.create(driver) } catch (_: Exception) {}
    val db = LocalDB.getInstance(driver)

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
        this.window.minimumSize = Dimension(600, 600)
        this.window.background = Color.WHITE

        val vm: MainViewModel = viewModel(
            factory = MainViewModel.Factory(
                showToast = {},
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
                    val driveUtils = DriveUtils(vm, Drive())
                    when (syncActionType) {
                        SyncActionTypes.Auto -> driveUtils.driveSyncAuto()
                        SyncActionTypes.ManualExport -> driveUtils.driveSyncManualThread(true)
                        SyncActionTypes.ManualImport -> driveUtils.driveSyncManualThread(false)
                    }
                },
                // Next used only in Android
                callExit = {},
                widgetIdWhenCreated = 0,
                callInitUpdateWidget = { _, _, _, _ -> }
            )
        )

        ANotesTheme {
            Navigation(vm = vm)
        }
    }
}