package com.yurhel.alex.anotes.ui.utils

import com.yurhel.alex.anotes.Drive
import com.yurhel.alex.anotes.ui.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DriveUtils(
    private val vm: MainViewModel,
    private val drive: Drive
) {
    private val scope = CoroutineScope(Job())

    fun driveSyncAuto(
        before: () -> Unit = { vm.changeSyncNow(true) },
        after: () -> Unit = { vm.changeSyncNow(false) }
    ) {
        scope.launch(Dispatchers.Default) {
            try {
                before()

                val appSettings = vm.db.settings.getSettings()
                val data = drive.getData()

                if (data.isServiceOK) {
                    if (!appSettings.isNotesEdited) {
                        // Data not edited
                        if (data.modifiedTime != null) {
                            // Update local
                            vm.db.importDB(data.data.toString())
                            vm.db.settings.updateReceived(data.modifiedTime)
                            vm.getDbNotes("")
                            vm.getAllTasks()
                            vm.getAllStatuses()
                        } else {
                            // If drive empty -> send data
                            driveSyncManual(true)
                        }
                    } else {
                        // Data edited
                        if (data.modifiedTime == appSettings.dataReceivedDate || data.modifiedTime == null) {
                            // Send data
                            driveSyncManual(true)
                        } else {
                            // Get user to choose
                            vm.openSyncDialog(true)
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                after()
            }
        }
    }

    fun driveSyncManualThread(
        isExport: Boolean,
        before: () -> Unit = { vm.changeSyncNow(true) },
        after: () -> Unit = { vm.changeSyncNow(false) }
    ) {
        scope.launch(Dispatchers.Default) {
            before()
            driveSyncManual(isExport)
            after()
        }
    }

    private suspend fun driveSyncManual(isExport: Boolean) {
        if (isExport) {
            // Send data
            drive.sendData(vm.db.exportDB().toString())
            vm.db.settings.updateEdit(false)
        }
        // Get data
        val data = drive.getData()
        if (!isExport && data.modifiedTime != null) {
            // Update local
            vm.db.importDB(data.data.toString())
            vm.getDbNotes("")
            vm.getAllTasks()
            vm.getAllStatuses()
        }
        vm.db.settings.updateReceived(data.modifiedTime)
    }
}