package com.yurhel.alex.anotes.ui

import android.content.Context
import com.yurhel.alex.anotes.data.drive.Drive
import com.yurhel.alex.anotes.data.local.obj.NoteObj
import com.yurhel.alex.anotes.data.local.obj.SettingsObj
import com.yurhel.alex.anotes.data.local.obj.StatusObj
import com.yurhel.alex.anotes.data.local.obj.TasksObj
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

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
        context: Context,
        before: () -> Unit = { vm.changeSyncNow(true) },
        after: () -> Unit = { vm.changeSyncNow(false) }
    ) {
        scope.launch(Dispatchers.Default) {
            try {
                before()

                var appSettings = vm.db.setting.getS()
                appSettings = if (appSettings != null) {
                    appSettings
                } else {
                    val newSettings = SettingsObj()
                    vm.db.setting.upsert(newSettings)
                    newSettings
                }
                val data = drive.getData(context)

                if (data.isServiceOK) {
                    if (!appSettings.isNotesEdited) {
                        // Data not edited
                        if (data.modifiedTime != null) {
                            // Update local
                            importDB(data.data.toString())
                            vm.db.setting.upsert(
                                vm.db.setting.getS()?.copy(dataReceivedDate = data.modifiedTime) ?: SettingsObj(dataReceivedDate = data.modifiedTime)
                            )
                            vm.getDbNotes("")
                            vm.getAllTasks()
                            vm.getAllStatuses()
                        } else {
                            // If drive empty -> send data
                            driveSyncManual(true, context)
                        }
                    } else {
                        // Data edited
                        if (data.modifiedTime == appSettings.dataReceivedDate || data.modifiedTime == null) {
                            // Send data
                            driveSyncManual(true, context)
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
        context: Context,
        before: () -> Unit = { vm.changeSyncNow(true) },
        after: () -> Unit = { vm.changeSyncNow(false) }
    ) {
        scope.launch(Dispatchers.Default) {
            before()
            driveSyncManual(isExport, context)
            after()
        }
    }

    private suspend fun driveSyncManual(
        isExport: Boolean,
        context: Context
    ) {
        if (isExport) {
            // Send data
            drive.sendData(exportDB().toString(), context)
            vm.db.setting.upsert(vm.db.setting.getS()?.copy(isNotesEdited = false) ?: SettingsObj(isNotesEdited = false))
        }
        // Get data
        val data = drive.getData(context)
        if (!isExport && data.modifiedTime != null) {
            // Update local
            importDB(data.data.toString())
            vm.getDbNotes("")
            vm.getAllTasks()
            vm.getAllStatuses()
        }
        vm.db.setting.upsert(
            vm.db.setting.getS()?.copy(dataReceivedDate = data.modifiedTime) ?: SettingsObj(dataReceivedDate = data.modifiedTime)
        )
    }

    // EXPORT / IMPORT DATA
    private fun exportDB(): JSONArray {
        return try {
            val jArray = JSONArray()
            for (i in vm.db.note.getAll()) {
                val jObj = JSONObject()
                jObj.put("id", i.id)
                jObj.put("withTasks", i.withTasks)
                jObj.put("text", i.text)
                jObj.put("dateUpdate", i.dateUpdate.toString())
                jObj.put("dateCreate", i.dateCreate.toString())
                jArray.put(jObj)
            }
            for (i in vm.db.status.getAll()) {
                val jObj = JSONObject()
                jObj.put("id", i.id)
                jObj.put("title", i.title)
                jObj.put("color", i.color)
                jObj.put("note", i.note)
                jArray.put(jObj)
            }
            for (i in vm.db.task.getAll()) {
                val jObj = JSONObject()
                jObj.put("id", i.id)
                jObj.put("position", i.position)
                jObj.put("description", i.description)
                jObj.put("status", i.status)
                jObj.put("note", i.note)
                jObj.put("dateUpdate", i.dateUpdate.toString())
                jObj.put("dateCreate", i.dateCreate.toString())
                jObj.put("dateUpdateStatus", i.dateUpdateStatus.toString())
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
            vm.db.status.deleteAll()
            vm.db.task.deleteAll()
            // Loop data
            val jsonData = JSONArray(data)
            for (i in 0..<jsonData.length()) {
                val obj = jsonData.getJSONObject(i)
                // Check object type
                val status = try {
                    obj.getInt("color")
                } catch (_: Exception) {
                    null
                }
                val task = try {
                    obj.getString("description")
                } catch (_: Exception) {
                    null
                }
                // Insert obj to DB
                when {
                    status != null -> {
                        vm.db.status.upsert(
                            StatusObj(
                                id = obj.getInt("id"),
                                title = obj.getString("title"),
                                color = obj.getInt("color"),
                                note = obj.getInt("note")
                            )
                        )
                    }
                    task != null -> {
                        vm.db.task.upsert(
                            TasksObj(
                                id = obj.getInt("id"),
                                position = obj.getInt("position"),
                                description = obj.getString("description"),
                                status = obj.getInt("status"),
                                note = obj.getInt("note"),
                                dateCreate = obj.getString("dateCreate").toLong(),
                                dateUpdate = obj.getString("dateUpdate").toLong(),
                                dateUpdateStatus = obj.getString("dateUpdateStatus").toLong(),
                            )
                        )
                    }
                    else -> {
                        vm.db.note.upsert(
                            NoteObj(
                                id = obj.getInt("id"),
                                withTasks = obj.getBoolean("withTasks"),
                                text = obj.getString("text"),
                                dateUpdate = obj.getString("dateUpdate").toLong(),
                                dateCreate = obj.getString("dateCreate").toLong()
                            )
                        )
                    }
                }
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