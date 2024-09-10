package com.yurhel.alex.anotes.data

import androidx.compose.ui.unit.dp
import app.cash.sqldelight.db.SqlDriver
import db.Database
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

class LocalDB private constructor(sqlDriver: SqlDriver) {

    companion object {
        @Volatile
        private var instance: LocalDB? = null

        fun getInstance(sqlDriver: SqlDriver): LocalDB {
            return instance ?: synchronized(this) {
                instance ?: LocalDB(sqlDriver).also { instance = it }
            }
        }
    }

    val driver: SqlDriver = sqlDriver



    // NOTES DAO ---------------------
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

    fun getByCreatedNote(noteCreated: String): NoteObj? {
        val note = try {
            val i = Database(driver).notesQueries.getByCreated(noteCreated).executeAsList()[0]
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
        for (i in Database(driver).notesQueries.getQuery(query).executeAsList()) {
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
        return list
    }



    // SCREENS DAO ---------------------
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



    // SETTINGS DAO ---------------------
    fun updateReceived(dataReceivedDate: Long?) {
        Database(driver).settingsQueries.updateReceived("${dataReceivedDate ?: ""}")
    }

    fun updateEdit(isNotesEdited: Boolean) {
        Database(driver).settingsQueries.updateEdit(if (isNotesEdited) 1 else 0)
    }

    fun updateViewMode(viewMode: String) {
        Database(driver).settingsQueries.updateViewMode(viewMode)
    }

    fun getSettings(): SettingsObj {
        val result = Database(driver).settingsQueries.selectAll().executeAsList()[0]

        var dataReceivedDate: Long? = null
        val x = result.dataReceivedDate
        if (x != null && x != "") dataReceivedDate = x.toLong()

        return SettingsObj(
            dataReceivedDate,
            result.isNotesEdited?.toInt() == 1,
            result.viewMode ?: "col"
        )
    }



    // STATUSES DAO ---------------------
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
    fun getAllStatuses(): List<StatusObj> {
        val list = mutableListOf<StatusObj>()
        val result = Database(driver).statusesQueries.getAll().executeAsList()
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



    // TASKS DAO ---------------------
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

    fun getManyByNoteCountTasks(note: Int): Int {
        return Database(driver).tasksQueries.getManyByNoteCount(note.toLong()).executeAsOne().toInt()
    }

    fun getAllTasks(): List<TasksObj> {
        val list = mutableListOf<TasksObj>()
        val result = Database(driver).tasksQueries.getAll().executeAsList()

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



    // WIDGETS DAO ---------------------
    fun insertWidget(w: WidgetObj) {
        Database(driver).widgetsQueries.insert(w.widgetId.toLong(), w.noteCreated)
    }

    fun deleteByIdWidget(widgetId: Int) {
        Database(driver).widgetsQueries.deleteById(widgetId.toLong())
    }

//    fun deleteByCreatedWidget(noteCreated: String) {
//        Database(driver).widgetsQueries.deleteByCreated(noteCreated)
//    }
//    fun getByIdWidget(widgetId: Int): WidgetObj? {
//        val res = Database(driver).widgetsQueries.getById(widgetId.toLong()).executeAsList()
//        var widget: WidgetObj? = null
//        for (i in res) {
//            widget = WidgetObj(
//                id = i.id.toInt(),
//                widgetId = i.widgetId?.toInt() ?: 0,
//                noteCreated = i.noteCreated ?: ""
//            )
//        }
//        return widget
//    }

    fun getByCreatedWidget(noteCreated: String): WidgetObj? {
        val res = Database(driver).widgetsQueries.getByCreated(noteCreated).executeAsList()
        var widget: WidgetObj? = null
        for (i in res) {
            widget = WidgetObj(
                id = i.id.toInt(),
                widgetId = i.widgetId?.toInt() ?: 0,
                noteCreated = i.noteCreated ?: ""
            )
        }
        return widget
    }



    // EXPORT / IMPORT ---------------------
    fun exportDB(): JsonArray {
        try {
            val db = Database(driver)

            return buildJsonArray {
                for (i in db.notesQueries.getAll().executeAsList()) {
                    add(
                        buildJsonObject {
                            put("id", i.id.toInt())
                            put("withTasks", i.withTasks == 1L)
                            put("text", i.text ?: "")
                            put("dateUpdate", i.dateUpdate ?: "")
                            put("dateCreate", i.dateCreate ?: "")
                        }
                    )
                }
                for (i in db.statusesQueries.getAll().executeAsList()) {
                    add(
                        buildJsonObject {
                            put("id", i.id.toInt())
                            put("title", i.title ?: "")
                            put("color", i.color?.toInt() ?: 0)
                            put("note", i.note?.toInt() ?: 0)
                        }
                    )
                }
                for (i in db.tasksQueries.getAll().executeAsList()) {
                    add(
                        buildJsonObject {
                            put("id", i.id.toInt())
                            put("position", i.position?.toInt() ?: 0)
                            put("description", i.description ?: "")
                            put("status", i.status?.toInt() ?: 0)
                            put("note", i.note?.toInt() ?: 0)
                            put("dateUpdate", i.dateUpdate ?: "")
                            put("dateCreate", i.dateCreate ?: "")
                            put("dateUpdateStatus", i.dateUpdateStatus ?: "")
                        }
                    )
                }
            }
        }  catch (e: Exception) {
            e.printStackTrace()
            return JsonArray(emptyList())
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
            val jsonData = Json.decodeFromString<JsonArray>(data)
            for (i in jsonData) {
                val obj = i.jsonObject

                // Check object type
                val status = try {
                    obj["color"]?.jsonPrimitive?.int
                } catch (_: Exception) {
                    null
                }
                val task = try {
                    obj["description"]?.jsonPrimitive?.content
                } catch (_: Exception) {
                    null
                }

                // Insert obj to DB
                when {
                    status != null -> {
                        db.statusesQueries.insertWithId(
                            id = obj["id"]?.jsonPrimitive?.long,
                            title = obj["title"]?.jsonPrimitive?.content,
                            color = obj["color"]?.jsonPrimitive?.long,
                            note = obj["note"]?.jsonPrimitive?.long
                        )
                    }
                    task != null -> {
                        db.tasksQueries.insertWithId(
                            id = obj["id"]?.jsonPrimitive?.long,
                            position = obj["position"]?.jsonPrimitive?.long,
                            description = obj["description"]?.jsonPrimitive?.content,
                            status = obj["status"]?.jsonPrimitive?.long,
                            note = obj["note"]?.jsonPrimitive?.long,
                            dateCreate = obj["dateCreate"]?.jsonPrimitive?.content,
                            dateUpdate = obj["dateUpdate"]?.jsonPrimitive?.content,
                            dateUpdateStatus = obj["dateUpdateStatus"]?.jsonPrimitive?.content
                        )
                    }
                    else -> {
                        db.notesQueries.insertWithId(
                            id = obj["id"]?.jsonPrimitive?.long,
                            withTasks = if (obj["withTasks"]?.jsonPrimitive?.boolean == true) 1 else 0,
                            text = obj["text"]?.jsonPrimitive?.content,
                            dateUpdate = obj["dateUpdate"]?.jsonPrimitive?.content,
                            dateCreate = obj["dateCreate"]?.jsonPrimitive?.content
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