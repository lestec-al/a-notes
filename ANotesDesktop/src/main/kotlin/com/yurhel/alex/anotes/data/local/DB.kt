package com.yurhel.alex.anotes.data.local

import androidx.compose.ui.unit.dp
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import db.Database
import org.json.JSONArray
import org.json.JSONObject

class DB {

    // INIT DB
    private fun tryInitDB(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:database.db")
        try {
            Database.Schema.create(driver)
        } catch (_: Exception) {}
        return driver
    }
    private val driver = tryInitDB()


    // NOTES DAO
    fun createNote(noteObj: NoteObj) {
        Database(driver).notesQueries.insert(
            withTasks = if (noteObj.withTasks) 1 else 0,
            text = noteObj.text,
            dateUpdate = noteObj.dateUpdate.toString(),
            dateCreate = noteObj.dateCreate.toString()
        )
    }

    fun updateNote(noteObj: NoteObj) {
        Database(driver).notesQueries.update(
            withTasks = if (noteObj.withTasks) 1 else 0,
            text = noteObj.text,
            dateUpdate = noteObj.dateUpdate.toString(),
            id = noteObj.id.toLong()
        )
    }

    fun deleteNote(id: Int) {
        Database(driver).notesQueries.delete(id.toLong())
    }

    fun getLastNote(): NoteObj? {
        val note = try {
            val i = Database(driver).notesQueries.getLast().executeAsOne()
            NoteObj(
                id = i.id.toInt(),
                withTasks = i.withTasks?.toInt() == 1,
                text = i.text ?: "",
                dateUpdate = i.dateUpdate?.toLong() ?: 0,
                dateCreate = i.dateCreate?.toLong() ?: 0
            )
        } catch (_: Exception) {
            null
        }
        return note
    }

    fun getNotes(query: String = ""): List<NoteObj> {
        val list = mutableListOf<NoteObj>()
        val result = Database(driver).notesQueries.getAll().executeAsList()
        for (i in result) {
            if (query == "" || i.text?.lowercase()?.contains(query.lowercase()) == true) {
                list.add(
                    NoteObj(
                        id = i.id.toInt(),
                        withTasks = i.withTasks?.toInt() == 1,
                        text = i.text ?: "",
                        dateUpdate = i.dateUpdate?.toLong() ?: 0,
                        dateCreate = i.dateCreate?.toLong() ?: 0
                    )
                )
            }
        }
        return list
    }


    // SCREENS DAO
    fun getScreen(): WinScreen {
        val result = Database(driver).settingsQueries.getScreen().executeAsOne()
        return WinScreen(
            width = result.screenWidth?.toInt()?.dp ?: 600.dp,
            height = result.screenHeight?.toInt()?.dp ?: 600.dp,
            posX = result.screenPosX?.toInt()?.dp ?: 20.dp,
            posY = result.screenPosY?.toInt()?.dp ?: 20.dp
        )
    }

    fun setScreen(
        width: Long,
        height: Long,
        posX: Long,
        posY: Long
    ) {
        Database(driver).settingsQueries.setScreen(
            screenWidth = width,
            screenHeight = height,
            screenPosX = posX,
            screenPosY = posY
        )
    }


    // SETTINGS DAO
    fun updateReceived(dataReceivedDate: Long?) {
        Database(driver).settingsQueries.updateReceived("${dataReceivedDate ?: ""}")
    }

    fun updateEdit(isNotesEdited: Boolean) {
        Database(driver).settingsQueries.updateEdit(if (isNotesEdited) 1 else 0)
    }

    fun getSettings(): SettingsObj {
        val result = Database(driver).settingsQueries.selectAll().executeAsList()[0]

        var dataReceivedDate: Long? = null
        val x = result.dataReceivedDate
        if (x != null && x != "") dataReceivedDate = x.toLong()

        return SettingsObj(
            dataReceivedDate,
            result.isNotesEdited?.toInt() == 1
        )
    }

    // STATUSES DAO
    fun insertStatus(statusObj: StatusObj) {
        Database(driver).statusesQueries.insert(
            statusObj.title,
            statusObj.color.toLong(),
            statusObj.note.toLong()
        )
    }

    fun updateStatus(statusObj: StatusObj) {
        Database(driver).statusesQueries.update(
            statusObj.title,
            statusObj.color.toLong(),
            statusObj.note.toLong(),
            statusObj.id.toLong()
        )
    }

    fun deleteStatus(id: Int) {
        Database(driver).statusesQueries.delete(id.toLong())
    }

    fun deleteManyByNoteStatuses(note: Int) {
        Database(driver).statusesQueries.deleteManyByNote(note.toLong())
    }

    fun getManyByNoteStatuses(note: Int): List<StatusObj> {
        val list = mutableListOf<StatusObj>()
        val result = Database(driver).statusesQueries.getManyByNote(note.toLong()).executeAsList()
        for (i in result) {
            list.add(
                StatusObj(
                    id = i.id.toInt(),
                    title = i.title ?: "",
                    color = i.color?.toInt() ?: 0,
                    note = i.note?.toInt() ?: 0
                )
            )
        }
        return list
    }


    // TASKS DAO
    fun insertTask(tasksObj: TasksObj) {
        Database(driver).tasksQueries.insert(
            tasksObj.position.toLong(),
            tasksObj.description,
            tasksObj.status.toLong(),
            tasksObj.note.toLong(),
            tasksObj.dateCreate.toString(),
            tasksObj.dateUpdate.toString(),
            tasksObj.dateUpdateStatus.toString()
        )
    }

    fun updateTask(tasksObj: TasksObj) {
        Database(driver).tasksQueries.update(
            tasksObj.position.toLong(),
            tasksObj.description,
            tasksObj.status.toLong(),
            tasksObj.dateUpdate.toString(),
            tasksObj.dateUpdateStatus.toString(),
            tasksObj.id.toLong()
        )
    }

    fun deleteTask(id: Int) {
        Database(driver).tasksQueries.delete(id.toLong())
    }

    fun deleteManyByStatusTasks(status: Int) {
        Database(driver).tasksQueries.deleteManyByStatus(status.toLong())
    }

    fun deleteManyByNoteTasks(note: Int) {
        Database(driver).tasksQueries.deleteManyByNote(note.toLong())
    }

    fun getManyByNoteAndStatusTasks(note: Int, status: Int): List<TasksObj> {
        val list = mutableListOf<TasksObj>()
        val result = Database(driver).tasksQueries.getManyByNoteAndStatus(
            note.toLong(),
            status.toLong()
        ).executeAsList()

        for (i in result) {
            list.add(
                TasksObj(
                    id = i.id.toInt(),
                    position = i.position?.toInt() ?: 0,
                    description = i.description ?: "",
                    status = i.status?.toInt() ?: 0,
                    note = i.note?.toInt() ?: 0,
                    dateCreate = i.dateCreate?.toLong() ?: 0,
                    dateUpdate = i.dateUpdate?.toLong() ?: 0,
                    dateUpdateStatus = i.dateUpdateStatus?.toLong() ?: 0
                )
            )
        }
        return list
    }

    fun getManyByNoteTasks(note: Int): List<TasksObj> {
        val list = mutableListOf<TasksObj>()
        val result = Database(driver).tasksQueries.getManyByNote(note.toLong()).executeAsList()

        for (i in result) {
            list.add(
                TasksObj(
                    id = i.id.toInt(),
                    position = i.position?.toInt() ?: 0,
                    description = i.description ?: "",
                    status = i.status?.toInt() ?: 0,
                    note = i.note?.toInt() ?: 0,
                    dateCreate = i.dateCreate?.toLong() ?: 0,
                    dateUpdate = i.dateUpdate?.toLong() ?: 0,
                    dateUpdateStatus = i.dateUpdateStatus?.toLong() ?: 0
                )
            )
        }
        return list
    }

    fun getLastTask(): TasksObj? {
        val res = Database(driver).tasksQueries.getLast().executeAsList()
        var task: TasksObj? = null
        for (i in res) {
            task = TasksObj(
                id = i.id.toInt(),
                position = i.position?.toInt() ?: 0,
                description = i.description ?: "",
                status = i.status?.toInt() ?: 0,
                note = i.note?.toInt() ?: 0,
                dateCreate = i.dateCreate?.toLong() ?: 0,
                dateUpdate = i.dateUpdate?.toLong() ?: 0,
                dateUpdateStatus = i.dateUpdateStatus?.toLong() ?: 0
            )
        }
        return task
    }

    fun getByPositionTask(position: Int): TasksObj? {
        val res = Database(driver).tasksQueries.getByPosition(position.toLong()).executeAsList()
        var task: TasksObj? = null
        for (i in res) {
            task = TasksObj(
                id = i.id.toInt(),
                position = i.position?.toInt() ?: 0,
                description = i.description ?: "",
                status = i.status?.toInt() ?: 0,
                note = i.note?.toInt() ?: 0,
                dateCreate = i.dateCreate?.toLong() ?: 0,
                dateUpdate = i.dateUpdate?.toLong() ?: 0,
                dateUpdateStatus = i.dateUpdateStatus?.toLong() ?: 0
            )
        }
        return task
    }

    // EXPORT / IMPORT
    fun exportDB(): JSONArray {
        try {
            val jArray = JSONArray()
            for (i in Database(driver).notesQueries.getAll().executeAsList()) {
                val jObj = JSONObject()
                jObj.put("id", i.id.toInt())
                jObj.put("withTasks", i.withTasks == 1L)
                jObj.put("text", i.text ?: "")
                jObj.put("dateUpdate", i.dateUpdate ?: "")
                jObj.put("dateCreate", i.dateCreate ?: "")
                jArray.put(jObj)
            }
            for (i in Database(driver).statusesQueries.getAll().executeAsList()) {
                val jObj = JSONObject()
                jObj.put("id", i.id.toInt())
                jObj.put("title", i.title ?: "")
                jObj.put("color", i.color?.toInt() ?: 0)
                jObj.put("note", i.note?.toInt() ?: 0)
                jArray.put(jObj)
            }
            for (i in Database(driver).tasksQueries.getAll().executeAsList()) {
                val jObj = JSONObject()
                jObj.put("id", i.id.toInt())
                jObj.put("position", i.position?.toInt() ?: 0)
                jObj.put("description", i.description ?: "")
                jObj.put("status", i.status?.toInt() ?: 0)
                jObj.put("note", i.note?.toInt() ?: 0)
                jObj.put("dateUpdate", i.dateUpdate ?: "")
                jObj.put("dateCreate", i.dateCreate ?: "")
                jObj.put("dateUpdateStatus", i.dateUpdateStatus ?: "")
                jArray.put(jObj)
            }
            return jArray
        }  catch (e: Exception) {
            e.printStackTrace()
            return JSONArray()
        }
    }

    fun importDB(data: String): Boolean {
        //val oldData = exportDB()
        try {
            val db = Database(driver)
            db.notesQueries.deleteAll()
            db.statusesQueries.deleteAll()
            db.tasksQueries.deleteAll()
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
                        db.statusesQueries.insertWithId(
                            id = obj.getInt("id").toLong(),
                            title = obj.getString("title"),
                            color = obj.getInt("color").toLong(),
                            note = obj.getInt("note").toLong()
                        )
                    }
                    task != null -> {
                        db.tasksQueries.insertWithId(
                            id = obj.getInt("id").toLong(),
                            position = obj.getInt("position").toLong(),
                            description = obj.getString("description"),
                            status = obj.getInt("status").toLong(),
                            note = obj.getInt("note").toLong(),
                            dateCreate = obj.getString("dateCreate"),
                            dateUpdate = obj.getString("dateUpdate"),
                            dateUpdateStatus = obj.getString("dateUpdateStatus")
                        )
                    }
                    else -> {
                        db.notesQueries.insertWithId(
                            id = obj.getInt("id").toLong(),
                            withTasks = if (obj.getBoolean("withTasks")) 1 else 0,
                            text = obj.getString("text"),
                            dateUpdate = obj.getString("dateUpdate"),
                            dateCreate = obj.getString("dateCreate")
                        )
                    }
                }


            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            //importDB(oldData.toString())
            return false
        }
    }
}