package com.yurhel.alex.anotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurhel.alex.anotes.data.Drive
import com.yurhel.alex.anotes.data.NoteObj
import com.yurhel.alex.anotes.data.SettingsObj
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class DriveViewModel(
    private val vm: MainViewModel,
    private val drive: Drive
): ViewModel() {

    fun driveSyncAuto(
        before: () -> Unit = { vm.changeSyncNow(true) },
        after: () -> Unit = { vm.changeSyncNow(false) }
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                before()
                vm.callTrySighIn()

                var appSettings = vm.db.settings.getS()
                appSettings = if (appSettings != null) {
                    appSettings
                } else {
                    val newSettings = SettingsObj()
                    vm.db.settings.upsert(newSettings)
                    newSettings
                }
                val data = drive.getData()
                val driveData = data.first
                val dataModifiedTime = data.second

                if (!appSettings.isNotesEdited) {
                    // Data not edited
                    if (dataModifiedTime != null) {
                        // Update local
                        importDB(driveData.toString())
                        vm.db.settings.upsert(
                            vm.db.settings.getS()?.copy(dataReceivedDate = dataModifiedTime) ?: SettingsObj(dataReceivedDate = dataModifiedTime)
                        )
                        vm.getDbNotes("")
                    } else {
                        // If drive empty -> send data
                        driveSyncManual(true)
                    }
                } else {
                    // Data edited
                    if (dataModifiedTime == appSettings.dataReceivedDate || dataModifiedTime == null) {
                        // Send data
                        driveSyncManual(true)
                    } else {
                        // Get user to choose
                        vm.openSyncDialog(true)
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
        viewModelScope.launch(Dispatchers.Default) {
            before()
            driveSyncManual(isExport)
            after()
        }
    }

    private suspend fun driveSyncManual(isExport: Boolean) {
        if (isExport) {
            // Send data
            drive.sendData(exportDB().toString())
            vm.db.settings.upsert(vm.db.settings.getS()?.copy(isNotesEdited = false) ?: SettingsObj(isNotesEdited = false))
        }
        // Get data
        val data = drive.getData()
        if (!isExport && data.second != null) {
            // Update local
            importDB(data.first.toString())
            vm.getDbNotes("")
        }
        vm.db.settings.upsert(
            vm.db.settings.getS()?.copy(dataReceivedDate = data.second) ?: SettingsObj(dataReceivedDate = data.second)
        )
    }

    // EXPORT / IMPORT DATA
    private fun exportDB(): JSONArray {
        return try {
            val jArray = JSONArray()
            for (i in vm.db.note.getAll()) {
                val jObj = JSONObject()
                jObj.put("id", i.id)
                jObj.put("text", i.text)
                jObj.put("dateUpdate", i.dateUpdate)
                jObj.put("dateCreate", i.dateCreate)
                jArray.put(jObj)
            }
            jArray
        }  catch (e: Exception) {
            e.printStackTrace()
            JSONArray()
        }
    }

    private suspend fun importDB(data: String): Boolean {
        //val oldData = exportDB()
        try {
            // Clear old data
            vm.db.note.deleteAll()
            // Loop data
            val jsonData = JSONArray(data)
            for (i in 0..<jsonData.length()) {
                val obj = jsonData.getJSONObject(i)
                // Insert obj to DB
                vm.db.note.upsert(
                    NoteObj(
                        //id = obj.getInt("id"),
                        text = obj.getString("text"),
                        dateUpdate = obj.getString("dateUpdate"),
                        dateCreate = obj.getString("dateCreate")
                    )
                )
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            //cleanDatabase()
            //importDB(oldData.toString())
            return false
        }
    }
}