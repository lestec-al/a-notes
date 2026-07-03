package com.yurhel.alex.anotes.ui.utils

import com.yurhel.alex.anotes.PlatformDrive
import com.yurhel.alex.anotes.data.DriveData
import com.yurhel.alex.anotes.ui.MainViewModel

class DriveUtils(
    private val vm: MainViewModel,
    private val drive: PlatformDrive
) {
    suspend fun syncAuto(
        before: () -> Unit = { vm.updateSyncNow(true) },
        after: () -> Unit = { vm.updateSyncNow(false) }
    ) {
        try {
            before()

            val isNotesEdited = vm.settings.getIsNotesEdited()
            val lastReceived = vm.settings.getDataReceivedDate()
            val data = drive.getData()

            if (data.isServiceOK) {
                if (data.modifiedTime == null) {
                    sync(true)
                } else {
                    if (isNotesEdited) {
                        if (data.modifiedTime == lastReceived) {
                            sync(true)
                        } else {
                            vm.setSyncDialogVisibility(true)
                        }
                    } else {
                        updateLocal(data)
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            after()
        }
    }

    suspend fun syncManual(
        isExport: Boolean,
        before: () -> Unit = { vm.updateSyncNow(true) },
        after: () -> Unit = { vm.updateSyncNow(false) }
    ) {
        before()
        sync(isExport)
        after()
    }

    private suspend fun sync(isExport: Boolean) {
        if (isExport) {
            drive.sendData(vm.db.exportDB().toString())
            vm.settings.setIsNotesEdited(false)
        }
        val data = drive.getData()
        if (!isExport && data.modifiedTime != null) {
            updateLocal(data)
        } else {
            vm.settings.setDataReceivedDate(data.modifiedTime)
        }
    }

    private suspend fun updateLocal(data: DriveData) {
        vm.db.importDB(data.data.toString())
        vm.settings.setIsNotesEdited(false)
        vm.settings.setDataReceivedDate(data.modifiedTime)
        vm.getDbNotes("")
        vm.getAllTasks()
        vm.getAllStatuses()
    }
}