package com.yurhel.alex.anotes.ui

import com.yurhel.alex.anotes.data.Drive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DriveUtils private constructor(
    private val vm: MainViewModel,
    private val drive: Drive
) {
    companion object {
        @Volatile
        private var instance: DriveUtils? = null

        fun getInstance(vm: MainViewModel, drive: Drive): DriveUtils {
            return instance ?: synchronized(this) {
                instance ?: DriveUtils(vm, drive).also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(Job())

    fun driveSyncAuto(
        before: () -> Unit = { vm.changeSyncNow(true) },
        after: () -> Unit = { vm.changeSyncNow(false) }
    ) {
        scope.launch(Dispatchers.Default) {
            try {
                before()

                val appSettings = vm.db.getSettings()
                val data = drive.getData()

                if (data.isServiceOK) {
                    if (!appSettings.isNotesEdited) {
                        // Data not edited
                        if (data.modifiedTime != null) {
                            // Update local
                            vm.db.importDB(data.data.toString())
                            vm.db.updateReceived(data.modifiedTime)
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

    private fun driveSyncManual(isExport: Boolean) {
        if (isExport) {
            // Send data
            drive.sendData(vm.db.exportDB().toString())
            vm.db.updateEdit(false)
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
        vm.db.updateReceived(data.modifiedTime)
    }
}