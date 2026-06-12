package com.yurhel.alex.anotes.ui.utils

import com.yurhel.alex.anotes.PlatformDrive
import com.yurhel.alex.anotes.ui.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DriveUtils(
    private val vm: MainViewModel,
    private val drive: PlatformDrive
) {
    private val scope = CoroutineScope(Job())

    fun driveSyncAuto(
        before: () -> Unit = { vm.updateSyncNow(true) },
        after: () -> Unit = { vm.updateSyncNow(false) }
    ) {
        scope.launch(Dispatchers.Default) {
            try {
                before()

                val isNotesEdited = vm.settings.getIsNotesEdited()
                val dataReceivedDate = vm.settings.getDataReceivedDate()
                val data = drive.getData()

                if (data.isServiceOK) {
                    if (!isNotesEdited) {
                        // Data not edited
                        if (data.modifiedTime != null) {
                            // Update local
                            vm.db.importDB(data.data.toString())
                            vm.settings.setDataReceivedDate(data.modifiedTime)
                            vm.getDbNotes("")
                            vm.getAllTasks()
                            vm.getAllStatuses()
                        } else {
                            // If drive empty -> send data
                            driveSyncManual(true)
                        }
                    } else {
                        // Data edited
                        if (data.modifiedTime == dataReceivedDate || data.modifiedTime == null) {
                            // Send data
                            driveSyncManual(true)
                        } else {
                            // Get user to choose
                            vm.syncDialogVisibility(true)
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
        before: () -> Unit = { vm.updateSyncNow(true) },
        after: () -> Unit = { vm.updateSyncNow(false) }
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
            vm.settings.setIsNotesEdited(false)
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
        vm.settings.setDataReceivedDate(data.modifiedTime)
    }
}